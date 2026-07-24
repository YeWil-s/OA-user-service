package com.oa.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oa.user.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    @Select("SELECT DISTINCT m.* FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "WHERE rm.role_id IN (" +
            "  SELECT ur.role_id FROM sys_user_role ur WHERE ur.user_id = #{userId} " +
            "  UNION " +
            "  SELECT pr.role_id FROM sys_position_role pr " +
            "  INNER JOIN sys_user u ON pr.position_id = u.position_id " +
            "  WHERE u.id = #{userId}" +
            ") AND m.status = 1 " +
            "ORDER BY m.sort_order ASC")
    List<SysMenu> selectMenusByUserId(@Param("userId") Long userId);
}
