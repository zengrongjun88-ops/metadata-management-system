package com.datawarehouse.metadata.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import com.datawarehouse.metadata.service.impl.ApprovalServiceImpl;
import com.datawarehouse.metadata.vo.ApprovalFlowVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * ApprovalService单元测试
 *
 * @author System
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class ApprovalServiceTest {

    @Mock
    private ApprovalFlowMapper approvalMapper;

    @Mock
    private MetadataTableMapper tableMapper;

    @Mock
    private IOperationHistoryService historyService;

    @InjectMocks
    private ApprovalServiceImpl approvalService;

    private ApprovalRequest createApprovalRequest;
    private ApprovalFlow mockApproval;
    private MetadataTable mockTable;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        createApprovalRequest = new ApprovalRequest();
        createApprovalRequest.setTableId(100L);
        createApprovalRequest.setApprovalType("CREATE");
        createApprovalRequest.setChangeContent("{\"tableName\": \"test_table\"}");

        mockTable = new MetadataTable();
        mockTable.setId(100L);
        mockTable.setTableName("test_table");
        mockTable.setDatabaseName("test_db");

        mockApproval = new ApprovalFlow();
        mockApproval.setId(1L);
        mockApproval.setFlowNo("APR-20260111-001001");
        mockApproval.setTableId(100L);
        mockApproval.setApprovalType("CREATE");
        mockApproval.setStatus(ApprovalStatusEnum.DRAFT.getCode());
        mockApproval.setSubmitter("system");
        mockApproval.setSubmitTime(LocalDateTime.now());
        mockApproval.setChangeContent("{\"tableName\": \"test_table\"}");
    }

    @Test
    void testCreateApproval_Success() {
        // given
        when(tableMapper.selectById(100L)).thenReturn(mockTable);

        doAnswer(invocation -> {
            ApprovalFlow approval = invocation.getArgument(0);
            approval.setId(1L);
            return true;
        }).when(approvalMapper).insert(any(ApprovalFlow.class));

        doNothing().when(historyService).recordOperation(anyLong(), any(OperationTypeEnum.class),
            any(), any(), anyString());

        // when
        Long approvalId = approvalService.createApproval(createApprovalRequest);

        // then
        assertNotNull(approvalId);
        assertEquals(1L, approvalId);

        ArgumentCaptor<ApprovalFlow> captor = ArgumentCaptor.forClass(ApprovalFlow.class);
        verify(approvalMapper, times(1)).insert(captor.capture());

        ApprovalFlow createdApproval = captor.getValue();
        assertEquals(ApprovalStatusEnum.DRAFT.getCode(), createdApproval.getStatus());
        assertTrue(createdApproval.getFlowNo().startsWith("APR-"));
        verify(historyService, times(1)).recordOperation(anyLong(), eq(OperationTypeEnum.CREATE),
            isNull(), any(ApprovalFlow.class), contains("创建审批单"));
    }

    @Test
    void testCreateApproval_TableNotExist_ThrowException() {
        // given
        when(tableMapper.selectById(999L)).thenReturn(null);
        createApprovalRequest.setTableId(999L);

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            approvalService.createApproval(createApprovalRequest);
        });

        assertTrue(exception.getMessage().contains("表不存在"));
        verify(approvalMapper, never()).insert(any(ApprovalFlow.class));
    }

    @Test
    void testSubmitApproval_Success() {
        // given - DRAFT状态
        when(approvalMapper.selectById(1L)).thenReturn(mockApproval);
        when(approvalMapper.updateById(any(ApprovalFlow.class))).thenReturn(1);
        doNothing().when(historyService).recordOperation(anyLong(), any(OperationTypeEnum.class),
            any(), any(), anyString());

        // when
        approvalService.submitApproval(1L);

        // then
        ArgumentCaptor<ApprovalFlow> captor = ArgumentCaptor.forClass(ApprovalFlow.class);
        verify(approvalMapper, times(1)).updateById(captor.capture());

        ApprovalFlow updatedApproval = captor.getValue();
        assertEquals(ApprovalStatusEnum.PENDING.getCode(), updatedApproval.getStatus());
        verify(historyService, times(1)).recordOperation(anyLong(), eq(OperationTypeEnum.APPROVE),
            isNull(), any(ApprovalFlow.class), contains("提交审批"));
    }

    @Test
    void testSubmitApproval_NotDraftStatus_ThrowException() {
        // given - PENDING状态不能再次提交
        mockApproval.setStatus(ApprovalStatusEnum.PENDING.getCode());
        when(approvalMapper.selectById(1L)).thenReturn(mockApproval);

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            approvalService.submitApproval(1L);
        });

        assertTrue(exception.getMessage().contains("草稿状态"));
        verify(approvalMapper, never()).updateById(any(ApprovalFlow.class));
    }

    @Test
    void testApprove_Success() {
        // given - PENDING状态
        mockApproval.setStatus(ApprovalStatusEnum.PENDING.getCode());
        when(approvalMapper.selectById(1L)).thenReturn(mockApproval);
        when(approvalMapper.updateById(any(ApprovalFlow.class))).thenReturn(1);
        doNothing().when(historyService).recordOperation(anyLong(), any(OperationTypeEnum.class),
            any(), any(), anyString());

        ApprovalActionRequest request = new ApprovalActionRequest();
        request.setComment("审批通过");

        // when
        approvalService.approve(1L, request);

        // then
        ArgumentCaptor<ApprovalFlow> captor = ArgumentCaptor.forClass(ApprovalFlow.class);
        verify(approvalMapper, times(1)).updateById(captor.capture());

        ApprovalFlow approvedApproval = captor.getValue();
        assertEquals(ApprovalStatusEnum.APPROVED.getCode(), approvedApproval.getStatus());
        assertEquals("审批通过", approvedApproval.getApproveComment());
        assertNotNull(approvedApproval.getApproveTime());
    }

    @Test
    void testApprove_NotPendingStatus_ThrowException() {
        // given - APPROVED状态不能再审批
        mockApproval.setStatus(ApprovalStatusEnum.APPROVED.getCode());
        when(approvalMapper.selectById(1L)).thenReturn(mockApproval);

        ApprovalActionRequest request = new ApprovalActionRequest();
        request.setComment("审批通过");

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            approvalService.approve(1L, request);
        });

        assertTrue(exception.getMessage().contains("不允许审批"));
    }

    @Test
    void testReject_Success() {
        // given - PENDING状态
        mockApproval.setStatus(ApprovalStatusEnum.PENDING.getCode());
        when(approvalMapper.selectById(1L)).thenReturn(mockApproval);
        when(approvalMapper.updateById(any(ApprovalFlow.class))).thenReturn(1);
        doNothing().when(historyService).recordOperation(anyLong(), any(OperationTypeEnum.class),
            any(), any(), anyString());

        ApprovalActionRequest request = new ApprovalActionRequest();
        request.setComment("不符合规范");

        // when
        approvalService.reject(1L, request);

        // then
        ArgumentCaptor<ApprovalFlow> captor = ArgumentCaptor.forClass(ApprovalFlow.class);
        verify(approvalMapper, times(1)).updateById(captor.capture());

        ApprovalFlow rejectedApproval = captor.getValue();
        assertEquals(ApprovalStatusEnum.REJECTED.getCode(), rejectedApproval.getStatus());
        assertEquals("不符合规范", rejectedApproval.getApproveComment());
        verify(historyService, times(1)).recordOperation(anyLong(), eq(OperationTypeEnum.REJECT),
            isNull(), any(ApprovalFlow.class), contains("审批拒绝"));
    }

    @Test
    void testCancel_FromPendingStatus_Success() {
        // given - PENDING状态可以取消
        mockApproval.setStatus(ApprovalStatusEnum.PENDING.getCode());
        when(approvalMapper.selectById(1L)).thenReturn(mockApproval);
        when(approvalMapper.updateById(any(ApprovalFlow.class))).thenReturn(1);
        doNothing().when(historyService).recordOperation(anyLong(), any(OperationTypeEnum.class),
            any(), any(), anyString());

        // when
        approvalService.cancel(1L);

        // then
        ArgumentCaptor<ApprovalFlow> captor = ArgumentCaptor.forClass(ApprovalFlow.class);
        verify(approvalMapper, times(1)).updateById(captor.capture());

        ApprovalFlow cancelledApproval = captor.getValue();
        assertEquals(ApprovalStatusEnum.CANCELLED.getCode(), cancelledApproval.getStatus());
    }

    @Test
    void testCancel_FromApprovedStatus_Success() {
        // given - APPROVED状态可以取消
        mockApproval.setStatus(ApprovalStatusEnum.APPROVED.getCode());
        when(approvalMapper.selectById(1L)).thenReturn(mockApproval);
        when(approvalMapper.updateById(any(ApprovalFlow.class))).thenReturn(1);
        doNothing().when(historyService).recordOperation(anyLong(), any(OperationTypeEnum.class),
            any(), any(), anyString());

        // when
        approvalService.cancel(1L);

        // then
        ArgumentCaptor<ApprovalFlow> captor = ArgumentCaptor.forClass(ApprovalFlow.class);
        verify(approvalMapper, times(1)).updateById(captor.capture());

        ApprovalFlow cancelledApproval = captor.getValue();
        assertEquals(ApprovalStatusEnum.CANCELLED.getCode(), cancelledApproval.getStatus());
    }

    @Test
    void testCancel_FromRejectedStatus_ThrowException() {
        // given - REJECTED状态不能取消
        mockApproval.setStatus(ApprovalStatusEnum.REJECTED.getCode());
        when(approvalMapper.selectById(1L)).thenReturn(mockApproval);

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            approvalService.cancel(1L);
        });

        assertTrue(exception.getMessage().contains("不允许取消"));
    }

    @Test
    void testPublish_Success() {
        // given - APPROVED状态可以发布
        mockApproval.setStatus(ApprovalStatusEnum.APPROVED.getCode());
        when(approvalMapper.selectById(1L)).thenReturn(mockApproval);
        when(approvalMapper.updateById(any(ApprovalFlow.class))).thenReturn(1);
        doNothing().when(historyService).recordOperation(anyLong(), any(OperationTypeEnum.class),
            any(), any(), anyString());

        // when
        approvalService.publish(1L);

        // then
        ArgumentCaptor<ApprovalFlow> captor = ArgumentCaptor.forClass(ApprovalFlow.class);
        verify(approvalMapper, times(1)).updateById(captor.capture());

        ApprovalFlow publishedApproval = captor.getValue();
        assertEquals(ApprovalStatusEnum.PUBLISHED.getCode(), publishedApproval.getStatus());
        verify(historyService, times(1)).recordOperation(anyLong(), eq(OperationTypeEnum.PUBLISH),
            isNull(), any(ApprovalFlow.class), contains("发布变更"));
    }

    @Test
    void testPublish_NotApprovedStatus_ThrowException() {
        // given - PENDING状态不能发布
        mockApproval.setStatus(ApprovalStatusEnum.PENDING.getCode());
        when(approvalMapper.selectById(1L)).thenReturn(mockApproval);

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            approvalService.publish(1L);
        });

        assertTrue(exception.getMessage().contains("审批通过"));
    }

    @Test
    void testGetApprovalDetail_Success() {
        // given
        when(approvalMapper.selectById(1L)).thenReturn(mockApproval);

        // when
        ApprovalFlowVO result = approvalService.getApprovalDetail(1L);

        // then
        assertNotNull(result);
        assertEquals("APR-20260111-001001", result.getFlowNo());
        assertEquals(100L, result.getTableId());
    }

    @Test
    void testGetApprovalDetail_NotFound_ThrowException() {
        // given
        when(approvalMapper.selectById(999L)).thenReturn(null);

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            approvalService.getApprovalDetail(999L);
        });

        assertTrue(exception.getMessage().contains("不存在"));
    }

    @Test
    void testGetMySubmissions_Success() {
        // given
        Page<ApprovalFlow> page = new Page<>(1, 10);
        page.setRecords(Collections.singletonList(mockApproval));
        page.setTotal(1);

        when(approvalMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        // when
        PageResult<ApprovalFlowVO> result = approvalService.getMySubmissions("system", 1, 10);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
    }

    @Test
    void testGetPendingApprovals_Success() {
        // given
        mockApproval.setStatus(ApprovalStatusEnum.PENDING.getCode());
        Page<ApprovalFlow> page = new Page<>(1, 10);
        page.setRecords(Collections.singletonList(mockApproval));
        page.setTotal(1);

        when(approvalMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        // when
        PageResult<ApprovalFlowVO> result = approvalService.getPendingApprovals("approver", 1, 10);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertTrue(result.getRecords().get(0).getStatus().equals(ApprovalStatusEnum.PENDING.getCode()));
    }

    @Test
    void testGetAllApprovals_Success() {
        // given
        Page<ApprovalFlow> page = new Page<>(1, 10);
        page.setRecords(Collections.singletonList(mockApproval));
        page.setTotal(1);

        when(approvalMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        // when
        PageResult<ApprovalFlowVO> result = approvalService.getAllApprovals(1, 10);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
    }
}
