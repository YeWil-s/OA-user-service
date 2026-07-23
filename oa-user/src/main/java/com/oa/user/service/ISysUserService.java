package com.oa.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oa.user.dto.EmployeeDTO;
import com.oa.user.dto.ResetPasswordDTO;
import com.oa.user.entity.SysUser;
import com.oa.user.vo.CurrentUserVO;
import com.oa.user.vo.LoginVO;

import java.util.List;

public interface ISysUserService extends IService<SysUser> {

    LoginVO login(String username, String password);

    void logout(String token);

    CurrentUserVO getCurrentUser(String token);

    IPage<SysUser> pageEmployees(Integer pageNum, Integer pageSize, Long deptId, Long positionId, String realName, Integer status);

    SysUser getEmployeeDetail(Long id);

    void addEmployee(EmployeeDTO dto);

    void updateEmployee(Long id, EmployeeDTO dto);

    void deleteEmployee(Long id);

    void resetPassword(Long id);

    void updatePassword(Long userId, ResetPasswordDTO dto);

    List<String> getUserRoles(Long userId);

    List<String> getUserPermissions(Long userId);

    List<SysUser> listByDeptId(Long deptId);

    void updateEmployeeDeptPosition(Long id, Long deptId, Long positionId);
}
