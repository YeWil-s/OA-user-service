package com.oa.datavisual.mapper;

import com.oa.datavisual.mapper.row.DeptNameRow;
import com.oa.datavisual.mapper.row.TypeCountRow;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface VisualRealtimeMapper {

    @Select("SELECT id AS deptId, dept_name AS deptName FROM user_db.sys_dept "
            + "WHERE status = 1 AND is_deleted = 0 ORDER BY sort_order ASC, id ASC")
    List<DeptNameRow> selectDeptNames();

    @Select("SELECT COUNT(1) FROM user_db.sys_user WHERE status = 1 AND is_deleted = 0")
    Long countActiveEmployees();

    @Select("SELECT COUNT(1) FROM attendance_db.att_record "
            + "WHERE record_date = CURRENT_DATE AND punch_in_time IS NOT NULL")
    Long countTodayPunchIn();

    @Select("SELECT COUNT(1) FROM attendance_db.att_record "
            + "WHERE record_date = CURRENT_DATE AND punch_out_time IS NOT NULL")
    Long countTodayPunchOut();

    @Select("SELECT COUNT(1) FROM approval_db.app_application WHERE status = 1")
    Long countPendingApprovals();

    @Select("SELECT app_type AS type, COUNT(1) AS count FROM approval_db.app_application "
            + "WHERE DATE_FORMAT(create_time, '%Y-%m') = #{month} GROUP BY app_type ORDER BY app_type")
    List<TypeCountRow> selectApprovalTypeCounts(@Param("month") String month);
}
