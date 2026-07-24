package com.oa.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oa.user.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("SELECT r.role_code FROM sys_role r WHERE r.id IN (" +
            "  SELECT ur.role_id FROM sys_user_role ur WHERE ur.user_id = #{userId} " +
            "  UNION " +
            "  SELECT pr.role_id FROM sys_position_role pr " +
            "  INNER JOIN sys_user u ON pr.position_id = u.position_id " +
            "  WHERE u.id = #{userId}" +
            ") AND r.status = 1")
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    @Select("SELECT DISTINCT m.permission_code FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "WHERE rm.role_id IN (" +
            "  SELECT ur.role_id FROM sys_user_role ur WHERE ur.user_id = #{userId} " +
            "  UNION " +
            "  SELECT pr.role_id FROM sys_position_role pr " +
            "  INNER JOIN sys_user u ON pr.position_id = u.position_id " +
            "  WHERE u.id = #{userId}" +
            ") AND m.status = 1 AND m.permission_code IS NOT NULL AND m.permission_code != ''")
    List<String> selectPermissionCodesByUserId(@Param("userId") Long userId);

    @Select("SELECT r.role_name FROM sys_role r WHERE r.id IN (" +
            "  SELECT ur.role_id FROM sys_user_role ur WHERE ur.user_id = #{userId} " +
            "  UNION " +
            "  SELECT pr.role_id FROM sys_position_role pr " +
            "  INNER JOIN sys_user u ON pr.position_id = u.position_id " +
            "  WHERE u.id = #{userId}" +
            ") AND r.status = 1")
    List<String> selectRoleNamesByUserId(@Param("userId") Long userId);
}
