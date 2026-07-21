package com.oa.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.common.exception.BusinessException;
import com.oa.common.result.ResultCode;
import com.oa.user.dto.DeptDTO;
import com.oa.user.entity.SysDept;
import com.oa.user.entity.SysUser;
import com.oa.user.mapper.SysDeptMapper;
import com.oa.user.mapper.SysUserMapper;
import com.oa.user.service.ISysDeptService;
import com.oa.user.vo.DeptTreeVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements ISysDeptService {

    private final SysUserMapper sysUserMapper;

    public SysDeptServiceImpl(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public List<DeptTreeVO> getDeptTree() {
        List<SysDept> allDepts = this.list(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getStatus, 1).orderByAsc(SysDept::getSortOrder));

        List<DeptTreeVO> vos = allDepts.stream().map(dept -> {
            DeptTreeVO vo = new DeptTreeVO();
            vo.setId(dept.getId());
            vo.setParentId(dept.getParentId());
            vo.setDeptName(dept.getDeptName());
            vo.setDeptCode(dept.getDeptCode());
            vo.setLeaderId(dept.getLeaderId());
            vo.setSortOrder(dept.getSortOrder());
            vo.setStatus(dept.getStatus());
            return vo;
        }).collect(Collectors.toList());

        Map<Long, List<DeptTreeVO>> childrenMap = vos.stream()
                .filter(v -> v.getParentId() != null && v.getParentId() != 0)
                .collect(Collectors.groupingBy(DeptTreeVO::getParentId));

        vos.forEach(v -> v.setChildren(childrenMap.getOrDefault(v.getId(), new ArrayList<>())));

        return vos.stream().filter(v -> v.getParentId() == null || v.getParentId() == 0)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addDept(DeptDTO dto) {
        SysDept dept = new SysDept();
        dept.setDeptName(dto.getDeptName());
        dept.setDeptCode(dto.getDeptCode());
        dept.setParentId(dto.getParentId() != null ? dto.getParentId() : 0L);
        dept.setLeaderId(dto.getLeaderId());
        dept.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        dept.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        this.save(dept);
    }

    @Override
    @Transactional
    public void updateDept(Long id, DeptDTO dto) {
        SysDept dept = this.getById(id);
        if (dept == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "部门不存在");
        }
        dept.setDeptName(dto.getDeptName());
        dept.setDeptCode(dto.getDeptCode());
        dept.setParentId(dto.getParentId() != null ? dto.getParentId() : 0L);
        dept.setLeaderId(dto.getLeaderId());
        dept.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        if (dto.getStatus() != null) dept.setStatus(dto.getStatus());
        this.updateById(dept);
    }

    @Override
    @Transactional
    public void deleteDept(Long id) {
        if (this.count(new LambdaQueryWrapper<SysDept>().eq(SysDept::getParentId, id)) > 0) {
            throw new BusinessException(ResultCode.DEPT_HAS_CHILDREN);
        }
        if (sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getDeptId, id).eq(SysUser::getStatus, 1)) > 0) {
            throw new BusinessException(ResultCode.DEPT_HAS_USERS);
        }
        this.removeById(id);
    }
}
