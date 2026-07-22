package com.oa.datavisual.job;

import com.oa.datavisual.dto.StatisticsSyncResponse;
import com.oa.datavisual.service.IStatisticsSyncService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class VisualStatisticsJobHandler {

    private final IStatisticsSyncService statisticsSyncService;

    public VisualStatisticsJobHandler(IStatisticsSyncService statisticsSyncService) {
        this.statisticsSyncService = statisticsSyncService;
    }

    @XxlJob("visualStatisticsMonthlyJob")
    public void visualStatisticsMonthlyJob() {
        String month = XxlJobHelper.getJobParam();
        StatisticsSyncResponse response = StringUtils.hasText(month)
                ? statisticsSyncService.syncMonth(month.trim())
                : statisticsSyncService.syncLastMonth();
        XxlJobHelper.log("visualStatisticsMonthlyJob synced month={}, deptRows={}, attendanceRows={}, approvalRows={}",
                response.statMonth(),
                response.deptOverviewRows(),
                response.attendanceRows(),
                response.approvalRows());
        XxlJobHelper.handleSuccess("统计数据同步完成: " + response.statMonth());
    }

    @XxlJob("visualStatisticsCurrentMonthJob")
    public void visualStatisticsCurrentMonthJob() {
        String month = XxlJobHelper.getJobParam();
        StatisticsSyncResponse response = StringUtils.hasText(month)
                ? statisticsSyncService.syncMonth(month.trim())
                : statisticsSyncService.syncCurrentMonth();
        XxlJobHelper.log("visualStatisticsCurrentMonthJob synced month={}, deptRows={}, attendanceRows={}, approvalRows={}",
                response.statMonth(),
                response.deptOverviewRows(),
                response.attendanceRows(),
                response.approvalRows());
        XxlJobHelper.handleSuccess("统计数据同步完成: " + response.statMonth());
    }
}
