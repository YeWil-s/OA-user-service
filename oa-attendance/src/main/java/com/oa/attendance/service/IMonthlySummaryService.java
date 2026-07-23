package com.oa.attendance.service;

import com.oa.attendance.vo.MonthlyAttendanceVO;
import java.util.List;

public interface IMonthlySummaryService {

    /**
     * 获取部门维度的月考勤汇总，供数据可视化模块调用。
     */
    List<MonthlyAttendanceVO> monthlyAttendanceSummary(String month);
}
