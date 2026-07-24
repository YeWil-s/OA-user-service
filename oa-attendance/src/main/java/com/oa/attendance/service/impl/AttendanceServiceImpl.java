package com.oa.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.attendance.client.UserServiceClient;
import com.oa.attendance.dto.AttendanceRecordQueryDTO;
import com.oa.attendance.dto.FieldWorkDTO;
import com.oa.attendance.dto.LeaveDTO;
import com.oa.attendance.dto.OvertimeDTO;
import com.oa.attendance.dto.PunchDTO;
import com.oa.attendance.dto.ScheduleItem;
import com.oa.attendance.dto.ShiftDTO;
import com.oa.attendance.dto.UserShiftDTO;
import com.oa.attendance.dto.VisualAttendanceStatsDTO;
import com.oa.attendance.dto.VisualTodayAttendanceDTO;
import com.oa.attendance.entity.AttDailySummary;
import com.oa.attendance.entity.AttRecord;
import com.oa.attendance.entity.AttSchedule;
import com.oa.attendance.entity.AttShift;
import com.oa.attendance.entity.UserShift;
import com.oa.attendance.mapper.AttDailySummaryMapper;
import com.oa.attendance.mapper.AttRecordMapper;
import com.oa.attendance.mapper.AttScheduleMapper;
import com.oa.attendance.mapper.AttShiftMapper;
import com.oa.attendance.mapper.UserShiftMapper;
import com.oa.attendance.service.IAttendanceService;
import com.oa.attendance.vo.AttendanceRecordVO;
import com.oa.attendance.vo.PunchVO;
import com.oa.attendance.vo.ScheduleVO;
import com.oa.attendance.vo.ShiftVO;
import com.oa.attendance.vo.UserShiftVO;
import com.oa.common.context.CurrentUser;
import com.oa.common.context.UserContextHolder;
import com.oa.common.exception.BusinessException;
import com.oa.common.result.ResultCode;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AttendanceServiceImpl implements IAttendanceService {

    private final AttShiftMapper attShiftMapper;
    private final AttRecordMapper attRecordMapper;
    private final UserShiftMapper userShiftMapper;
    private final AttScheduleMapper attScheduleMapper;
    private final AttDailySummaryMapper dailySummaryMapper;
    private final UserServiceClient userServiceClient;
    private final com.oa.attendance.client.NoticeServiceClient noticeServiceClient;

    private static final int MSG_TYPE_ATTENDANCE = 2;

    public AttendanceServiceImpl(AttShiftMapper attShiftMapper,
                                 AttRecordMapper attRecordMapper,
                                 UserShiftMapper userShiftMapper,
                                 AttScheduleMapper attScheduleMapper,
                                 AttDailySummaryMapper dailySummaryMapper,
                                 UserServiceClient userServiceClient,
                                 com.oa.attendance.client.NoticeServiceClient noticeServiceClient) {
        this.attShiftMapper = attShiftMapper;
        this.attRecordMapper = attRecordMapper;
        this.userShiftMapper = userShiftMapper;
        this.attScheduleMapper = attScheduleMapper;
        this.dailySummaryMapper = dailySummaryMapper;
        this.userServiceClient = userServiceClient;
        this.noticeServiceClient = noticeServiceClient;
    }

    @Override
    public IPage<ShiftVO> pageShifts(Integer pageNum, Integer pageSize) {
        requireAdminOrHr();
        Page<AttShift> page = attShiftMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<AttShift>().orderByDesc(AttShift::getCreateTime));
        return page.convert(this::toShiftVO);
    }

    @Override
    public ShiftVO getShift(Long id) {
        requireAdminOrHr();
        return toShiftVO(getShiftEntity(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShiftVO createShift(ShiftDTO dto) {
        requireAdminOrHr();
        validateShiftTime(dto);
        AttShift shift = new AttShift();
        fillShift(shift, dto);
        attShiftMapper.insert(shift);
        return toShiftVO(shift);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShiftVO updateShift(Long id, ShiftDTO dto) {
        requireAdminOrHr();
        validateShiftTime(dto);
        AttShift shift = getShiftEntity(id);
        fillShift(shift, dto);
        attShiftMapper.updateById(shift);
        return toShiftVO(shift);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteShift(Long id) {
        requireAdminOrHr();
        getShiftEntity(id);
        Long assignedCount = userShiftMapper.selectCount(new LambdaQueryWrapper<UserShift>()
                .eq(UserShift::getShiftId, id));
        if (assignedCount > 0) {
            throw new BusinessException(ResultCode.CONFLICT, "该班次已分配给员工，不能删除");
        }
        attShiftMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignShift(UserShiftDTO dto) {
        requireAdminOrHr();
        userServiceClient.requireActiveUser(dto.getUserId());
        AttShift shift = getShiftEntity(dto.getShiftId());
        if (Integer.valueOf(0).equals(shift.getStatus())) {
            throw new BusinessException(ResultCode.NOT_FOUND, "可用班次不存在");
        }
        UserShift mapping = userShiftMapper.selectOne(new LambdaQueryWrapper<UserShift>()
                .eq(UserShift::getUserId, dto.getUserId())
                .last("limit 1"));
        if (mapping == null) {
            mapping = new UserShift();
            mapping.setUserId(dto.getUserId());
            mapping.setShiftId(dto.getShiftId());
            userShiftMapper.insert(mapping);
            return;
        }
        mapping.setShiftId(dto.getShiftId());
        userShiftMapper.updateById(mapping);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PunchVO punchIn(PunchDTO dto) {
        CurrentUser currentUser = UserContextHolder.getCurrentUser();
        userServiceClient.requireActiveUser(currentUser.getUserId());
        PunchDTO safeDto = dto == null ? new PunchDTO() : dto;
        LocalDate today = LocalDate.now();
        AttRecord record = selectDailyRecord(currentUser.getUserId(), today);
        if (record != null && Integer.valueOf(3).equals(record.getPunchType())) {
            throw new BusinessException(ResultCode.ON_LEAVE);
        }
        if (record != null && record.getPunchInTime() != null) {
            throw new BusinessException(ResultCode.ALREADY_PUNCHED_IN, "今天已经完成上班打卡");
        }
        if (record != null && record.getPunchOutTime() != null) {
            throw new BusinessException(ResultCode.CONFLICT, "今天已经完成下班打卡，不能再补上班打卡");
        }
        LocalDateTime now = LocalDateTime.now();
        if (record == null) {
            record = new AttRecord();
            record.setUserId(currentUser.getUserId());
            record.setRecordDate(today);
            record.setPunchType(resolvePunchType(safeDto));
            record.setPunchInTime(now);
            record.setDeviceInfo(safeDto.getDeviceInfo());
            record.setLocation(safeDto.getLocation());
            record.setLatitude(safeDto.getLatitude());
            record.setLongitude(safeDto.getLongitude());
            try {
                attRecordMapper.insert(record);
            } catch (DuplicateKeyException ex) {
                throw new BusinessException(ResultCode.ALREADY_PUNCHED_IN, "今天已经完成上班打卡");
            }
        } else {
            record.setPunchInTime(now);
            record.setPunchType(resolvePunchType(safeDto));
            record.setDeviceInfo(safeDto.getDeviceInfo());
            record.setLocation(safeDto.getLocation());
            record.setLatitude(safeDto.getLatitude());
            record.setLongitude(safeDto.getLongitude());
            attRecordMapper.updateById(record);
        }
        PunchVO vo = buildPunchVO("上班打卡成功", record, resolveShiftForUser(currentUser.getUserId()), now, false);
        if (vo.getLateMinutes() != null && vo.getLateMinutes() > 0) {
            noticeServiceClient.sendMessage(currentUser.getUserId(),
                    "上班打卡迟到提醒",
                    "您今天上班打卡迟到" + vo.getLateMinutes() + "分钟。",
                    MSG_TYPE_ATTENDANCE,
                    record.getId());
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PunchVO punchOut(PunchDTO dto) {
        CurrentUser currentUser = UserContextHolder.getCurrentUser();
        userServiceClient.requireActiveUser(currentUser.getUserId());
        PunchDTO safeDto = dto == null ? new PunchDTO() : dto;
        LocalDate today = LocalDate.now();
        AttRecord record = selectDailyRecord(currentUser.getUserId(), today);
        if (record != null && Integer.valueOf(3).equals(record.getPunchType())) {
            throw new BusinessException(ResultCode.ON_LEAVE);
        }
        if (record == null || record.getPunchInTime() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "请先完成上班打卡");
        }
        if (record.getPunchOutTime() != null) {
            throw new BusinessException(ResultCode.CONFLICT, "今天已经完成下班打卡");
        }
        LocalDateTime now = LocalDateTime.now();
        record.setPunchOutTime(now);
        record.setPunchType(resolvePunchType(safeDto));
        record.setDeviceInfo(safeDto.getDeviceInfo());
        record.setLocation(safeDto.getLocation());
        record.setLatitude(safeDto.getLatitude());
        record.setLongitude(safeDto.getLongitude());
        attRecordMapper.updateById(record);
        PunchVO vo = buildPunchVO("下班打卡成功", record, resolveShiftForUser(currentUser.getUserId()), now, true);
        if (vo.getEarlyMinutes() != null && vo.getEarlyMinutes() > 0) {
            noticeServiceClient.sendMessage(currentUser.getUserId(),
                    "下班打卡早退提醒",
                    "您今天下班打卡早退" + vo.getEarlyMinutes() + "分钟。",
                    MSG_TYPE_ATTENDANCE,
                    record.getId());
        }
        return vo;
    }

    @Override
    public IPage<AttendanceRecordVO> myRecords(AttendanceRecordQueryDTO dto) {
        CurrentUser currentUser = UserContextHolder.getCurrentUser();
        userServiceClient.requireActiveUser(currentUser.getUserId());
        return queryRecords(Collections.singletonList(currentUser.getUserId()), dto);
    }

    @Override
    public IPage<AttendanceRecordVO> deptRecords(AttendanceRecordQueryDTO dto) {
        requireAdminHrOrLeader();
        CurrentUser currentUser = UserContextHolder.getCurrentUser();
        com.oa.common.remote.UserInfo currentUserInfo = userServiceClient.requireActiveUser(currentUser.getUserId());
        Long deptId = currentUserInfo.getDeptId() == null ? currentUser.getDeptId() : currentUserInfo.getDeptId();
        List<Long> deptIds = userServiceClient.collectDeptAndChildren(deptId);
        List<Long> userIds = deptIds.stream()
                .flatMap(scopedDeptId -> userServiceClient.listUsersByDept(scopedDeptId).stream())
                .map(com.oa.common.remote.UserInfo::getId)
                .distinct()
                .toList();
        return queryRecords(userIds, dto);
    }

    @Override
    public IPage<AttendanceRecordVO> allRecords(AttendanceRecordQueryDTO dto) {
        requireAdminOrHr();
        return queryRecords(null, dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markLeave(LeaveDTO dto) {
        LocalDate date = dto.getStartDate();
        while (!date.isAfter(dto.getEndDate())) {
            // 更新考勤记录
            AttRecord existing = attRecordMapper.selectOne(new LambdaQueryWrapper<AttRecord>()
                    .eq(AttRecord::getUserId, dto.getUserId())
                    .eq(AttRecord::getRecordDate, date)
                    .last("limit 1"));
            if (existing == null) {
                AttRecord leaveRecord = new AttRecord();
                leaveRecord.setUserId(dto.getUserId());
                leaveRecord.setRecordDate(date);
                leaveRecord.setPunchType(3);
                leaveRecord.setCreateTime(LocalDateTime.now());
                attRecordMapper.insert(leaveRecord);
            } else if (existing.getPunchInTime() == null && existing.getPunchOutTime() == null) {
                existing.setPunchType(3);
                attRecordMapper.updateById(existing);
            }

            // 同步更新排班表状态为请假
            AttSchedule schedule = attScheduleMapper.selectOne(new LambdaQueryWrapper<AttSchedule>()
                    .eq(AttSchedule::getUserId, dto.getUserId())
                    .eq(AttSchedule::getScheduleDate, date)
                    .last("limit 1"));
            if (schedule != null) {
                schedule.setStatus(2);
                attScheduleMapper.updateById(schedule);
            } else {
                // 无排班记录时，插入一条请假标记（使用默认班次）
                UserShift userShift = userShiftMapper.selectOne(new LambdaQueryWrapper<UserShift>()
                        .eq(UserShift::getUserId, dto.getUserId())
                        .last("limit 1"));
                AttSchedule leaveSchedule = new AttSchedule();
                leaveSchedule.setUserId(dto.getUserId());
                leaveSchedule.setScheduleDate(date);
                leaveSchedule.setShiftId(userShift != null ? userShift.getShiftId() : 1L);
                leaveSchedule.setStatus(2);
                leaveSchedule.setCreateTime(LocalDateTime.now());
                attScheduleMapper.insert(leaveSchedule);
            }
            date = date.plusDays(1);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelLeave(Long applicationId) {
        // 请假撤销时删除对应日期的请假记录（仅删除仅包含请假标记的空记录）
        attRecordMapper.delete(new LambdaQueryWrapper<AttRecord>()
                .eq(AttRecord::getPunchType, 3)
                .isNull(AttRecord::getPunchInTime)
                .isNull(AttRecord::getPunchOutTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markOvertime(OvertimeDTO dto) {
        LocalDate date = dto.getStartDate();
        while (!date.isAfter(dto.getEndDate())) {
            AttSchedule existing = attScheduleMapper.selectOne(new LambdaQueryWrapper<AttSchedule>()
                    .eq(AttSchedule::getUserId, dto.getUserId())
                    .eq(AttSchedule::getScheduleDate, date)
                    .last("limit 1"));
            if (existing != null) {
                existing.setOvertimeHours(dto.getOvertimeHours());
                existing.setApplicationId(dto.getApplicationId());
                attScheduleMapper.updateById(existing);
            } else {
                UserShift userShift = userShiftMapper.selectOne(new LambdaQueryWrapper<UserShift>()
                        .eq(UserShift::getUserId, dto.getUserId())
                        .last("limit 1"));
                AttSchedule schedule = new AttSchedule();
                schedule.setUserId(dto.getUserId());
                schedule.setScheduleDate(date);
                schedule.setShiftId(userShift != null ? userShift.getShiftId() : 1L);
                schedule.setStatus(1);
                schedule.setOvertimeHours(dto.getOvertimeHours());
                schedule.setApplicationId(dto.getApplicationId());
                schedule.setCreateTime(LocalDateTime.now());
                attScheduleMapper.insert(schedule);
            }
            date = date.plusDays(1);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOvertime(Long applicationId) {
        List<AttSchedule> overtimeSchedules = attScheduleMapper.selectList(new LambdaQueryWrapper<AttSchedule>()
                .eq(AttSchedule::getApplicationId, applicationId));
        for (AttSchedule s : overtimeSchedules) {
            s.setOvertimeHours(java.math.BigDecimal.ZERO);
            s.setApplicationId(null);
            attScheduleMapper.updateById(s);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markFieldWork(FieldWorkDTO dto) {
        LocalDate date = dto.getStartDate();
        while (!date.isAfter(dto.getEndDate())) {
            AttRecord existing = attRecordMapper.selectOne(new LambdaQueryWrapper<AttRecord>()
                    .eq(AttRecord::getUserId, dto.getUserId())
                    .eq(AttRecord::getRecordDate, date)
                    .last("limit 1"));
            if (existing != null) {
                existing.setPunchType(2);
                attRecordMapper.updateById(existing);
            } else {
                AttRecord fieldRecord = new AttRecord();
                fieldRecord.setUserId(dto.getUserId());
                fieldRecord.setRecordDate(date);
                fieldRecord.setPunchType(2);
                fieldRecord.setCreateTime(LocalDateTime.now());
                attRecordMapper.insert(fieldRecord);
            }
            date = date.plusDays(1);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelFieldWork(Long applicationId) {
        // 删除仅有外勤标记且无实际打卡时间的记录，恢复有打卡记录的 punch_type 为 1
        List<AttRecord> fieldRecords = attRecordMapper.selectList(new LambdaQueryWrapper<AttRecord>()
                .eq(AttRecord::getPunchType, 2));
        for (AttRecord record : fieldRecords) {
            if (record.getPunchInTime() == null && record.getPunchOutTime() == null) {
                attRecordMapper.deleteById(record.getId());
            } else {
                record.setPunchType(1);
                attRecordMapper.updateById(record);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateDailySummary(LocalDate date) {
        dailySummaryMapper.delete(new LambdaQueryWrapper<AttDailySummary>()
                .eq(AttDailySummary::getSummaryDate, date));

        List<com.oa.common.remote.UserInfo> activeUsers = userServiceClient.listAllActiveUsers();
        for (com.oa.common.remote.UserInfo user : activeUsers) {
            AttDailySummary summary = buildDailySummary(user, date);
            if (summary != null) {
                dailySummaryMapper.insert(summary);
            }
        }
    }

    private AttDailySummary buildDailySummary(com.oa.common.remote.UserInfo user, LocalDate date) {
        AttShift shift = resolveShiftForUser(user.getId(), date);
        if (shift == null || Integer.valueOf(0).equals(shift.getStatus())) {
            return null;
        }

        AttRecord record = selectDailyRecord(user.getId(), date);
        AttDailySummary summary = new AttDailySummary();
        summary.setUserId(user.getId());
        summary.setDeptId(user.getDeptId());
        summary.setSummaryDate(date);
        summary.setShiftId(shift.getId());

        if (record == null) {
            summary.setStatus(4); // 旷工
            summary.setLateMinutes(0);
            summary.setEarlyMinutes(0);
            summary.setWorkHours(java.math.BigDecimal.ZERO);
            summary.setOvertimeHours(java.math.BigDecimal.ZERO);
            return summary;
        }

        // 请假标记（只有 punchType=3 且无打卡时间）
        if (Integer.valueOf(3).equals(record.getPunchType())
                && record.getPunchInTime() == null
                && record.getPunchOutTime() == null) {
            summary.setStatus(6); // 请假
            summary.setLateMinutes(0);
            summary.setEarlyMinutes(0);
            summary.setWorkHours(java.math.BigDecimal.ZERO);
            summary.setOvertimeHours(java.math.BigDecimal.ZERO);
            return summary;
        }

        summary.setPunchInTime(record.getPunchInTime());
        summary.setPunchOutTime(record.getPunchOutTime());

        // 缺卡：有打卡但缺一个
        if (record.getPunchInTime() == null || record.getPunchOutTime() == null) {
            summary.setStatus(5); // 缺卡
            summary.setLateMinutes(0);
            summary.setEarlyMinutes(0);
            summary.setWorkHours(java.math.BigDecimal.ZERO);
            summary.setOvertimeHours(java.math.BigDecimal.ZERO);
            return summary;
        }

        int lateMinutes = calcLateMinutes(record, shift);
        int earlyMinutes = calcEarlyMinutes(record, shift);
        summary.setLateMinutes(lateMinutes);
        summary.setEarlyMinutes(earlyMinutes);

        // 确定状态
        if (lateMinutes > 0 && earlyMinutes > 0) {
            summary.setStatus(2); // 迟到+早退 → 按迟到算
        } else if (lateMinutes > 0) {
            summary.setStatus(2); // 迟到
        } else if (earlyMinutes > 0) {
            summary.setStatus(3); // 早退
        } else {
            summary.setStatus(1); // 正常
        }

        // 计算工时
        summary.setWorkHours(calcWorkHours(record));

        // 计算加班（不覆盖 base status，加班时长单独记录）
        if (shift.getEndTime() != null && record.getPunchOutTime() != null) {
            java.time.LocalDateTime standardEnd = java.time.LocalDateTime.of(date, shift.getEndTime());
            long overtimeMinutes = java.time.Duration.between(standardEnd, record.getPunchOutTime()).toMinutes();
            if (overtimeMinutes > 0) {
                summary.setOvertimeHours(java.math.BigDecimal.valueOf(overtimeMinutes)
                        .divide(java.math.BigDecimal.valueOf(60), 1, java.math.RoundingMode.HALF_UP));
            } else {
                summary.setOvertimeHours(java.math.BigDecimal.ZERO);
            }
        } else {
            summary.setOvertimeHours(java.math.BigDecimal.ZERO);
        }

        return summary;
    }

    private IPage<AttendanceRecordVO> queryRecords(List<Long> userIds, AttendanceRecordQueryDTO dto) {
        AttendanceRecordQueryDTO safeDto = dto == null ? new AttendanceRecordQueryDTO() : dto;
        DateRange range = resolveDateRange(safeDto);
        if (userIds != null && userIds.isEmpty()) {
            return new Page<>(safeDto.getPageNum(), safeDto.getPageSize(), 0);
        }
        LambdaQueryWrapper<AttRecord> wrapper = new LambdaQueryWrapper<AttRecord>()
                .ge(AttRecord::getRecordDate, range.startDate)
                .le(AttRecord::getRecordDate, range.endDate)
                .orderByDesc(AttRecord::getRecordDate)
                .orderByDesc(AttRecord::getPunchInTime);
        if (userIds != null) {
            wrapper.in(AttRecord::getUserId, userIds);
        }

        // if status filter is set, fetch more records to ensure enough after filtering
        boolean hasStatusFilter = StringUtils.hasText(safeDto.getStatus());
        Page<AttRecord> page = attRecordMapper.selectPage(
                new Page<>(hasStatusFilter ? 1 : safeDto.getPageNum(),
                           hasStatusFilter ? 2000 : safeDto.getPageSize()),
                wrapper);
        List<AttendanceRecordVO> allVos = buildRecordViews(page.getRecords());

        if (hasStatusFilter) {
            allVos = filterByStatus(allVos, safeDto.getStatus());
            // in-memory pagination
            int fromIndex = (safeDto.getPageNum() - 1) * safeDto.getPageSize();
            int toIndex = Math.min(fromIndex + safeDto.getPageSize(), allVos.size());
            if (fromIndex >= allVos.size()) {
                allVos = Collections.emptyList();
            } else {
                allVos = allVos.subList(fromIndex, toIndex);
            }
            Page<AttendanceRecordVO> result = new Page<>(safeDto.getPageNum(), safeDto.getPageSize(), page.getTotal());
            result.setRecords(allVos);
            return result;
        }

        Page<AttendanceRecordVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(allVos);
        return result;
    }

    private List<AttendanceRecordVO> filterByStatus(List<AttendanceRecordVO> vos, String status) {
        if (vos == null || vos.isEmpty() || !StringUtils.hasText(status)) {
            return vos;
        }
        return vos.stream()
                .filter(vo -> matchesStatus(vo.getStatusLabel(), status))
                .toList();
    }

    private boolean matchesStatus(String statusLabel, String filterValue) {
        if (statusLabel == null) return false;
        return switch (filterValue) {
            case "normal" -> "正常".equals(statusLabel);
            case "late" -> "迟到".equals(statusLabel);
            case "early" -> "早退".equals(statusLabel);
            case "late_early" -> "迟到/早退".equals(statusLabel);
            case "missing" -> "缺卡".equals(statusLabel);
            case "leave" -> "请假".equals(statusLabel);
            case "field" -> "外勤".equals(statusLabel);
            default -> true;
        };
    }

    private List<AttendanceRecordVO> buildRecordViews(List<AttRecord> records) {
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> userIds = records.stream().map(AttRecord::getUserId).distinct().toList();
        Map<Long, com.oa.common.remote.UserInfo> userMap = userServiceClient.mapUsers(userIds);
        Map<Long, com.oa.common.remote.DeptInfo> deptMap = userServiceClient.mapDepts();

        // batch-fetch schedules for overtime info
        Map<String, java.math.BigDecimal> overtimeMap = new java.util.HashMap<>();
        if (!records.isEmpty()) {
            List<LocalDate> dates = records.stream().map(AttRecord::getRecordDate).distinct().toList();
            List<AttSchedule> schedules = attScheduleMapper.selectList(new LambdaQueryWrapper<AttSchedule>()
                    .in(AttSchedule::getUserId, userIds)
                    .in(AttSchedule::getScheduleDate, dates));
            for (AttSchedule s : schedules) {
                if (s.getOvertimeHours() != null && s.getOvertimeHours().compareTo(java.math.BigDecimal.ZERO) > 0) {
                    overtimeMap.put(s.getUserId() + "_" + s.getScheduleDate(), s.getOvertimeHours());
                }
            }
        }

        return records.stream()
                .map(record -> {
                    com.oa.common.remote.UserInfo user = userMap.get(record.getUserId());
                    com.oa.common.remote.DeptInfo dept = user == null ? null : deptMap.get(user.getDeptId());
                    AttShift shift = findShiftForUser(record.getUserId());
                    java.math.BigDecimal overtime = overtimeMap.get(record.getUserId() + "_" + record.getRecordDate());
                    return buildRecordVO(record, user, dept, shift, overtime);
                })
                .toList();
    }

    private AttRecord selectDailyRecord(Long userId, LocalDate recordDate) {
        return attRecordMapper.selectOne(new LambdaQueryWrapper<AttRecord>()
                .eq(AttRecord::getUserId, userId)
                .eq(AttRecord::getRecordDate, recordDate)
                .last("limit 1"));
    }

    private AttShift resolveShiftForUser(Long userId) {
        // 优先使用每日排班表
        LocalDate today = LocalDate.now();
        AttSchedule schedule = attScheduleMapper.selectOne(new LambdaQueryWrapper<AttSchedule>()
                .eq(AttSchedule::getUserId, userId)
                .eq(AttSchedule::getScheduleDate, today)
                .last("limit 1"));
        if (schedule != null && !Integer.valueOf(2).equals(schedule.getStatus())) {
            AttShift shift = attShiftMapper.selectById(schedule.getShiftId());
            if (shift != null && !Integer.valueOf(0).equals(shift.getStatus())) {
                return shift;
            }
        }
        // fallback 到固定班次
        UserShift mapping = userShiftMapper.selectOne(new LambdaQueryWrapper<UserShift>()
                .eq(UserShift::getUserId, userId)
                .last("limit 1"));
        if (mapping == null) {
            throw new BusinessException(ResultCode.SHIFT_NOT_ASSIGNED);
        }
        AttShift shift = attShiftMapper.selectById(mapping.getShiftId());
        if (shift == null || Integer.valueOf(0).equals(shift.getStatus())) {
            throw new BusinessException(ResultCode.SHIFT_NOT_ASSIGNED);
        }
        return shift;
    }

    private AttShift resolveShiftForUser(Long userId, LocalDate date) {
        AttSchedule schedule = attScheduleMapper.selectOne(new LambdaQueryWrapper<AttSchedule>()
                .eq(AttSchedule::getUserId, userId)
                .eq(AttSchedule::getScheduleDate, date)
                .last("limit 1"));
        if (schedule != null && !Integer.valueOf(2).equals(schedule.getStatus())) {
            AttShift shift = attShiftMapper.selectById(schedule.getShiftId());
            if (shift != null && !Integer.valueOf(0).equals(shift.getStatus())) {
                return shift;
            }
        }
        UserShift mapping = userShiftMapper.selectOne(new LambdaQueryWrapper<UserShift>()
                .eq(UserShift::getUserId, userId)
                .last("limit 1"));
        if (mapping == null) {
            return null;
        }
        AttShift shift = attShiftMapper.selectById(mapping.getShiftId());
        if (shift == null || Integer.valueOf(0).equals(shift.getStatus())) {
            return null;
        }
        return shift;
    }

    private AttShift findShiftForUser(Long userId) {
        UserShift mapping = userShiftMapper.selectOne(new LambdaQueryWrapper<UserShift>()
                .eq(UserShift::getUserId, userId)
                .last("limit 1"));
        if (mapping == null) {
            return null;
        }
        return attShiftMapper.selectById(mapping.getShiftId());
    }

    private PunchVO buildPunchVO(String message, AttRecord record, AttShift shift, LocalDateTime punchTime, boolean completed) {
        PunchVO vo = new PunchVO();
        vo.setMessage(message);
        vo.setUserId(record.getUserId());
        vo.setRecordDate(record.getRecordDate());
        vo.setPunchTime(punchTime);
        vo.setShiftId(shift.getId());
        vo.setShiftName(shift.getShiftName());
        vo.setLateMinutes(calcLateMinutes(record, shift));
        vo.setEarlyMinutes(calcEarlyMinutes(record, shift));
        vo.setStatusLabel(completed ? evaluateStatus(record, shift)
                : vo.getLateMinutes() > 0 ? "迟到" : "正常");
        return vo;
    }

    private AttendanceRecordVO buildRecordVO(AttRecord record,
                                             com.oa.common.remote.UserInfo user,
                                             com.oa.common.remote.DeptInfo dept,
                                             AttShift shift) {
        return buildRecordVO(record, user, dept, shift, null);
    }

    private AttendanceRecordVO buildRecordVO(AttRecord record,
                                             com.oa.common.remote.UserInfo user,
                                             com.oa.common.remote.DeptInfo dept,
                                             AttShift shift,
                                             java.math.BigDecimal overtimeHours) {
        AttendanceRecordVO vo = new AttendanceRecordVO();
        vo.setId(record.getId());
        vo.setUserId(record.getUserId());
        vo.setRealName(user == null ? "未知用户" : user.getRealName());
        vo.setDeptId(user == null ? null : user.getDeptId());
        vo.setDeptName(dept == null ? "未知部门" : dept.getDeptName());
        vo.setShiftId(shift == null ? null : shift.getId());
        vo.setShiftName(shift == null ? null : shift.getShiftName());
        vo.setRecordDate(record.getRecordDate());
        vo.setPunchInTime(record.getPunchInTime());
        vo.setPunchOutTime(record.getPunchOutTime());
        vo.setLateMinutes(calcLateMinutes(record, shift));
        vo.setEarlyMinutes(calcEarlyMinutes(record, shift, overtimeHours));
        vo.setWorkHours(calcWorkHours(record));
        vo.setPunchType(record.getPunchType());
        vo.setDeviceInfo(record.getDeviceInfo());
        vo.setLocation(record.getLocation());
        vo.setStatusLabel(evaluateStatus(record, shift, overtimeHours));
        return vo;
    }

    private Integer calcLateMinutes(AttRecord record, AttShift shift) {
        if (record == null || shift == null || record.getPunchInTime() == null) {
            return 0;
        }
        LocalDateTime threshold = record.getRecordDate().atTime(
                shift.getFlexEnd() == null ? shift.getStartTime() : shift.getFlexEnd());
        if (!record.getPunchInTime().isAfter(threshold)) {
            return 0;
        }
        return Math.toIntExact(Duration.between(threshold, record.getPunchInTime()).toMinutes());
    }

    private Integer calcEarlyMinutes(AttRecord record, AttShift shift) {
        return calcEarlyMinutes(record, shift, null);
    }

    private Integer calcEarlyMinutes(AttRecord record, AttShift shift, java.math.BigDecimal overtimeHours) {
        if (record == null || shift == null || record.getPunchOutTime() == null) {
            return 0;
        }
        java.time.LocalTime endTime = shift.getEndTime();
        if (overtimeHours != null && overtimeHours.compareTo(java.math.BigDecimal.ZERO) > 0) {
            endTime = endTime.plusMinutes(overtimeHours.multiply(java.math.BigDecimal.valueOf(60)).longValue());
        }
        LocalDateTime threshold = record.getRecordDate().atTime(endTime);
        if (!record.getPunchOutTime().isBefore(threshold)) {
            return 0;
        }
        return Math.toIntExact(Duration.between(record.getPunchOutTime(), threshold).toMinutes());
    }

    private BigDecimal calcWorkHours(AttRecord record) {
        if (record.getPunchInTime() == null || record.getPunchOutTime() == null
                || !record.getPunchOutTime().isAfter(record.getPunchInTime())) {
            return BigDecimal.ZERO;
        }
        long minutes = Duration.between(record.getPunchInTime(), record.getPunchOutTime()).toMinutes();
        return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 1, RoundingMode.HALF_UP);
    }

    private String evaluateStatus(AttRecord record, AttShift shift) {
        return evaluateStatus(record, shift, null);
    }

    private String evaluateStatus(AttRecord record, AttShift shift, java.math.BigDecimal overtimeHours) {
        if (Integer.valueOf(3).equals(record.getPunchType())) {
            return "请假";
        }
        if (Integer.valueOf(2).equals(record.getPunchType())) {
            return "外勤";
        }
        if (shift == null) {
            return "班次未分配";
        }
        if (record.getPunchInTime() == null || record.getPunchOutTime() == null) {
            return "缺卡";
        }
        int lateMinutes = calcLateMinutes(record, shift);
        int earlyMinutes = calcEarlyMinutes(record, shift, overtimeHours);
        if (lateMinutes > 0 && earlyMinutes > 0) {
            return "迟到/早退";
        }
        if (lateMinutes > 0) {
            return "迟到";
        }
        if (earlyMinutes > 0) {
            return "早退";
        }
        return "正常";
    }

    private DateRange resolveDateRange(AttendanceRecordQueryDTO dto) {
        if (dto.getDate() != null) {
            return new DateRange(dto.getDate(), dto.getDate());
        }
        if (StringUtils.hasText(dto.getMonth())) {
            try {
                YearMonth yearMonth = YearMonth.parse(dto.getMonth());
                return new DateRange(yearMonth.atDay(1), yearMonth.atEndOfMonth());
            } catch (DateTimeParseException ex) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "month格式应为yyyy-MM");
            }
        }
        YearMonth currentMonth = YearMonth.now();
        return new DateRange(currentMonth.atDay(1), currentMonth.atEndOfMonth());
    }

    private void validateShiftTime(ShiftDTO dto) {
        if (!dto.getStartTime().isBefore(dto.getEndTime())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "上班时间必须早于下班时间");
        }
        if (dto.getFlexStart() != null && dto.getFlexEnd() != null && dto.getFlexStart().isAfter(dto.getFlexEnd())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "弹性开始时间不能晚于弹性结束时间");
        }
    }

    private Integer resolvePunchType(PunchDTO dto) {
        return dto.getPunchType() == null ? 1 : dto.getPunchType();
    }

    private AttShift getShiftEntity(Long id) {
        AttShift shift = attShiftMapper.selectById(id);
        if (shift == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "班次不存在");
        }
        return shift;
    }

    private void requireAdminOrHr() {
        CurrentUser currentUser = UserContextHolder.getCurrentUser();
        if (!currentUser.hasRole("ROLE_ADMIN") && !currentUser.hasRole("ROLE_HR")) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }

    private void requireAdminHrOrLeader() {
        CurrentUser currentUser = UserContextHolder.getCurrentUser();
        if (!currentUser.hasRole("ROLE_ADMIN")
                && !currentUser.hasRole("ROLE_HR")
                && !currentUser.hasRole("ROLE_LEADER")) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }

    private void fillShift(AttShift shift, ShiftDTO dto) {
        shift.setShiftName(dto.getShiftName());
        shift.setStartTime(dto.getStartTime());
        shift.setEndTime(dto.getEndTime());
        shift.setFlexStart(dto.getFlexStart());
        shift.setFlexEnd(dto.getFlexEnd());
        shift.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
    }

    private ShiftVO toShiftVO(AttShift shift) {
        ShiftVO vo = new ShiftVO();
        vo.setId(shift.getId());
        vo.setShiftName(shift.getShiftName());
        vo.setStartTime(shift.getStartTime());
        vo.setEndTime(shift.getEndTime());
        vo.setFlexStart(shift.getFlexStart());
        vo.setFlexEnd(shift.getFlexEnd());
        vo.setStatus(shift.getStatus());
        vo.setCreateTime(shift.getCreateTime());
        vo.setUpdateTime(shift.getUpdateTime());
        return vo;
    }

    @Override
    public List<ScheduleVO> mySchedules(LocalDate startDate, LocalDate endDate) {
        CurrentUser currentUser = UserContextHolder.getCurrentUser();
        return buildSchedulesWithFallback(currentUser.getUserId(), startDate, endDate);
    }

    @Override
    public List<ScheduleVO> deptSchedules(Long deptId, LocalDate startDate, LocalDate endDate) {
        requireAdminHrOrLeader();
        List<Long> deptIds = userServiceClient.collectDeptAndChildren(deptId);
        List<Long> userIds = deptIds.stream()
                .flatMap(id -> userServiceClient.listUsersByDept(id).stream())
                .map(com.oa.common.remote.UserInfo::getId)
                .distinct()
                .toList();
        if (userIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<ScheduleVO> result = new java.util.ArrayList<>();
        for (Long userId : userIds) {
            result.addAll(buildSchedulesWithFallback(userId, startDate, endDate));
        }
        return result;
    }

    @Override
    public List<ScheduleVO> userSchedules(Long userId, LocalDate startDate, LocalDate endDate) {
        requireAdminHrOrLeader();
        return buildSchedulesWithFallback(userId, startDate, endDate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSchedule(List<ScheduleItem> items) {
        requireAdminOrHr();
        for (ScheduleItem item : items) {
            userServiceClient.requireActiveUser(item.getUserId());
            AttSchedule existing = attScheduleMapper.selectOne(new LambdaQueryWrapper<AttSchedule>()
                    .eq(AttSchedule::getUserId, item.getUserId())
                    .eq(AttSchedule::getScheduleDate, item.getScheduleDate())
                    .last("limit 1"));
            if (existing != null) {
                existing.setShiftId(item.getShiftId());
                existing.setStatus(1);
                attScheduleMapper.updateById(existing);
            } else {
                AttSchedule schedule = new AttSchedule();
                schedule.setUserId(item.getUserId());
                schedule.setScheduleDate(item.getScheduleDate());
                schedule.setShiftId(item.getShiftId());
                schedule.setStatus(1);
                schedule.setCreateTime(LocalDateTime.now());
                attScheduleMapper.insert(schedule);
            }
        }
    }

    @Override
    public List<UserShiftVO> allUserShifts() {
        requireAdminHrOrLeader();
        List<com.oa.common.remote.UserInfo> allUsers = userServiceClient.listAllActiveUsers();
        Map<Long, AttShift> shiftMap = attShiftMapper.selectList(new LambdaQueryWrapper<AttShift>()
                        .eq(AttShift::getStatus, 1))
                .stream().collect(Collectors.toMap(AttShift::getId, s -> s, (a, b) -> a));
        Map<Long, UserShift> existingMap = userShiftMapper.selectList(new LambdaQueryWrapper<>())
                .stream().collect(Collectors.toMap(UserShift::getUserId, u -> u, (a, b) -> a));
        Map<Long, com.oa.common.remote.DeptInfo> deptMap = userServiceClient.mapDepts();

        return allUsers.stream().map(user -> {
            UserShiftVO vo = new UserShiftVO();
            vo.setUserId(user.getId());
            vo.setUserName(user.getRealName() != null ? user.getRealName() : user.getUsername());
            com.oa.common.remote.DeptInfo dept = user.getDeptId() != null ? deptMap.get(user.getDeptId()) : null;
            vo.setDeptName(dept != null ? dept.getDeptName() : "-");

            UserShift existing = existingMap.get(user.getId());
            if (existing != null) {
                vo.setShiftId(existing.getShiftId());
                AttShift shift = shiftMap.get(existing.getShiftId());
                vo.setShiftName(shift != null ? shift.getShiftName() : "-");
                vo.setStartTime(shift != null ? shift.getStartTime() : null);
                vo.setEndTime(shift != null ? shift.getEndTime() : null);
            } else {
                vo.setShiftId(null);
                vo.setShiftName("未分配");
                vo.setStartTime(null);
                vo.setEndTime(null);
            }
            return vo;
        }).toList();
    }

    private List<ScheduleVO> buildSchedulesWithFallback(Long userId, LocalDate startDate, LocalDate endDate) {
        List<AttSchedule> schedules = attScheduleMapper.selectList(new LambdaQueryWrapper<AttSchedule>()
                .eq(AttSchedule::getUserId, userId)
                .ge(AttSchedule::getScheduleDate, startDate)
                .le(AttSchedule::getScheduleDate, endDate));
        Map<LocalDate, AttSchedule> scheduleMap = schedules.stream()
                .collect(Collectors.toMap(AttSchedule::getScheduleDate, s -> s, (a, b) -> a));

        com.oa.common.remote.UserInfo user = userServiceClient.getUser(userId);
        String userName = user != null ? (user.getRealName() != null ? user.getRealName() : user.getUsername()) : "未知";
        String deptName = "-";
        if (user != null && user.getDeptId() != null) {
            com.oa.common.remote.DeptInfo dept = userServiceClient.mapDepts().get(user.getDeptId());
            deptName = dept != null ? dept.getDeptName() : "-";
        }

        // 查默认班次（用于 fallback）
        UserShift userShift = userShiftMapper.selectOne(new LambdaQueryWrapper<UserShift>()
                .eq(UserShift::getUserId, userId).last("limit 1"));
        AttShift defaultShift = null;
        if (userShift != null) {
            defaultShift = attShiftMapper.selectById(userShift.getShiftId());
        }

        // 查所有涉及的班次
        java.util.Set<Long> shiftIds = schedules.stream().map(AttSchedule::getShiftId).collect(Collectors.toSet());
        if (defaultShift != null) shiftIds.add(defaultShift.getId());
        Map<Long, AttShift> shiftMap = attShiftMapper.selectBatchIds(new java.util.ArrayList<>(shiftIds)).stream()
                .collect(Collectors.toMap(AttShift::getId, s -> s, (a, b) -> a));

        List<ScheduleVO> result = new java.util.ArrayList<>();
        LocalDate date = startDate;
        while (!date.isAfter(endDate)) {
            AttSchedule s = scheduleMap.get(date);
            ScheduleVO vo = new ScheduleVO();
            vo.setScheduleDate(date);
            vo.setUserId(userId);
            vo.setUserName(userName);
            vo.setDeptName(deptName);

            if (s != null) {
                vo.setId(s.getId());
                vo.setShiftId(s.getShiftId());
                AttShift shift = shiftMap.get(s.getShiftId());
                vo.setShiftName(shift != null ? shift.getShiftName() : "未知");
                vo.setStartTime(shift != null ? shift.getStartTime() : null);
                vo.setEndTime(shift != null ? shift.getEndTime() : null);
                vo.setStatus(s.getStatus());
                vo.setStatusText(null);
                vo.setOvertimeHours(s.getOvertimeHours());
            } else if (defaultShift != null) {
                vo.setId(null);
                vo.setShiftId(defaultShift.getId());
                vo.setShiftName(defaultShift.getShiftName());
                vo.setStartTime(defaultShift.getStartTime());
                vo.setEndTime(defaultShift.getEndTime());
                vo.setStatus(1);
                vo.setStatusText("默认");
            } else {
                vo.setId(null);
                vo.setShiftId(null);
                vo.setShiftName("未分配");
                vo.setStartTime(null);
                vo.setEndTime(null);
                vo.setStatus(0);
                vo.setStatusText("未排班");
            }
            result.add(vo);
            date = date.plusDays(1);
        }
        return result;
    }

    private List<ScheduleVO> buildScheduleVOList(List<AttSchedule> schedules) {
        if (schedules == null || schedules.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> userIds = schedules.stream().map(AttSchedule::getUserId).distinct().toList();
        Map<Long, com.oa.common.remote.UserInfo> userMap = userServiceClient.mapUsers(userIds);
        Map<Long, com.oa.common.remote.DeptInfo> deptMap = userServiceClient.mapDepts();
        List<Long> shiftIds = schedules.stream().map(AttSchedule::getShiftId).distinct().toList();
        Map<Long, AttShift> shiftMap = attShiftMapper.selectBatchIds(shiftIds).stream()
                .collect(Collectors.toMap(AttShift::getId, s -> s, (a, b) -> a));
        return schedules.stream().map(s -> {
            ScheduleVO vo = new ScheduleVO();
            vo.setId(s.getId());
            vo.setUserId(s.getUserId());
            com.oa.common.remote.UserInfo user = userMap.get(s.getUserId());
            vo.setUserName(user != null ? (user.getRealName() != null ? user.getRealName() : user.getUsername()) : "未知用户");
            com.oa.common.remote.DeptInfo dept = user != null ? deptMap.get(user.getDeptId()) : null;
            vo.setDeptName(dept != null ? dept.getDeptName() : "未知部门");
            vo.setScheduleDate(s.getScheduleDate());
            vo.setShiftId(s.getShiftId());
            AttShift shift = shiftMap.get(s.getShiftId());
            vo.setShiftName(shift != null ? shift.getShiftName() : "未知班次");
            vo.setStartTime(shift != null ? shift.getStartTime() : null);
            vo.setEndTime(shift != null ? shift.getEndTime() : null);
            vo.setStatus(s.getStatus());
            return vo;
        }).toList();
    }

    @Override
    public List<VisualAttendanceStatsDTO> monthlyVisualStats(String month) {
        YearMonth statMonth = parseMonth(month);
        LocalDate startDate = statMonth.atDay(1);
        LocalDate endDate = statMonth.plusMonths(1).atDay(1);
        List<AttRecord> records = attRecordMapper.selectList(new LambdaQueryWrapper<AttRecord>()
                .ge(AttRecord::getRecordDate, startDate)
                .lt(AttRecord::getRecordDate, endDate));
        if (records.isEmpty()) {
            return List.of();
        }

        Map<Long, com.oa.common.remote.UserInfo> users = userServiceClient.mapUsers(
                records.stream().map(AttRecord::getUserId).distinct().toList());
        Map<Long, AttShift> shifts = new java.util.HashMap<>();
        for (Long userId : records.stream().map(AttRecord::getUserId).distinct().toList()) {
            shifts.put(userId, findShiftForUser(userId));
        }
        Map<Long, VisualAttendanceAccumulator> stats = new java.util.TreeMap<>();
        for (AttRecord record : records) {
            com.oa.common.remote.UserInfo user = users.get(record.getUserId());
            if (user == null || user.getDeptId() == null) {
                continue;
            }
            VisualAttendanceAccumulator item = stats.computeIfAbsent(user.getDeptId(), ignored -> new VisualAttendanceAccumulator());
            AttShift shift = shifts.get(record.getUserId());
            boolean completed = record.getPunchInTime() != null && record.getPunchOutTime() != null;
            boolean late = isLate(record, shift);
            boolean early = !late && isEarly(record, shift);
            if (completed && !late && !early) {
                item.normalCount++;
            } else if (late) {
                item.lateCount++;
            } else if (early) {
                item.earlyCount++;
            }
            item.overtimeHours = item.overtimeHours.add(calculateOvertimeHours(record, shift));
        }
        return stats.entrySet().stream()
                .map(entry -> new VisualAttendanceStatsDTO(entry.getKey(), entry.getValue().normalCount,
                        entry.getValue().lateCount, entry.getValue().earlyCount, 0, 0,
                        entry.getValue().overtimeHours.setScale(1, RoundingMode.HALF_UP)))
                .toList();
    }

    @Override
    public VisualTodayAttendanceDTO todayVisualStats() {
        LocalDate today = LocalDate.now();
        long punchInCount = attRecordMapper.selectCount(new LambdaQueryWrapper<AttRecord>()
                .eq(AttRecord::getRecordDate, today)
                .isNotNull(AttRecord::getPunchInTime));
        long punchOutCount = attRecordMapper.selectCount(new LambdaQueryWrapper<AttRecord>()
                .eq(AttRecord::getRecordDate, today)
                .isNotNull(AttRecord::getPunchOutTime));
        return new VisualTodayAttendanceDTO(today, punchInCount, punchOutCount);
    }

    private YearMonth parseMonth(String month) {
        if (!StringUtils.hasText(month)) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(month.trim());
        } catch (DateTimeParseException ex) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "统计月份格式应为 yyyy-MM");
        }
    }

    private boolean isLate(AttRecord record, AttShift shift) {
        if (record.getPunchInTime() == null || shift == null || shift.getStartTime() == null) {
            return false;
        }
        LocalTime deadline = shift.getFlexEnd() == null ? shift.getStartTime() : shift.getFlexEnd();
        return record.getPunchInTime().toLocalTime().isAfter(deadline);
    }

    private boolean isEarly(AttRecord record, AttShift shift) {
        return record.getPunchOutTime() != null
                && shift != null
                && shift.getEndTime() != null
                && record.getPunchOutTime().toLocalTime().isBefore(shift.getEndTime());
    }

    private BigDecimal calculateOvertimeHours(AttRecord record, AttShift shift) {
        if (record.getPunchOutTime() == null || shift == null || shift.getEndTime() == null) {
            return BigDecimal.ZERO;
        }
        LocalDateTime standardEnd = LocalDateTime.of(record.getRecordDate(), shift.getEndTime());
        long minutes = Duration.between(standardEnd, record.getPunchOutTime()).toMinutes();
        return minutes <= 0 ? BigDecimal.ZERO : BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 1, RoundingMode.HALF_UP);
    }

    private static class VisualAttendanceAccumulator {
        private int normalCount;
        private int lateCount;
        private int earlyCount;
        private BigDecimal overtimeHours = BigDecimal.ZERO;
    }

    private static class DateRange {
        private final LocalDate startDate;
        private final LocalDate endDate;

        private DateRange(LocalDate startDate, LocalDate endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }
}
