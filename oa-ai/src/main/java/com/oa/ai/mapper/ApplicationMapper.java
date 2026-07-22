package com.oa.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oa.ai.entity.AppApplication;
import org.apache.ibatis.annotations.Select;

public interface ApplicationMapper extends BaseMapper<AppApplication> {

    // TODO: oa-approval 上线后改为 HTTP 调用，届时删除本 Mapper
    @Select("SELECT MAX(application_no) FROM app_application WHERE application_no LIKE CONCAT('LV', DATE_FORMAT(NOW(), '%Y%m%d'), '%')")
    String selectMaxApplicationNoToday();
}
