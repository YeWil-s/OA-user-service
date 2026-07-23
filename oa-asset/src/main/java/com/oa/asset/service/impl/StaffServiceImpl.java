package com.oa.asset.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.asset.dto.EmployeeArchiveDTO;
import com.oa.asset.dto.StaffChangeDTO;
import com.oa.asset.entity.EmployeeArchive;
import com.oa.asset.entity.StaffChange;
import com.oa.asset.mapper.EmployeeArchiveMapper;
import com.oa.asset.mapper.StaffChangeMapper;
import com.oa.asset.client.UserDirectoryClient;
import com.oa.asset.service.StaffService;
import com.oa.asset.service.UserDirectoryService;
import com.oa.asset.vo.StaffChangeVO;
import com.oa.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
/**
 * 员工档案、合同和人事变动业务实现。
 */
public class StaffServiceImpl implements StaffService {
    private static final Logger log = LoggerFactory.getLogger(StaffServiceImpl.class);
    private final EmployeeArchiveMapper archiveMapper;
    private final StaffChangeMapper changeMapper;
    private final UserDirectoryService userDirectoryService;
    private final UserDirectoryClient userDirectoryClient;
    private final com.oa.asset.client.NoticeServiceClient noticeServiceClient;

    private static final int MSG_TYPE_SYSTEM = 3;

    public StaffServiceImpl(EmployeeArchiveMapper archiveMapper, StaffChangeMapper changeMapper,
                            UserDirectoryService userDirectoryService,
                            UserDirectoryClient userDirectoryClient,
                            com.oa.asset.client.NoticeServiceClient noticeServiceClient) {
        this.archiveMapper = archiveMapper; this.changeMapper = changeMapper;
        this.userDirectoryService = userDirectoryService;
        this.userDirectoryClient = userDirectoryClient;
        this.noticeServiceClient = noticeServiceClient;
    }

    @Override
    public EmployeeArchive getArchive(Long userId) {
        userDirectoryService.requireEmployee(userId);
        EmployeeArchive archive = archiveMapper.selectOne(new LambdaQueryWrapper<EmployeeArchive>().eq(EmployeeArchive::getUserId, userId));
        if (archive == null) throw new BusinessException(60006, "员工档案不存在");
        return archive;
    }

    @Override
    @Transactional
    public void saveArchive(Long userId, EmployeeArchiveDTO dto) {
        userDirectoryService.requireEmployee(userId);
        if (dto.getEducation() != null && (dto.getEducation() < 1 || dto.getEducation() > 5)) throw new BusinessException(400, "学历仅支持1-5");
        if (dto.getContractStart() != null && dto.getContractEnd() != null && dto.getContractEnd().isBefore(dto.getContractStart())) {
            throw new BusinessException(400, "合同结束日期不能早于开始日期");
        }
        EmployeeArchive archive = archiveMapper.selectOne(new LambdaQueryWrapper<EmployeeArchive>().eq(EmployeeArchive::getUserId, userId));
        // 同一员工只允许一份档案：不存在时新增，已存在时原记录更新（upsert）。
        boolean insert = archive == null;
        if (insert) { archive = new EmployeeArchive(); archive.setUserId(userId); archive.setCreateTime(LocalDateTime.now()); }
        archive.setIdCard(dto.getIdCard()); archive.setEducation(dto.getEducation()); archive.setMajor(dto.getMajor());
        archive.setGraduateSchool(dto.getGraduateSchool()); archive.setAddress(dto.getAddress());
        archive.setEmergencyContact(dto.getEmergencyContact()); archive.setEmergencyPhone(dto.getEmergencyPhone());
        archive.setContractStart(dto.getContractStart()); archive.setContractEnd(dto.getContractEnd()); archive.setUpdateTime(LocalDateTime.now());
        if (insert) archiveMapper.insert(archive); else archiveMapper.updateById(archive);
    }

    @Override
    public IPage<EmployeeArchive> contracts(int pageNum, int pageSize, Long userId) {
        LambdaQueryWrapper<EmployeeArchive> q = new LambdaQueryWrapper<EmployeeArchive>().isNotNull(EmployeeArchive::getContractEnd);
        if (userId != null) q.eq(EmployeeArchive::getUserId, userId);
        return archiveMapper.selectPage(new Page<>(pageNum, pageSize), q.orderByAsc(EmployeeArchive::getContractEnd));
    }

    @Override
    public IPage<EmployeeArchive> expiringContracts(int pageNum, int pageSize, int days) {
        if (days < 0 || days > 3650) throw new BusinessException(400, "预警天数必须在0-3650之间");
        LocalDate today = LocalDate.now();
        // 预警范围包含今天和截止日；已经过期的合同不属于"即将到期"。
        return archiveMapper.selectPage(new Page<>(pageNum, pageSize), new LambdaQueryWrapper<EmployeeArchive>()
                .between(EmployeeArchive::getContractEnd, today, today.plusDays(days)).orderByAsc(EmployeeArchive::getContractEnd));
    }

    @Override
    public IPage<StaffChangeVO> changes(int pageNum, int pageSize, Long userId, Integer changeType) {
        LambdaQueryWrapper<StaffChange> q = new LambdaQueryWrapper<>();
        if (userId != null) q.eq(StaffChange::getUserId, userId);
        if (changeType != null) q.eq(StaffChange::getChangeType, changeType);
        Page<StaffChange> page = changeMapper.selectPage(
                new Page<>(pageNum, pageSize),
                q.orderByDesc(StaffChange::getChangeDate).orderByDesc(StaffChange::getId));
        List<StaffChangeVO> vos = enrichNames(page.getRecords());
        Page<StaffChangeVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(vos);
        return result;
    }

    private List<StaffChangeVO> enrichNames(List<StaffChange> records) {
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, UserDirectoryClient.EmployeeRef> employeeMap = buildEmployeeMap(records);
        Map<Long, String> deptNameMap = buildDeptNameMap();
        Map<Long, String> positionNameMap = buildPositionNameMap();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return records.stream().map(r -> {
            StaffChangeVO vo = new StaffChangeVO();
            vo.setId(r.getId());
            vo.setUserId(r.getUserId());
            vo.setChangeType(r.getChangeType());
            vo.setChangeDate(r.getChangeDate() != null ? r.getChangeDate().format(dateFormatter) : null);
            vo.setFromDeptId(r.getBeforeDept());
            vo.setFromDeptName(deptNameMap.get(r.getBeforeDept()));
            vo.setToDeptId(r.getAfterDept());
            vo.setToDeptName(deptNameMap.get(r.getAfterDept()));
            vo.setFromPositionId(r.getBeforePosition());
            vo.setFromPositionName(positionNameMap.get(r.getBeforePosition()));
            vo.setToPositionId(r.getAfterPosition());
            vo.setToPositionName(positionNameMap.get(r.getAfterPosition()));
            vo.setRemark(r.getRemark());
            vo.setCreateTime(r.getCreateTime() != null ? r.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
            UserDirectoryClient.EmployeeRef emp = employeeMap.get(r.getUserId());
            if (emp != null) {
                vo.setRealName(emp.getRealName());
                vo.setUserName(emp.getUsername());
            }
            return vo;
        }).collect(Collectors.toList());
    }

    private Map<Long, UserDirectoryClient.EmployeeRef> buildEmployeeMap(List<StaffChange> records) {
        Set<Long> userIds = records.stream().map(StaffChange::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, UserDirectoryClient.EmployeeRef> map = new HashMap<>();
        for (Long uid : userIds) {
            try {
                UserDirectoryClient.EmployeeRef emp = userDirectoryService.requireEmployee(uid);
                map.put(uid, emp);
            } catch (Exception e) {
                log.warn("获取员工信息失败 userId={}: {}", uid, e.getMessage());
            }
        }
        return map;
    }

    private Map<Long, String> buildDeptNameMap() {
        Map<Long, String> map = new HashMap<>();
        try {
            com.oa.common.result.Result<List<UserDirectoryClient.DeptRef>> result = userDirectoryClient.departments();
            if (result != null && result.getCode() == 200 && result.getData() != null) {
                flattenDeptNames(result.getData(), map);
            }
        } catch (Exception e) {
            log.warn("获取部门列表失败: {}", e.getMessage());
        }
        return map;
    }

    private void flattenDeptNames(List<UserDirectoryClient.DeptRef> nodes, Map<Long, String> map) {
        if (nodes == null) return;
        for (UserDirectoryClient.DeptRef node : nodes) {
            if (node.getId() != null) {
                map.put(node.getId(), node.getDeptName());
            }
            if (node.getChildren() != null) {
                flattenDeptNames(node.getChildren(), map);
            }
        }
    }

    private Map<Long, String> buildPositionNameMap() {
        Map<Long, String> map = new HashMap<>();
        try {
            com.oa.common.result.Result<UserDirectoryClient.PageRef<UserDirectoryClient.PositionRef>> result =
                    userDirectoryClient.positions(1, 100, null);
            if (result != null && result.getCode() == 200 && result.getData() != null
                    && result.getData().getRecords() != null) {
                for (UserDirectoryClient.PositionRef p : result.getData().getRecords()) {
                    if (p.getId() != null) {
                        map.put(p.getId(), p.getPositionName());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("获取岗位列表失败: {}", e.getMessage());
        }
        return map;
    }

        @Override
    public void createChange(StaffChangeDTO dto) {
        StaffChange c = new StaffChange();
        copyValidated(dto, c);
        c.setCreateTime(LocalDateTime.now());
        changeMapper.insert(c);
        if (dto.getUserId() != null) {
            noticeServiceClient.sendMessage(dto.getUserId(),
                    "人事变动通知",
                    "您有一条新的人事变动记录，请及时查看。",
                    MSG_TYPE_SYSTEM,
                    c.getId());
        }
    }
    @Override public void updateChange(Long id, StaffChangeDTO dto) { StaffChange c = requireChange(id); copyValidated(dto, c); changeMapper.updateById(c); }
    @Override public void deleteChange(Long id) { requireChange(id); changeMapper.deleteById(id); }
    private StaffChange requireChange(Long id) { StaffChange c = changeMapper.selectById(id); if (c == null) throw new BusinessException(60007, "人事变动记录不存在"); return c; }
    private void copyValidated(StaffChangeDTO d, StaffChange c) {
        UserDirectoryClient.EmployeeRef employee = userDirectoryService.requireEmployee(d.getUserId());
        Long currentDept = employee.getDeptId();
        Long currentPosition = employee.getPositionId();
        c.setUserId(d.getUserId()); c.setChangeType(d.getChangeType());
        c.setChangeDate(d.getChangeDate()); c.setRemark(d.getRemark());

        if (d.getChangeType() == 1) {
            userDirectoryService.requireDepartmentAndPosition(d.getAfterDept(), d.getAfterPosition());
            c.setBeforeDept(null); c.setBeforePosition(null);
            c.setAfterDept(d.getAfterDept()); c.setAfterPosition(d.getAfterPosition());
            syncUserDeptAndPosition(d.getUserId(), d.getAfterDept(), d.getAfterPosition());
        } else if (d.getChangeType() == 3) {
            c.setBeforeDept(currentDept); c.setBeforePosition(currentPosition);
            c.setAfterDept(d.getAfterDept()); c.setAfterPosition(d.getAfterPosition());
        } else if (d.getChangeType() == 4) {
            c.setBeforeDept(currentDept); c.setBeforePosition(currentPosition);
            c.setAfterDept(null); c.setAfterPosition(null);
        } else {
            userDirectoryService.requireDepartmentAndPosition(currentDept, currentPosition);
            c.setBeforeDept(currentDept); c.setBeforePosition(currentPosition);
            c.setAfterDept(currentDept); c.setAfterPosition(currentPosition);
        }
    }

    private void syncUserDeptAndPosition(Long userId, Long deptId, Long positionId) {
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("deptId", deptId);
        body.put("positionId", positionId);
        userDirectoryClient.updateDeptPosition(userId, body);
    }
}
