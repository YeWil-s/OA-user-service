package com.oa.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.attendance.client.UserServiceClient;
import com.oa.attendance.dto.AttendanceRecordQueryDTO;
import com.oa.attendance.dto.PunchDTO;
import com.oa.attendance.dto.ShiftDTO;
import com.oa.attendance.dto.UserShiftDTO;
import com.oa.attendance.entity.AttRecord;
import com.oa.attendance.entity.AttShift;
import com.oa.attendance.entity.UserShift;
import com.oa.attendance.mapper.AttRecordMapper;
import com.oa.attendance.mapper.AttShiftMapper;
import com.oa.attendance.mapper.UserShiftMapper;
import com.oa.attendance.service.IAttendanceService;
import com.oa.attendance.vo.AttendanceRecordVO;
import com.oa.attendance.vo.PunchVO;
import com.oa.attendance.vo.ShiftVO;
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
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AttendanceServiceImpl implements IAttendanceService {

    private final AttShiftMapper attShiftMapper;
    private final AttRecordMapper attRecordMapper;
    private final UserShiftMapper userShiftMapper;
    private final UserServiceClient userServiceClient;

    public AttendanceServiceImpl(AttShiftMapper attShiftMapper,
                                 AttRecordMapper attRecordMapper,
                                 UserShiftMapper userShiftMapper,
                                 UserServiceClient userServiceClient) {
        this.attShiftMapper = attShiftMapper;
        this.attRecordMapper = attRecordMapper;
        this.userShiftMapper = userShiftMapper;
        this.userServiceClient = userServiceClient;
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
        return buildPunchVO("上班打卡成功", record, resolveShiftForUser(currentUser.getUserId()), now, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PunchVO punchOut(PunchDTO dto) {
        CurrentUser currentUser = UserContextHolder.getCurrentUser();
        userServiceClient.requireActiveUser(currentUser.getUserId());
        PunchDTO safeDto = dto == null ? new PunchDTO() : dto;
        LocalDate today = LocalDate.now();
        AttRecord record = selectDailyRecord(currentUser.getUserId(), today);
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
        return buildPunchVO("下班打卡成功", record, resolveShiftForUser(currentUser.getUserId()), now, true);
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
        UserServiceClient.UserInfo currentUserInfo = userServiceClient.requireActiveUser(currentUser.getUserId());
        Long deptId = currentUserInfo.getDeptId() == null ? currentUser.getDeptId() : currentUserInfo.getDeptId();
        List<Long> deptIds = userServiceClient.collectDeptAndChildren(deptId);
        List<Long> userIds = deptIds.stream()
                .flatMap(scopedDeptId -> userServiceClient.listUsersByDept(scopedDeptId).stream())
                .map(UserServiceClient.UserInfo::getId)
                .distinct()
                .toList();
        return queryRecords(userIds, dto);
    }

    @Override
    public IPage<AttendanceRecordVO> allRecords(AttendanceRecordQueryDTO dto) {
        requireAdminOrHr();
        return queryRecords(null, dto);
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
        Page<AttRecord> page = attRecordMapper.selectPage(new Page<>(safeDto.getPageNum(), safeDto.getPageSize()), wrapper);
        Page<AttendanceRecordVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(buildRecordViews(page.getRecords()));
        return result;
    }

    private List<AttendanceRecordVO> buildRecordViews(List<AttRecord> records) {
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> userIds = records.stream().map(AttRecord::getUserId).distinct().toList();
        Map<Long, UserServiceClient.UserInfo> userMap = userServiceClient.mapUsers(userIds);
        Map<Long, UserServiceClient.DeptInfo> deptMap = userServiceClient.mapDepts();
        return records.stream()
                .map(record -> {
                    UserServiceClient.UserInfo user = userMap.get(record.getUserId());
                    UserServiceClient.DeptInfo dept = user == null ? null : deptMap.get(user.getDeptId());
                    AttShift shift = findShiftForUser(record.getUserId());
                    return buildRecordVO(record, user, dept, shift);
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
                                             UserServiceClient.UserInfo user,
                                             UserServiceClient.DeptInfo dept,
                                             AttShift shift) {
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
        vo.setEarlyMinutes(calcEarlyMinutes(record, shift));
        vo.setWorkHours(calcWorkHours(record));
        vo.setPunchType(record.getPunchType());
        vo.setDeviceInfo(record.getDeviceInfo());
        vo.setLocation(record.getLocation());
        vo.setStatusLabel(evaluateStatus(record, shift));
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
        if (record == null || shift == null || record.getPunchOutTime() == null) {
            return 0;
        }
        LocalDateTime threshold = record.getRecordDate().atTime(shift.getEndTime());
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
        if (shift == null) {
            return "班次未分配";
        }
        if (record.getPunchInTime() == null || record.getPunchOutTime() == null) {
            return "缺卡";
        }
        int lateMinutes = calcLateMinutes(record, shift);
        int earlyMinutes = calcEarlyMinutes(record, shift);
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

    private static class DateRange {
        private final LocalDate startDate;
        private final LocalDate endDate;

        private DateRange(LocalDate startDate, LocalDate endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }
}
