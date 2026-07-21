package com.oa.datavisual.controller;

import com.oa.common.result.Result;
import com.oa.datavisual.dto.StatisticsSyncResponse;
import com.oa.datavisual.service.IStatisticsSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "数据统计同步")
@RestController
@RequestMapping("/api/visual/statistics")
public class VisualStatisticsController {

    private final IStatisticsSyncService statisticsSyncService;

    public VisualStatisticsController(IStatisticsSyncService statisticsSyncService) {
        this.statisticsSyncService = statisticsSyncService;
    }

    @Operation(summary = "手动同步指定月份统计数据")
    @PostMapping("/sync")
    public Result<StatisticsSyncResponse> sync(@RequestParam(required = false) String month) {
        return Result.success(statisticsSyncService.syncMonth(month));
    }

    @Operation(summary = "手动同步上月统计数据")
    @PostMapping("/sync/last-month")
    public Result<StatisticsSyncResponse> syncLastMonth() {
        return Result.success(statisticsSyncService.syncLastMonth());
    }
}
