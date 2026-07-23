package com.oa.approval.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.oa.approval.dto.ApplicationQueryDTO;
import com.oa.approval.dto.ApplicationSubmitDTO;
import com.oa.approval.dto.ApprovalActionDTO;
import com.oa.approval.service.IApprovalService;
import com.oa.approval.vo.ApplicationDetailVO;
import com.oa.approval.vo.ApplicationVO;
import com.oa.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Approval Service")
@Validated
@RestController
@RequestMapping("/api/approval")
public class ApprovalController {

    private final IApprovalService approvalService;

    public ApprovalController(IApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @Operation(summary = "Submit application")
    @PostMapping("/applications")
    public Result<ApplicationDetailVO> submit(@Valid @RequestBody ApplicationSubmitDTO dto) {
        return Result.success("application submitted successfully", approvalService.submit(dto));
    }

    @Operation(summary = "My applications")
    @GetMapping("/applications")
    public Result<IPage<ApplicationVO>> myApplications(@Valid ApplicationQueryDTO dto) {

        return Result.success(approvalService.myApplications(dto));
    }

    @Operation(summary = "Application detail")
    @GetMapping("/applications/{id}")
    public Result<ApplicationDetailVO> detail(@PathVariable Long id) {
        return Result.success(approvalService.getDetail(id));
    }

    @Operation(summary = "Cancel application")
    @PutMapping("/applications/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        approvalService.cancel(id);
        return Result.success();
    }

    @Operation(summary = "Pending approvals")
    @GetMapping("/pending")
    public Result<IPage<ApplicationVO>> pendingApplications(@Valid ApplicationQueryDTO dto) {
        return Result.success(approvalService.pendingApplications(dto));
    }

    @Operation(summary = "Approve application")
    @PutMapping("/pending/{id}/approve")
    public Result<Void> approve(@PathVariable Long id, @Valid @RequestBody ApprovalActionDTO dto) {
        approvalService.approve(id, dto);
        return Result.success();
    }

    @Operation(summary = "Processed approvals")
    @GetMapping("/processed")
    public Result<IPage<ApplicationVO>> processedApplications(@Valid ApplicationQueryDTO dto) {
        return Result.success(approvalService.processedApplications(dto));
    }

    @Operation(summary = "All applications (admin/HR only)")
    @GetMapping("/applications/all")
    public Result<IPage<ApplicationVO>> allApplications(@Valid ApplicationQueryDTO dto) {
        return Result.success(approvalService.allApplications(dto));
    }

    @Operation(summary = "All pending (admin/HR only)")
    @GetMapping("/pending/all")
    public Result<IPage<ApplicationVO>> allPending(@Valid ApplicationQueryDTO dto) {
        return Result.success(approvalService.allPending(dto));
    }
}
