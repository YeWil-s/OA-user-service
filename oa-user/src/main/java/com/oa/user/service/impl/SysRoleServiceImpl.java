package com.oa.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.common.exception.BusinessException;
import com.oa.common.result.ResultCode;
import com.oa.user.dto.RoleDTO;
import com.oa.user.entity.SysRole;
import com.oa.user.entity.SysRoleMenu;
import com.oa.user.mapper.SysRoleMapper;
import com.oa.user.mapper.SysRoleMenuMapper;
import com.oa.user.service.ISysRoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

    private final SysRoleMenuMapper sysRoleMenuMapper;

    public SysRoleServiceImpl(SysRoleMenuMapper sysRoleMenuMapper) {
        this.sysRoleMenuMapper = sysRoleMenuMapper;
    }

    @Override
    public IPage<SysRole> pageRoles(Integer pageNum, Integer pageSize) {
        return this.page(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SysRole>().orderByAsc(SysRole::getSortOrder));
    }

    @Override
    @Transactional
    public void addRole(RoleDTO dto) {
        if (this.count(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, dto.getRoleCode())) > 0) {
            throw new BusinessException(ResultCode.ROLE_CODE_DUPLICATE);
        }
        SysRole role = new SysRole();
        role.setRoleName(dto.getRoleName());
        role.setRoleCode(dto.getRoleCode());
        role.setRoleDesc(dto.getRoleDesc());
        role.setDataScope(dto.getDataScope() != null ? dto.getDataScope() : 3);
        role.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        role.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        this.save(role);
    }

    @Override
    @Transactional
    public void updateRole(Long id, RoleDTO dto) {
        SysRole role = this.getById(id);
        if (role == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "角色不存在");
        }
        SysRole exist = this.getOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, dto.getRoleCode()).ne(SysRole::getId, id));
        if (exist != null) {
            throw new BusinessException(ResultCode.ROLE_CODE_DUPLICATE);
        }
        role.setRoleName(dto.getRoleName());
        role.setRoleCode(dto.getRoleCode());
        role.setRoleDesc(dto.getRoleDesc());
        if (dto.getDataScope() != null) role.setDataScope(dto.getDataScope());
        if (dto.getSortOrder() != null) role.setSortOrder(dto.getSortOrder());
        if (dto.getStatus() != null) role.setStatus(dto.getStatus());
        this.updateById(role);
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));
        this.removeById(id);
    }

    @Override
    @Transactional
    public void assignMenus(Long roleId, List<Long> menuIds) {
        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        if (menuIds != null && !menuIds.isEmpty()) {
            List<SysRoleMenu> list = menuIds.stream().map(menuId -> {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setRoleId(roleId);
                rm.setMenuId(menuId);
                return rm;
            }).collect(Collectors.toList());
            sysRoleMenuMapper.insert(list);
        }
    }

    @Override
    public List<Long> getRoleMenuIds(Long roleId) {
        return sysRoleMenuMapper.selectList(
                        new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId))
                .stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
    }
}
