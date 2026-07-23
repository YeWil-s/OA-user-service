package com.oa.attendance.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class ScheduleDTO {

    @NotEmpty(message = "排班项不能为空")
    @Valid
    private List<ScheduleItem> items;

    public List<ScheduleItem> getItems() { return items; }
    public void setItems(List<ScheduleItem> items) { this.items = items; }
}
