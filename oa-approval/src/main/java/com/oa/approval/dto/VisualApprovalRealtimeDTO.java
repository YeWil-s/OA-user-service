package com.oa.approval.dto;

import java.util.List;

public record VisualApprovalRealtimeDTO(long pendingCount, List<VisualTypeCountDTO> typeCounts) {
}
