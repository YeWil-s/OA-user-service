package com.oa.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oa.user.dto.RoleDTO;
import com.oa.user.entity.SysRole;

import java.util.List;

public interface ISysRoleService extends IService<SysRole> {

    IPage<SysRole> pageRoles(Integer pageNum, Integer pageSize);

    void addRole(RoleDTO dto);

    void updateRole(Long id, RoleDTO dto);

    void deleteRole(Long id);

    void assignMenus(Long roleId, List<Long> menuIds);

    List<Long> getRoleMenuIds(Long roleId);
}
