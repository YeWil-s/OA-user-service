package com.oa.user.controller;

import com.oa.common.result.Result;
import com.oa.user.dto.VisualDeptStatsDTO;
import com.oa.user.service.IVisualStatisticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user/internal/visual")
public class InternalVisualController {

    private final IVisualStatisticsService visualStatisticsService;

    public InternalVisualController(IVisualStatisticsService visualStatisticsService) {
        this.visualStatisticsService = visualStatisticsService;
    }

    @GetMapping("/departments")
    public Result<List<VisualDeptStatsDTO>> departmentStats(@RequestParam(required = false) String month) {
        return Result.success(visualStatisticsService.departmentStats(month));
    }
}
