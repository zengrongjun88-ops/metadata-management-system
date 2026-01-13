package com.datawarehouse.metadata.integration;

import com.datawarehouse.metadata.BaseIntegrationTest;
import com.datawarehouse.metadata.common.PageResult;
import com.datawarehouse.metadata.dto.request.*;
import com.datawarehouse.metadata.enums.ApprovalStatusEnum;
import com.datawarehouse.metadata.service.IApprovalService;
import com.datawarehouse.metadata.service.IMetadataFieldService;
import com.datawarehouse.metadata.service.IMetadataTableService;
import com.datawarehouse.metadata.service.IOperationHistoryService;
import com.datawarehouse.metadata.vo.ApprovalFlowVO;
import com.datawarehouse.metadata.vo.MetadataFieldVO;
import com.datawarehouse.metadata.vo.MetadataTableVO;
import com.datawarehouse.metadata.vo.OperationHistoryVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 元数据管理完整流程集成测试
 * 测试场景: 创建表 → 审批 → 发布 → 查询 → 更新 → 删除
 *
 * @author System
 * @since 1.0.0
 */
@DisplayName("元数据管理完整流程集成测试")
class MetadataManagementIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private IMetadataTableService tableService;

    @Autowired
    private IMetadataFieldService fieldService;

    @Autowired
    private IApprovalService approvalService;

    @Autowired
    private IOperationHistoryService historyService;

    @Test
    @DisplayName("完整流程: 创建表->查询->更新->删除")
    void testFullWorkflow_CreateQueryUpdateDelete() {
        // ========== 1. 创建表 ==========
        TableCreateRequest createRequest = buildTableCreateRequest();

        Long tableId = tableService.createTable(createRequest);
        assertNotNull(tableId, "表ID不应为空");
        assertTrue(tableId > 0, "表ID应大于0");

        // ========== 2. 查询表详情 ==========
        MetadataTableVO tableVO = tableService.getTableById(tableId);
        assertNotNull(tableVO, "查询的表不应为空");
        assertEquals("integration_test_db", tableVO.getDatabaseName());
        assertEquals("test_user_table", tableVO.getTableName());
        assertEquals("集成测试用户表", tableVO.getTableComment());
        assertNotNull(tableVO.getFields(), "字段列表不应为空");
        assertEquals(3, tableVO.getFields().size(), "应该有3个字段");

        // ========== 3. 验证字段 ==========
        List<MetadataFieldVO> fields = tableVO.getFields();
        assertEquals("user_id", fields.get(0).getFieldName());
        assertEquals("BIGINT", fields.get(0).getFieldType());
        assertEquals(1, fields.get(0).getIsPrimaryKey());

        assertEquals("user_name", fields.get(1).getFieldName());
        assertEquals("STRING", fields.get(1).getFieldType());

        assertEquals("created_at", fields.get(2).getFieldName());
        assertEquals("TIMESTAMP", fields.get(2).getFieldType());

        // ========== 4. 按名称查询表 ==========
        MetadataTableVO tableByName = tableService.getTableByName("integration_test_db", "test_user_table");
        assertNotNull(tableByName);
        assertEquals(tableId, tableByName.getId());

        // ========== 5. 分页查询 ==========
        TableSearchRequest searchRequest = new TableSearchRequest();
        searchRequest.setDatabaseName("integration_test_db");
        searchRequest.setPageNum(1);
        searchRequest.setPageSize(10);

        PageResult<MetadataTableVO> pageResult = tableService.pageQuery(searchRequest);
        assertNotNull(pageResult);
        assertTrue(pageResult.getTotal() > 0);
        assertTrue(pageResult.getRecords().size() > 0);

        // ========== 6. 更新表 ==========
        TableUpdateRequest updateRequest = new TableUpdateRequest();
        updateRequest.setId(tableId);
        updateRequest.setTableComment("更新后的表注释");
        updateRequest.setOwner("new_owner");
        updateRequest.setFields(new ArrayList<>()); // 不更新字段

        tableService.updateTable(updateRequest);

        // 验证更新
        MetadataTableVO updatedTable = tableService.getTableById(tableId);
        assertEquals("更新后的表注释", updatedTable.getTableComment());

        // ========== 7. 查询操作历史 ==========
        PageResult<OperationHistoryVO> historyPage = historyService.getHistory(tableId, 1, 10);
        assertNotNull(historyPage);
        System.out.println("实际历史记录数量: " + historyPage.getTotal());
        System.out.println("历史记录列表: " + historyPage.getRecords());
        assertTrue(historyPage.getTotal() >= 2, "至少应该有创建和更新两条历史记录，实际数量: " + historyPage.getTotal());

        // ========== 8. 删除表 ==========
        tableService.deleteTable(tableId);

        // 验证软删除 - 应该抛出异常
        assertThrows(Exception.class, () -> {
            tableService.getTableById(tableId);
        });
    }

    @Test
    @DisplayName("审批流程: 创建审批单->提交->审批通过->发布")
    void testApprovalWorkflow_SubmitApprovePublish() {
        // ========== 1. 创建表(用于审批) ==========
        TableCreateRequest createRequest = buildTableCreateRequest();
        Long tableId = tableService.createTable(createRequest);

        // ========== 2. 创建审批单 ==========
        ApprovalRequest approvalRequest = new ApprovalRequest();
        approvalRequest.setTableId(tableId);
        approvalRequest.setApprovalType("UPDATE");
        approvalRequest.setChangeContent("{\"operation\":\"update_table\",\"changes\":[\"add_field\"]}");

        Long approvalId = approvalService.createApproval(approvalRequest);
        assertNotNull(approvalId);

        // ========== 3. 查询审批详情 ==========
        ApprovalFlowVO approval = approvalService.getApprovalDetail(approvalId);
        assertNotNull(approval);
        assertEquals(ApprovalStatusEnum.DRAFT.getCode(), approval.getStatus());
        assertTrue(approval.getFlowNo().startsWith("APR-"));

        // ========== 4. 提交审批 ==========
        approvalService.submitApproval(approvalId);

        approval = approvalService.getApprovalDetail(approvalId);
        assertEquals(ApprovalStatusEnum.PENDING.getCode(), approval.getStatus());

        // ========== 5. 审批通过 ==========
        ApprovalActionRequest approveRequest = new ApprovalActionRequest();
        approveRequest.setComment("审批通过，可以发布");

        approvalService.approve(approvalId, approveRequest);

        approval = approvalService.getApprovalDetail(approvalId);
        assertEquals(ApprovalStatusEnum.APPROVED.getCode(), approval.getStatus());
        assertEquals("审批通过，可以发布", approval.getApproveComment());

        // ========== 6. 发布变更 ==========
        approvalService.publish(approvalId);

        approval = approvalService.getApprovalDetail(approvalId);
        assertEquals(ApprovalStatusEnum.PUBLISHED.getCode(), approval.getStatus());

        // ========== 7. 查询我提交的审批单 ==========
        PageResult<ApprovalFlowVO> mySubmissions = approvalService.getMySubmissions("system", 1, 10);
        assertNotNull(mySubmissions);
        assertTrue(mySubmissions.getTotal() > 0);

        // ========== 8. 查询待审批的审批单 ==========
        PageResult<ApprovalFlowVO> pendingApprovals = approvalService.getPendingApprovals("approver", 1, 10);
        assertNotNull(pendingApprovals);
        // 注意: 因为我们已经审批通过了，所以待审批列表应该不包含这个审批单
    }

    @Test
    @DisplayName("审批流程: 创建审批单->提交->拒绝")
    void testApprovalWorkflow_SubmitReject() {
        // ========== 1. 创建表 ==========
        TableCreateRequest createRequest = buildTableCreateRequest();
        Long tableId = tableService.createTable(createRequest);

        // ========== 2. 创建并提交审批单 ==========
        ApprovalRequest approvalRequest = new ApprovalRequest();
        approvalRequest.setTableId(tableId);
        approvalRequest.setApprovalType("DELETE");
        approvalRequest.setChangeContent("{\"operation\":\"delete_table\"}");

        Long approvalId = approvalService.createApproval(approvalRequest);
        approvalService.submitApproval(approvalId);

        // ========== 3. 拒绝审批 ==========
        ApprovalActionRequest rejectRequest = new ApprovalActionRequest();
        rejectRequest.setComment("不符合删除规范，拒绝");

        approvalService.reject(approvalId, rejectRequest);

        // ========== 4. 验证状态 ==========
        ApprovalFlowVO approval = approvalService.getApprovalDetail(approvalId);
        assertEquals(ApprovalStatusEnum.REJECTED.getCode(), approval.getStatus());
        assertEquals("不符合删除规范，拒绝", approval.getApproveComment());
    }

    @Test
    @DisplayName("审批流程: 创建审批单->提交->取消")
    void testApprovalWorkflow_SubmitCancel() {
        // ========== 1. 创建表 ==========
        TableCreateRequest createRequest = buildTableCreateRequest();
        Long tableId = tableService.createTable(createRequest);

        // ========== 2. 创建并提交审批单 ==========
        ApprovalRequest approvalRequest = new ApprovalRequest();
        approvalRequest.setTableId(tableId);
        approvalRequest.setApprovalType("UPDATE");
        approvalRequest.setChangeContent("{\"operation\":\"update_table\"}");

        Long approvalId = approvalService.createApproval(approvalRequest);
        approvalService.submitApproval(approvalId);

        // ========== 3. 取消审批 ==========
        approvalService.cancel(approvalId);

        // ========== 4. 验证状态 ==========
        ApprovalFlowVO approval = approvalService.getApprovalDetail(approvalId);
        assertEquals(ApprovalStatusEnum.CANCELLED.getCode(), approval.getStatus());
    }

    @Test
    @DisplayName("SQL生成器: 生成并校验Hive建表SQL")
    void testSqlGenerator_HiveCreateTableSql() {
        // ========== 1. 构建建表请求 ==========
        TableCreateRequest request = buildTableCreateRequest();

        // ========== 2. 生成SQL ==========
        String sql = tableService.generateCreateSql(request);

        // ========== 3. 验证SQL内容 ==========
        assertNotNull(sql);
        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS"));
        assertTrue(sql.contains("integration_test_db.test_user_table"));
        assertTrue(sql.contains("user_id BIGINT COMMENT '用户ID'"));
        assertTrue(sql.contains("user_name STRING COMMENT '用户姓名'"));
        assertTrue(sql.contains("COMMENT '集成测试用户表'"));
        assertTrue(sql.contains("STORED AS PARQUET"));

        // ========== 4. 校验SQL ==========
        assertDoesNotThrow(() -> {
            tableService.validateSql(sql, "Hive");
        });

        // ========== 5. 校验危险SQL会被拦截 ==========
        assertThrows(Exception.class, () -> {
            tableService.validateSql("DROP DATABASE test_db", "Hive");
        });
    }

    @Test
    @DisplayName("字段管理: 批量创建字段")
    void testFieldManagement_BatchCreate() {
        // ========== 1. 创建表(不带字段) ==========
        TableCreateRequest createRequest = buildTableCreateRequest();
        createRequest.setFields(new ArrayList<>()); // 清空字段
        Long tableId = tableService.createTable(createRequest);

        // ========== 2. 批量添加字段 ==========
        List<FieldCreateRequest> newFields = new ArrayList<>();

        FieldCreateRequest field1 = new FieldCreateRequest();
        field1.setFieldName("new_field1");
        field1.setFieldType("STRING");
        field1.setFieldComment("新字段1");
        field1.setFieldOrder(1);
        newFields.add(field1);

        FieldCreateRequest field2 = new FieldCreateRequest();
        field2.setFieldName("new_field2");
        field2.setFieldType("INT");
        field2.setFieldComment("新字段2");
        field2.setFieldOrder(2);
        newFields.add(field2);

        fieldService.batchCreateFields(tableId, newFields);

        // ========== 3. 验证字段已添加 ==========
        List<MetadataFieldVO> fields = fieldService.getFieldsByTableId(tableId);
        assertEquals(2, fields.size());
        assertEquals("new_field1", fields.get(0).getFieldName());
        assertEquals("new_field2", fields.get(1).getFieldName());
    }

    /**
     * 构建测试用的建表请求
     */
    private TableCreateRequest buildTableCreateRequest() {
        TableCreateRequest request = new TableCreateRequest();
        request.setDataSource("Hive");
        request.setDatabaseName("integration_test_db");
        request.setTableName("test_user_table");
        request.setTableComment("集成测试用户表");
        request.setWarehouseLayer("dwd");
        request.setThemeFirst("usr");
        request.setThemeSecond("mem");
        request.setSensitivityLevel("L1");
        request.setImportanceLevel("P1");
        request.setPartitionType("NONE");
        request.setUpdateFrequency("DAILY");
        request.setOwner("test_owner");

        // 添加字段
        List<FieldCreateRequest> fields = new ArrayList<>();

        FieldCreateRequest field1 = new FieldCreateRequest();
        field1.setFieldName("user_id");
        field1.setFieldType("BIGINT");
        field1.setFieldComment("用户ID");
        field1.setFieldOrder(1);
        field1.setIsPrimaryKey(1);
        field1.setIsNullable(0);
        fields.add(field1);

        FieldCreateRequest field2 = new FieldCreateRequest();
        field2.setFieldName("user_name");
        field2.setFieldType("STRING");
        field2.setFieldComment("用户姓名");
        field2.setFieldOrder(2);
        field2.setIsPrimaryKey(0);
        field2.setIsNullable(1);
        fields.add(field2);

        FieldCreateRequest field3 = new FieldCreateRequest();
        field3.setFieldName("created_at");
        field3.setFieldType("TIMESTAMP");
        field3.setFieldComment("创建时间");
        field3.setFieldOrder(3);
        field3.setIsPrimaryKey(0);
        field3.setIsNullable(0);
        fields.add(field3);

        request.setFields(fields);

        return request;
    }
}
