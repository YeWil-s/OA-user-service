-- ============================================================
-- 恢复 2026年7月 考勤日汇总数据
-- 基于 att_record 打卡记录 + user_shift 排班 + att_shift 班次
-- 执行方式: mysql -u root -p123456 < recover_july_daily_summary.sql
-- ============================================================

-- 1. 先删除7月已有的日汇总（如果有的话）
DELETE FROM attendance_db.att_daily_summary WHERE summary_date >= '2026-07-01' AND summary_date < '2026-08-01';

-- 2. 从打卡记录 + 排班数据生成日汇总
INSERT INTO attendance_db.att_daily_summary
    (user_id, dept_id, summary_date, shift_id, punch_in_time, punch_out_time,
     status, late_minutes, early_minutes, work_hours, overtime_hours, create_time)
SELECT
    r.user_id,
    u.dept_id,
    r.record_date,
    us.shift_id,
    r.punch_in_time,
    r.punch_out_time,
    CASE
        -- 请假：punchType=3 且无打卡时间
        WHEN r.punch_type = 3 AND r.punch_in_time IS NULL AND r.punch_out_time IS NULL THEN 6
        -- 缺卡：有记录但缺少打卡时间
        WHEN r.punch_in_time IS NULL OR r.punch_out_time IS NULL THEN 5
        -- 正常：上午9点前打卡
        WHEN TIME(r.punch_in_time) <= ADDTIME(s.start_time, '00:15:00') THEN 1
        -- 迟到：上午9点后打卡
        WHEN TIME(r.punch_in_time) > ADDTIME(s.start_time, '00:15:00')
             AND TIME(r.punch_in_time) < ADDTIME(s.start_time, '02:00:00') THEN 2
        -- 其他视为正常
        ELSE 1
    END AS status,
    CASE
        WHEN r.punch_in_time IS NULL THEN 0
        WHEN TIME(r.punch_in_time) > s.start_time
        THEN GREATEST(0, TIMESTAMPDIFF(MINUTE, TIMESTAMP(r.record_date, s.start_time), r.punch_in_time))
        ELSE 0
    END AS late_minutes,
    CASE
        WHEN r.punch_out_time IS NULL THEN 0
        WHEN TIME(r.punch_out_time) < s.end_time
        THEN GREATEST(0, TIMESTAMPDIFF(MINUTE, r.punch_out_time, TIMESTAMP(r.record_date, s.end_time)))
        ELSE 0
    END AS early_minutes,
    CASE
        WHEN r.punch_in_time IS NULL OR r.punch_out_time IS NULL THEN 0.0
        ELSE ROUND(GREATEST(0, TIMESTAMPDIFF(MINUTE, r.punch_in_time, r.punch_out_time)) / 60.0
                   - (CASE WHEN TIMESTAMPDIFF(MINUTE, r.punch_in_time, r.punch_out_time) > 540 THEN 1.0 ELSE 0.0 END), 1)
    END AS work_hours,
    CASE
        WHEN r.punch_out_time IS NULL THEN 0.0
        WHEN TIME(r.punch_out_time) > ADDTIME(s.end_time, '00:30:00')
        THEN ROUND(GREATEST(0, TIMESTAMPDIFF(MINUTE, TIMESTAMP(r.record_date, s.end_time), r.punch_out_time)) / 60.0, 1)
        ELSE 0.0
    END AS overtime_hours,
    NOW()
FROM attendance_db.att_record r
JOIN user_db.sys_user u ON u.id = r.user_id AND u.status = 1 AND u.is_deleted = 0
LEFT JOIN attendance_db.user_shift us ON us.user_id = r.user_id
LEFT JOIN attendance_db.att_shift s ON s.id = us.shift_id AND s.status = 1
WHERE r.record_date >= '2026-07-01' AND r.record_date < '2026-08-01';

-- 3. 验证生成结果
SELECT summary_date, COUNT(*) AS total,
       SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) AS normal,
       SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) AS late,
       SUM(CASE WHEN status = 5 THEN 1 ELSE 0 END) AS missing_punch,
       SUM(CASE WHEN status = 6 THEN 1 ELSE 0 END) AS leave_count
FROM attendance_db.att_daily_summary
WHERE summary_date >= '2026-07-01' AND summary_date < '2026-08-01'
GROUP BY summary_date
ORDER BY summary_date;

-- 4. 日汇总生成后，重新同步7月统计数据到 statistics_db
-- 这个需要调用 API，或者在应用重启后点"同步统计"按钮（已修复空数据保护）
-- POST /api/visual/statistics/sync?month=2026-07
