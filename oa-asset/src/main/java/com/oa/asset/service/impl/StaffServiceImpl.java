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
import com.oa.asset.service.StaffService;
import com.oa.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
/**
 * 员工档案、合同和人事变动业务实现。
 */
public class StaffServiceImpl implements StaffService {
    private final EmployeeArchiveMapper archiveMapper;
    private final StaffChangeMapper changeMapper;
    public StaffServiceImpl(EmployeeArchiveMapper archiveMapper, StaffChangeMapper changeMapper) {
        this.archiveMapper = archiveMapper; this.changeMapper = changeMapper;
    }

    @Override
    public EmployeeArchive getArchive(Long userId) {
        EmployeeArchive archive = archiveMapper.selectOne(new LambdaQueryWrapper<EmployeeArchive>().eq(EmployeeArchive::getUserId, userId));
        if (archive == null) throw new BusinessException(60006, "员工档案不存在");
        return archive;
    }

    @Override
    @Transactional
    public void saveArchive(Long userId, EmployeeArchiveDTO dto) {
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
        // 预警范围包含今天和截止日；已经过期的合同不属于“即将到期”。
        return archiveMapper.selectPage(new Page<>(pageNum, pageSize), new LambdaQueryWrapper<EmployeeArchive>()
                .between(EmployeeArchive::getContractEnd, today, today.plusDays(days)).orderByAsc(EmployeeArchive::getContractEnd));
    }

    @Override
    public IPage<StaffChange> changes(int pageNum, int pageSize, Long userId, Integer changeType) {
        LambdaQueryWrapper<StaffChange> q = new LambdaQueryWrapper<>();
        if (userId != null) q.eq(StaffChange::getUserId, userId);
        if (changeType != null) q.eq(StaffChange::getChangeType, changeType);
        return changeMapper.selectPage(new Page<>(pageNum, pageSize), q.orderByDesc(StaffChange::getChangeDate).orderByDesc(StaffChange::getId));
    }

    @Override public void createChange(StaffChangeDTO dto) { StaffChange c = new StaffChange(); copy(dto, c); c.setCreateTime(LocalDateTime.now()); changeMapper.insert(c); }
    @Override public void updateChange(Long id, StaffChangeDTO dto) { StaffChange c = requireChange(id); copy(dto, c); changeMapper.updateById(c); }
    @Override public void deleteChange(Long id) { requireChange(id); changeMapper.deleteById(id); }
    private StaffChange requireChange(Long id) { StaffChange c = changeMapper.selectById(id); if (c == null) throw new BusinessException(60007, "人事变动记录不存在"); return c; }
    private void copy(StaffChangeDTO d, StaffChange c) {
        c.setUserId(d.getUserId()); c.setChangeType(d.getChangeType()); c.setBeforeDept(d.getBeforeDept()); c.setAfterDept(d.getAfterDept());
        c.setBeforePosition(d.getBeforePosition()); c.setAfterPosition(d.getAfterPosition()); c.setChangeDate(d.getChangeDate()); c.setRemark(d.getRemark());
    }
}
