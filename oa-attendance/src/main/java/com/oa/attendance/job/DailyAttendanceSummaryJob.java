package com.oa.attendance.job;

import com.oa.attendance.service.IAttendanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DailyAttendanceSummaryJob {

    private static final Logger log = LoggerFactory.getLogger(DailyAttendanceSummaryJob.class);

    private final IAttendanceService attendanceService;

    public DailyAttendanceSummaryJob(IAttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @Scheduled(cron = "0 5 1 * * ?")
    public void generateYesterdaySummary() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("开始生成昨日考勤汇总: {}", yesterday);
        try {
            attendanceService.generateDailySummary(yesterday);
            log.info("昨日考勤汇总完成: {}", yesterday);
        } catch (Exception e) {
            log.error("昨日考勤汇总失败: {}", yesterday, e);
        }
    }
}
