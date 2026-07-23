package com.oa.ai.service;

import com.oa.ai.dto.ApprovalSubmitDTO;

public interface IApprovalService {

    String submitApplication(ApprovalSubmitDTO dto, Long userId, Long deptId);
}
