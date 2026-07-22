package com.oa.datavisual.service;

import com.oa.datavisual.dto.StatisticsSyncResponse;

public interface IStatisticsSyncService {

    StatisticsSyncResponse syncMonth(String month);

    StatisticsSyncResponse syncCurrentMonth();

    StatisticsSyncResponse syncLastMonth();
}
