package com.oa.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.common.exception.BusinessException;
import com.oa.common.result.ResultCode;
import com.oa.user.dto.PositionDTO;
import com.oa.user.entity.SysPosition;
import com.oa.user.entity.SysPositionRole;
import com.oa.user.mapper.SysPositionMapper;
import com.oa.user.mapper.SysPositionRoleMapper;
import com.oa.user.service.ISysPositionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class SysPositionServiceImpl extends ServiceImpl<SysPositionMapper, SysPosition> implements ISysPositionService {

    private final SysPositionRoleMapper sysPositionRoleMapper;

    public SysPositionServiceImpl(SysPositionRoleMapper sysPositionRoleMapper) {
        this.sysPositionRoleMapper = sysPositionRoleMapper;
    }

    @Override
    public IPage<SysPosition> pagePositions(Integer pageNum, Integer pageSize, Long deptId) {
        LambdaQueryWrapper<SysPosition> wrapper = new LambdaQueryWrapper<>();
        if (deptId != null) wrapper.eq(SysPosition::getDeptId, deptId);
        wrapper.orderByAsc(SysPosition::getSortOrder);
        return this.page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    @Transactional
    public void addPosition(PositionDTO dto) {
        if (this.count(new LambdaQueryWrapper<SysPosition>()
                .eq(SysPosition::getPositionCode, dto.getPositionCode())) > 0) {
            throw new BusinessException(ResultCode.POSITION_CODE_DUPLICATE);
        }
        SysPosition pos = new SysPosition();
        pos.setPositionName(dto.getPositionName());
        pos.setPositionCode(dto.getPositionCode());
        pos.setDeptId(dto.getDeptId());
        pos.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        pos.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        this.save(pos);
        saveRoleBindings(pos.getId(), dto.getRoleIds());
    }

    @Override
    @Transactional
    public void updatePosition(Long id, PositionDTO dto) {
        SysPosition pos = this.getById(id);
        if (pos == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "岗位不存在");
        }
        SysPosition exist = this.getOne(new LambdaQueryWrapper<SysPosition>()
                .eq(SysPosition::getPositionCode, dto.getPositionCode())
                .ne(SysPosition::getId, id));
        if (exist != null) {
            throw new BusinessException(ResultCode.POSITION_CODE_DUPLICATE);
        }
        pos.setPositionName(dto.getPositionName());
        pos.setPositionCode(dto.getPositionCode());
        pos.setDeptId(dto.getDeptId());
        if (dto.getSortOrder() != null) pos.setSortOrder(dto.getSortOrder());
        if (dto.getStatus() != null) pos.setStatus(dto.getStatus());
        this.updateById(pos);
        // replace role bindings
        sysPositionRoleMapper.delete(new LambdaQueryWrapper<SysPositionRole>()
                .eq(SysPositionRole::getPositionId, id));
        saveRoleBindings(id, dto.getRoleIds());
    }

    @Override
    @Transactional
    public void deletePosition(Long id) {
        this.removeById(id);
        sysPositionRoleMapper.delete(new LambdaQueryWrapper<SysPositionRole>()
                .eq(SysPositionRole::getPositionId, id));
    }

    @Override
    public List<Long> getRoleIdsByPositionId(Long positionId) {
        return sysPositionRoleMapper.selectRoleIdsByPositionId(positionId);
    }

    private void saveRoleBindings(Long positionId, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) return;
        for (Long roleId : roleIds) {
            SysPositionRole pr = new SysPositionRole();
            pr.setPositionId(positionId);
            pr.setRoleId(roleId);
            sysPositionRoleMapper.insert(pr);
        }
    }
}
