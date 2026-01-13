package com.datawarehouse.metadata.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.datawarehouse.metadata.common.PageResult;
import com.datawarehouse.metadata.dto.request.ApprovalActionRequest;
import com.datawarehouse.metadata.dto.request.ApprovalRequest;
import com.datawarehouse.metadata.entity.ApprovalFlow;
import com.datawarehouse.metadata.vo.ApprovalFlowVO;

/**
 * 审批流程Service接口
 *
 * @author System
 * @since 1.0.0
 */
public interface IApprovalService extends IService<ApprovalFlow> {

    /**
     * 创建审批单
     *
     * @param request 审批请求
     * @return 审批单ID
     */
    Long createApproval(ApprovalRequest request);

    /**
     * 提交审批
     *
     * @param approvalId 审批单ID
     */
    void submitApproval(Long approvalId);

    /**
     * 审批通过
     *
     * @param approvalId 审批单ID
     * @param request 审批操作请求
     */
    void approve(Long approvalId, ApprovalActionRequest request);

    /**
     * 审批拒绝
     *
     * @param approvalId 审批单ID
     * @param request 审批操作请求
     */
    void reject(Long approvalId, ApprovalActionRequest request);

    /**
     * 取消审批
     *
     * @param approvalId 审批单ID
     */
    void cancel(Long approvalId);

    /**
     * 发布变更(审批通过后执行)
     *
     * @param approvalId 审批单ID
     */
    void publish(Long approvalId);

    /**
     * 获取审批详情
     *
     * @param approvalId 审批单ID
     * @return 审批详情
     */
    ApprovalFlowVO getApprovalDetail(Long approvalId);

    /**
     * 分页查询我的审批单(提交的)
     *
     * @param submitter 提交人
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResult<ApprovalFlowVO> getMySubmissions(String submitter, Integer pageNum, Integer pageSize);

    /**
     * 分页查询待我审批的审批单
     *
     * @param approver 审批人
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResult<ApprovalFlowVO> getPendingApprovals(String approver, Integer pageNum, Integer pageSize);

    /**
     * 分页查询所有审批单
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResult<ApprovalFlowVO> getAllApprovals(Integer pageNum, Integer pageSize);
}
