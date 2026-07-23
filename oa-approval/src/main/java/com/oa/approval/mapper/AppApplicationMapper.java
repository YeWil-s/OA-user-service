package com.oa.approval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oa.approval.entity.AppApplication;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface AppApplicationMapper extends BaseMapper<AppApplication> {

    @Select("SELECT MAX(CAST(RIGHT(application_no, 4) AS UNSIGNED)) FROM app_application WHERE application_no LIKE CONCAT(#{prefix}, '%')")
    Long selectMaxSeqByPrefix(@Param("prefix") String prefix);
}
