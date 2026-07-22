package com.oa.ai.service;

import com.oa.ai.dto.ApprovalSubmitDTO;
import com.oa.ai.entity.AppApplication;

public interface IApprovalService {

    String submitApplication(ApprovalSubmitDTO dto, Long userId, Long deptId);

    AppApplication getApplication(Long id);
}
