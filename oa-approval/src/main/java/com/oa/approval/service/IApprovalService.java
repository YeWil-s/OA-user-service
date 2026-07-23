package com.oa.approval.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.oa.approval.dto.ApplicationQueryDTO;
import com.oa.approval.dto.ApplicationSubmitDTO;
import com.oa.approval.dto.ApprovalActionDTO;
import com.oa.approval.dto.VisualApprovalRealtimeDTO;
import com.oa.approval.dto.VisualApprovalStatsDTO;
import com.oa.approval.vo.ApplicationDetailVO;
import com.oa.approval.vo.ApplicationVO;

import java.util.List;

public interface IApprovalService {

    ApplicationDetailVO submit(ApplicationSubmitDTO dto);

    IPage<ApplicationVO> myApplications(ApplicationQueryDTO dto);

    ApplicationDetailVO getDetail(Long id);

    void cancel(Long id);

    IPage<ApplicationVO> pendingApplications(ApplicationQueryDTO dto);

    void approve(Long id, ApprovalActionDTO dto);

    IPage<ApplicationVO> processedApplications(ApplicationQueryDTO dto);

    IPage<ApplicationVO> allApplications(ApplicationQueryDTO dto);

    IPage<ApplicationVO> allPending(ApplicationQueryDTO dto);

    List<VisualApprovalStatsDTO> monthlyVisualStats(String month);

    VisualApprovalRealtimeDTO visualRealtimeStats(String month);
}
