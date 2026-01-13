package com.datawarehouse.metadata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.datawarehouse.metadata.common.PageResult;
import com.datawarehouse.metadata.dto.request.ApprovalActionRequest;
import com.datawarehouse.metadata.dto.request.ApprovalRequest;
import com.datawarehouse.metadata.entity.ApprovalFlow;
import com.datawarehouse.metadata.entity.MetadataTable;
import com.datawarehouse.metadata.enums.ApprovalStatusEnum;
import com.datawarehouse.metadata.enums.OperationTypeEnum;
import com.datawarehouse.metadata.exception.BusinessException;
import com.datawarehouse.metadata.mapper.ApprovalFlowMapper;
import com.datawarehouse.metadata.mapper.MetadataTableMapper;
import com.datawarehouse.metadata.service.IApprovalService;
import com.datawarehouse.metadata.service.IOperationHistoryService;
import com.datawarehouse.metadata.vo.ApprovalFlowVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 审批流程Service实现类
 *
 * @author System
 * @since 1.0.0
 */
@Slf4j
@Service
public class ApprovalServiceImpl extends ServiceImpl<ApprovalFlowMapper, ApprovalFlow>
        implements IApprovalService {

    @Autowired(required = false)
    private MetadataTableMapper tableMapper;

    @Autowired(required = false)
    private IOperationHistoryService historyService;

    private static final AtomicLong FLOW_NO_SEQUENCE = new AtomicLong(1000);

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createApproval(ApprovalRequest request) {
        // 1. 校验表是否存在
        MetadataTable table = tableMapper.selectById(request.getTableId());
        if (table == null) {
            throw new BusinessException("表不存在: ID=" + request.getTableId());
        }

        // 2. 生成审批单号
        String flowNo = generateFlowNo();

        // 3. 创建审批单
        ApprovalFlow approval = new ApprovalFlow();
        approval.setFlowNo(flowNo);
        approval.setTableId(request.getTableId());
        approval.setApprovalType(request.getApprovalType());
        approval.setStatus(ApprovalStatusEnum.DRAFT.getCode());
        approval.setSubmitter("system"); // TODO: 从当前登录用户获取
        approval.setSubmitTime(LocalDateTime.now());
        approval.setChangeContent(request.getChangeContent());

        this.save(approval);

        // 4. 记录操作历史
        if (historyService != null) {
            historyService.recordOperation(
                request.getTableId(),
                OperationTypeEnum.CREATE,
                null,
                approval,
                "创建审批单: " + flowNo
            );
        }

        return approval.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitApproval(Long approvalId) {
        ApprovalFlow approval = getAndValidateApproval(approvalId);

        // 状态检查
        if (!ApprovalStatusEnum.DRAFT.getCode().equals(approval.getStatus())) {
            throw new BusinessException("只有草稿状态的审批单可以提交");
        }

        // 更新状态
        approval.setStatus(ApprovalStatusEnum.PENDING.getCode());
        approval.setSubmitTime(LocalDateTime.now());
        this.updateById(approval);

        // 记录操作历史
        if (historyService != null) {
            historyService.recordOperation(
                approval.getTableId(),
                OperationTypeEnum.APPROVE,
                null,
                approval,
                "提交审批: " + approval.getFlowNo()
            );
        }

        log.info("审批单提交成功: flowNo={}", approval.getFlowNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long approvalId, ApprovalActionRequest request) {
        ApprovalFlow approval = getAndValidateApproval(approvalId);

        // 状态检查
        ApprovalStatusEnum currentStatus = ApprovalStatusEnum.getByCode(approval.getStatus());
        if (currentStatus == null || !currentStatus.canApprove()) {
            throw new BusinessException("当前状态不允许审批: " + approval.getStatus());
        }

        // 更新审批信息
        approval.setStatus(ApprovalStatusEnum.APPROVED.getCode());
        approval.setApprover("system"); // TODO: 从当前登录用户获取
        approval.setApproveTime(LocalDateTime.now());
        approval.setApproveComment(request.getComment());
        this.updateById(approval);

        // 记录操作历史
        if (historyService != null) {
            historyService.recordOperation(
                approval.getTableId(),
                OperationTypeEnum.APPROVE,
                null,
                approval,
                "审批通过: " + approval.getFlowNo()
            );
        }

        log.info("审批通过: flowNo={}, approver={}", approval.getFlowNo(), approval.getApprover());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long approvalId, ApprovalActionRequest request) {
        ApprovalFlow approval = getAndValidateApproval(approvalId);

        // 状态检查
        ApprovalStatusEnum currentStatus = ApprovalStatusEnum.getByCode(approval.getStatus());
        if (currentStatus == null || !currentStatus.canApprove()) {
            throw new BusinessException("当前状态不允许审批: " + approval.getStatus());
        }

        // 更新审批信息
        approval.setStatus(ApprovalStatusEnum.REJECTED.getCode());
        approval.setApprover("system"); // TODO: 从当前登录用户获取
        approval.setApproveTime(LocalDateTime.now());
        approval.setApproveComment(request.getComment());
        this.updateById(approval);

        // 记录操作历史
        if (historyService != null) {
            historyService.recordOperation(
                approval.getTableId(),
                OperationTypeEnum.REJECT,
                null,
                approval,
                "审批拒绝: " + approval.getFlowNo()
            );
        }

        log.info("审批拒绝: flowNo={}, approver={}, reason={}",
            approval.getFlowNo(), approval.getApprover(), request.getComment());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long approvalId) {
        ApprovalFlow approval = getAndValidateApproval(approvalId);

        // 状态检查 - 只有PENDING和APPROVED状态可以取消
        if (!ApprovalStatusEnum.PENDING.getCode().equals(approval.getStatus()) &&
            !ApprovalStatusEnum.APPROVED.getCode().equals(approval.getStatus())) {
            throw new BusinessException("当前状态不允许取消: " + approval.getStatus());
        }

        // 更新状态
        approval.setStatus(ApprovalStatusEnum.CANCELLED.getCode());
        this.updateById(approval);

        // 记录操作历史
        if (historyService != null) {
            historyService.recordOperation(
                approval.getTableId(),
                OperationTypeEnum.DELETE,
                null,
                approval,
                "取消审批: " + approval.getFlowNo()
            );
        }

        log.info("审批取消: flowNo={}", approval.getFlowNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(Long approvalId) {
        ApprovalFlow approval = getAndValidateApproval(approvalId);

        // 状态检查
        ApprovalStatusEnum currentStatus = ApprovalStatusEnum.getByCode(approval.getStatus());
        if (currentStatus == null || !currentStatus.canPublish()) {
            throw new BusinessException("只有审批通过的单据可以发布: " + approval.getStatus());
        }

        // 更新状态
        approval.setStatus(ApprovalStatusEnum.PUBLISHED.getCode());
        this.updateById(approval);

        // TODO: 执行实际的变更操作(如执行SQL等)
        // executeChange(approval);

        // 记录操作历史
        if (historyService != null) {
            historyService.recordOperation(
                approval.getTableId(),
                OperationTypeEnum.PUBLISH,
                null,
                approval,
                "发布变更: " + approval.getFlowNo()
            );
        }

        log.info("变更发布成功: flowNo={}", approval.getFlowNo());
    }

    @Override
    public ApprovalFlowVO getApprovalDetail(Long approvalId) {
        ApprovalFlow approval = this.getById(approvalId);
        if (approval == null) {
            throw new BusinessException("审批单不存在: ID=" + approvalId);
        }

        return convertToVO(approval);
    }

    @Override
    public PageResult<ApprovalFlowVO> getMySubmissions(String submitter, Integer pageNum, Integer pageSize) {
        Page<ApprovalFlow> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<ApprovalFlow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalFlow::getSubmitter, submitter)
               .orderByDesc(ApprovalFlow::getSubmitTime);

        Page<ApprovalFlow> resultPage = this.page(page, wrapper);
        return PageResult.of(resultPage.convert(this::convertToVO));
    }

    @Override
    public PageResult<ApprovalFlowVO> getPendingApprovals(String approver, Integer pageNum, Integer pageSize) {
        Page<ApprovalFlow> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<ApprovalFlow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalFlow::getStatus, ApprovalStatusEnum.PENDING.getCode())
               .orderByAsc(ApprovalFlow::getSubmitTime);

        Page<ApprovalFlow> resultPage = this.page(page, wrapper);
        return PageResult.of(resultPage.convert(this::convertToVO));
    }

    @Override
    public PageResult<ApprovalFlowVO> getAllApprovals(Integer pageNum, Integer pageSize) {
        Page<ApprovalFlow> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<ApprovalFlow> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ApprovalFlow::getSubmitTime);

        Page<ApprovalFlow> resultPage = this.page(page, wrapper);
        return PageResult.of(resultPage.convert(this::convertToVO));
    }

    /**
     * 获取并校验审批单
     */
    private ApprovalFlow getAndValidateApproval(Long approvalId) {
        ApprovalFlow approval = this.getById(approvalId);
        if (approval == null) {
            throw new BusinessException("审批单不存在: ID=" + approvalId);
        }
        return approval;
    }

    /**
     * 生成审批单号
     * 格式: APR-yyyyMMdd-序号
     */
    private String generateFlowNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long sequence = FLOW_NO_SEQUENCE.incrementAndGet();
        return String.format("APR-%s-%06d", dateStr, sequence);
    }

    /**
     * 实体转VO
     */
    private ApprovalFlowVO convertToVO(ApprovalFlow entity) {
        if (entity == null) {
            return null;
        }

        ApprovalFlowVO vo = new ApprovalFlowVO();
        BeanUtils.copyProperties(entity, vo);

        // 可以在这里关联查询表名和数据库名(暂不实现，避免N+1查询)
        return vo;
    }
}
