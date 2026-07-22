package com.oa.datavisual.mapper;

import com.oa.datavisual.entity.StatApprovalSummary;
import com.oa.datavisual.entity.StatAttendanceMonthly;
import com.oa.datavisual.entity.StatDeptOverview;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface VisualStatisticsExtractMapper {

    @Select("""
            SELECT
                #{statMonth} AS statMonth,
                d.id AS deptId,
                COALESCE(active_user.total_employees, 0) AS totalEmployees,
                COALESCE(att.normal_count, 0) AS normalCount,
                COALESCE(att.late_count, 0) AS lateCount,
                COALESCE(att.early_count, 0) AS earlyCount,
                COALESCE(att.absent_count, 0) AS absentCount,
                COALESCE(att.leave_count, 0) AS leaveCount,
                COALESCE(att.overtime_total, 0) AS overtimeTotal,
                NOW() AS createTime
            FROM user_db.sys_dept d
            LEFT JOIN (
                SELECT dept_id, COUNT(1) AS total_employees
                FROM user_db.sys_user
                WHERE status = 1
                  AND is_deleted = 0
                  AND dept_id IS NOT NULL
                GROUP BY dept_id
            ) active_user ON active_user.dept_id = d.id
            LEFT JOIN (
                SELECT
                    dept_id,
                    SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) AS normal_count,
                    SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) AS late_count,
                    SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END) AS early_count,
                    SUM(CASE WHEN status = 4 THEN 1 ELSE 0 END) AS absent_count,
                    SUM(CASE WHEN status = 6 THEN 1 ELSE 0 END) AS leave_count,
                    ROUND(COALESCE(SUM(overtime_hours), 0), 1) AS overtime_total
                FROM attendance_db.att_daily_summary
                WHERE summary_date >= #{startDate}
                  AND summary_date < #{endDate}
                GROUP BY dept_id
            ) att ON att.dept_id = d.id
            WHERE d.status = 1
              AND d.is_deleted = 0
            HAVING totalEmployees > 0
                OR normalCount > 0
                OR lateCount > 0
                OR earlyCount > 0
                OR absentCount > 0
                OR leaveCount > 0
                OR overtimeTotal > 0
            ORDER BY d.sort_order ASC, d.id ASC
            """)
    List<StatAttendanceMonthly> selectAttendanceMonthly(
            @Param("statMonth") String statMonth,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Select("""
            SELECT
                #{statMonth} AS statMonth,
                d.id AS deptId,
                COALESCE(active_user.active_employees, 0) AS activeEmployees,
                COALESCE(new_user.new_hires, 0) AS newHires,
                COALESCE(resigned_user.resignations, 0) AS resignations,
                COALESCE(attendance.attendance_rate, 0) AS attendanceRate,
                NOW() AS createTime
            FROM user_db.sys_dept d
            LEFT JOIN (
                SELECT dept_id, COUNT(1) AS active_employees
                FROM user_db.sys_user
                WHERE status = 1
                  AND is_deleted = 0
                  AND dept_id IS NOT NULL
                  AND (entry_date IS NULL OR entry_date < #{endDate})
                GROUP BY dept_id
            ) active_user ON active_user.dept_id = d.id
            LEFT JOIN (
                SELECT dept_id, COUNT(1) AS new_hires
                FROM user_db.sys_user
                WHERE is_deleted = 0
                  AND dept_id IS NOT NULL
                  AND entry_date >= #{startDate}
                  AND entry_date < #{endDate}
                GROUP BY dept_id
            ) new_user ON new_user.dept_id = d.id
            LEFT JOIN (
                SELECT dept_id, COUNT(1) AS resignations
                FROM user_db.sys_user
                WHERE status = 0
                  AND is_deleted = 0
                  AND dept_id IS NOT NULL
                  AND update_time >= #{startTime}
                  AND update_time < #{endTime}
                GROUP BY dept_id
            ) resigned_user ON resigned_user.dept_id = d.id
            LEFT JOIN (
                SELECT
                    dept_id,
                    ROUND(
                        SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) * 100.0 / NULLIF(COUNT(1), 0),
                        2
                    ) AS attendance_rate
                FROM attendance_db.att_daily_summary
                WHERE summary_date >= #{startDate}
                  AND summary_date < #{endDate}
                GROUP BY dept_id
            ) attendance ON attendance.dept_id = d.id
            WHERE d.status = 1
              AND d.is_deleted = 0
            HAVING activeEmployees > 0
                OR newHires > 0
                OR resignations > 0
                OR attendanceRate > 0
            ORDER BY d.sort_order ASC, d.id ASC
            """)
    List<StatDeptOverview> selectDeptOverview(
            @Param("statMonth") String statMonth,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Select("""
            SELECT
                #{statMonth} AS statMonth,
                d.id AS deptId,
                COALESCE(app.total_applications, 0) AS totalApplications,
                COALESCE(app.approved_count, 0) AS approvedCount,
                COALESCE(app.rejected_count, 0) AS rejectedCount,
                COALESCE(app.pending_count, 0) AS pendingCount,
                COALESCE(app.avg_approval_hours, 0) AS avgApprovalHours,
                NOW() AS createTime
            FROM user_db.sys_dept d
            LEFT JOIN (
                SELECT
                    a.dept_id,
                    COUNT(1) AS total_applications,
                    SUM(CASE WHEN a.status = 2 THEN 1 ELSE 0 END) AS approved_count,
                    SUM(CASE WHEN a.status = 3 THEN 1 ELSE 0 END) AS rejected_count,
                    SUM(CASE WHEN a.status = 1 THEN 1 ELSE 0 END) AS pending_count,
                    ROUND(
                        COALESCE(AVG(
                            CASE
                                WHEN a.status IN (2, 3) AND finish_record.finish_time IS NOT NULL
                                THEN TIMESTAMPDIFF(MINUTE, a.create_time, finish_record.finish_time) / 60.0
                                ELSE NULL
                            END
                        ), 0),
                        1
                    ) AS avg_approval_hours
                FROM approval_db.app_application a
                LEFT JOIN (
                    SELECT application_id, MAX(action_time) AS finish_time
                    FROM approval_db.app_approval_record
                    GROUP BY application_id
                ) finish_record ON finish_record.application_id = a.id
                WHERE a.status IN (1, 2, 3)
                  AND a.create_time >= #{startTime}
                  AND a.create_time < #{endTime}
                GROUP BY a.dept_id
            ) app ON app.dept_id = d.id
            WHERE d.status = 1
              AND d.is_deleted = 0
            HAVING totalApplications > 0
            ORDER BY d.sort_order ASC, d.id ASC
            """)
    List<StatApprovalSummary> selectApprovalSummary(
            @Param("statMonth") String statMonth,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}
