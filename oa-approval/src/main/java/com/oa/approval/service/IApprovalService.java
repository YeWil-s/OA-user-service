package com.oa.approval.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.oa.approval.dto.ApplicationQueryDTO;
import com.oa.approval.dto.ApplicationSubmitDTO;
import com.oa.approval.dto.ApprovalActionDTO;
import com.oa.approval.vo.ApplicationDetailVO;
import com.oa.approval.vo.ApplicationVO;

public interface IApprovalService {

    ApplicationDetailVO submit(ApplicationSubmitDTO dto);

    IPage<ApplicationVO> myApplications(ApplicationQueryDTO dto);

    ApplicationDetailVO getDetail(Long id);

    void cancel(Long id);

    IPage<ApplicationVO> pendingApplications(ApplicationQueryDTO dto);

    void approve(Long id, ApprovalActionDTO dto);

    IPage<ApplicationVO> processedApplications(ApplicationQueryDTO dto);
}
