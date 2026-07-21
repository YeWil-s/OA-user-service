package com.oa.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oa.ai.entity.AppApplication;
import org.apache.ibatis.annotations.Select;

public interface ApplicationMapper extends BaseMapper<AppApplication> {

    @Select("SELECT MAX(application_no) FROM approval_db.app_application WHERE application_no LIKE CONCAT('LV', DATE_FORMAT(NOW(), '%Y%m%d'), '%')")
    String selectMaxApplicationNoToday();
}
