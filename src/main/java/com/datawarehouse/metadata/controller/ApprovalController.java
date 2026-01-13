package com.datawarehouse.metadata.controller;

import com.datawarehouse.metadata.common.PageResult;
import com.datawarehouse.metadata.common.Result;
import com.datawarehouse.metadata.dto.request.ApprovalActionRequest;
import com.datawarehouse.metadata.dto.request.ApprovalRequest;
import com.datawarehouse.metadata.service.IApprovalService;
import com.datawarehouse.metadata.vo.ApprovalFlowVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 审批流程Controller
 *
 * @author System
 * @since 1.0.0
 */
@Api(tags = "审批流程管理")
@RestController
@RequestMapping("/api/metadata/approvals")
@RequiredArgsConstructor
public class ApprovalController {

    private final IApprovalService approvalService;

    @ApiOperation("创建审批单")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody ApprovalRequest request) {
        Long approvalId = approvalService.createApproval(request);
        return Result.success("创建成功", approvalId);
    }

    @ApiOperation("提交审批")
    @PostMapping("/{id}/submit")
    public Result<Void> submit(@ApiParam("审批单ID") @PathVariable Long id) {
        approvalService.submitApproval(id);
        return Result.success("提交成功", null);
    }

    @ApiOperation("审批通过")
    @PostMapping("/{id}/approve")
    public Result<Void> approve(
            @ApiParam("审批单ID") @PathVariable Long id,
            @Valid @RequestBody ApprovalActionRequest request) {
        approvalService.approve(id, request);
        return Result.success("审批通过", null);
    }

    @ApiOperation("审批拒绝")
    @PostMapping("/{id}/reject")
    public Result<Void> reject(
            @ApiParam("审批单ID") @PathVariable Long id,
            @Valid @RequestBody ApprovalActionRequest request) {
        approvalService.reject(id, request);
        return Result.success("审批拒绝", null);
    }

    @ApiOperation("取消审批")
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@ApiParam("审批单ID") @PathVariable Long id) {
        approvalService.cancel(id);
        return Result.success("取消成功", null);
    }

    @ApiOperation("发布变更")
    @PostMapping("/{id}/publish")
    public Result<Void> publish(@ApiParam("审批单ID") @PathVariable Long id) {
        approvalService.publish(id);
        return Result.success("发布成功", null);
    }

    @ApiOperation("获取审批详情")
    @GetMapping("/{id}")
    public Result<ApprovalFlowVO> getDetail(@ApiParam("审批单ID") @PathVariable Long id) {
        ApprovalFlowVO vo = approvalService.getApprovalDetail(id);
        return Result.success(vo);
    }

    @ApiOperation("获取我提交的审批单")
    @GetMapping("/my-submissions")
    public Result<PageResult<ApprovalFlowVO>> getMySubmissions(
            @ApiParam("提交人") @RequestParam String submitter,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<ApprovalFlowVO> result = approvalService.getMySubmissions(submitter, pageNum, pageSize);
        return Result.success(result);
    }

    @ApiOperation("获取待我审批的审批单")
    @GetMapping("/pending")
    public Result<PageResult<ApprovalFlowVO>> getPendingApprovals(
            @ApiParam("审批人") @RequestParam String approver,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<ApprovalFlowVO> result = approvalService.getPendingApprovals(approver, pageNum, pageSize);
        return Result.success(result);
    }

    @ApiOperation("获取所有审批单")
    @GetMapping("/all")
    public Result<PageResult<ApprovalFlowVO>> getAllApprovals(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<ApprovalFlowVO> result = approvalService.getAllApprovals(pageNum, pageSize);
        return Result.success(result);
    }
}
