package com.oa.user.service;

import com.oa.user.dto.VisualDeptStatsDTO;

import java.util.List;

public interface IVisualStatisticsService {

    List<VisualDeptStatsDTO> departmentStats(String month);
}
