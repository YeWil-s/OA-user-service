package com.oa.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oa.user.entity.SysPositionRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysPositionRoleMapper extends BaseMapper<SysPositionRole> {

    @Select("SELECT role_id FROM sys_position_role WHERE position_id = #{positionId}")
    List<Long> selectRoleIdsByPositionId(@Param("positionId") Long positionId);
}
