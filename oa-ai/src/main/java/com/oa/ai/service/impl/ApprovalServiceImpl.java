package com.oa.ai.service.impl;

import com.oa.ai.client.ApprovalServiceClient;
import com.oa.ai.dto.ApprovalSubmitDTO;
import com.oa.ai.service.IApprovalService;
import com.oa.common.exception.BusinessException;
import com.oa.common.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class ApprovalServiceImpl implements IApprovalService {

    private static final Logger log = LoggerFactory.getLogger(ApprovalServiceImpl.class);

    private final ApprovalServiceClient approvalServiceClient;

    public ApprovalServiceImpl(ApprovalServiceClient approvalServiceClient) {
        this.approvalServiceClient = approvalServiceClient;
    }

    @Override
    public String submitApplication(ApprovalSubmitDTO dto, Long userId, Long deptId) {
        Map<String, Object> body = new HashMap<>();
        body.put("appType", dto.getAppType());
        body.put("leaveType", dto.getLeaveType());
        body.put("startTime", dto.getStartTime().toString());
        body.put("endTime", dto.getEndTime().toString());
        body.put("reason", dto.getReason());

        try {
            Result<Map<String, Object>> result = approvalServiceClient.submit(body);
            if (result == null || result.getCode() != 200 || result.getData() == null) {
                throw new BusinessException(50000, "调用审批服务失败");
            }
            Object applicationNo = result.getData().get("applicationNo");
            return applicationNo != null ? applicationNo.toString() : "未知单号";
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用审批服务提交申请失败", e);
            throw new BusinessException(50000, "调用审批服务失败: " + e.getMessage());
        }
    }
}
