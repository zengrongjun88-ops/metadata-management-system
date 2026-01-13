package com.datawarehouse.metadata.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.datawarehouse.metadata.common.PageResult;
import com.datawarehouse.metadata.dto.request.FieldCreateRequest;
import com.datawarehouse.metadata.dto.request.TableCreateRequest;
import com.datawarehouse.metadata.dto.request.TableSearchRequest;
import com.datawarehouse.metadata.dto.request.TableUpdateRequest;
import com.datawarehouse.metadata.entity.MetadataField;
import com.datawarehouse.metadata.entity.MetadataTable;
import com.datawarehouse.metadata.enums.DataSourceTypeEnum;
import com.datawarehouse.metadata.enums.OperationTypeEnum;
import com.datawarehouse.metadata.exception.BusinessException;
import com.datawarehouse.metadata.mapper.MetadataTableMapper;
import com.datawarehouse.metadata.service.impl.MetadataTableServiceImpl;
import com.datawarehouse.metadata.strategy.SqlGeneratorFactory;
import com.datawarehouse.metadata.strategy.impl.HiveSqlGenerator;
import com.datawarehouse.metadata.vo.MetadataTableVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * MetadataTableService单元测试
 *
 * @author System
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class MetadataTableServiceTest {

    @Mock
    private MetadataTableMapper tableMapper;

    @Mock
    private IMetadataFieldService fieldService;

    @Mock
    private IOperationHistoryService historyService;

    @Mock
    private SqlGeneratorFactory sqlGeneratorFactory;

    @Mock
    private HiveSqlGenerator hiveSqlGenerator;

    @InjectMocks
    private MetadataTableServiceImpl tableService;

    private TableCreateRequest createRequest;
    private MetadataTable mockTable;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        createRequest = new TableCreateRequest();
        createRequest.setDataSource(DataSourceTypeEnum.HIVE.getCode());
        createRequest.setDatabaseName("test_db");
        createRequest.setTableName("test_table");
        createRequest.setTableComment("测试表");
        createRequest.setWarehouseLayer("DWD");
        createRequest.setThemeFirst("usr");
        createRequest.setThemeSecond("mem");
        createRequest.setSensitivityLevel("L1");
        createRequest.setImportanceLevel("P1");
        createRequest.setPartitionType("DAILY");
        createRequest.setUpdateFrequency("DAILY");
        createRequest.setOwner("test_user");

        // 创建字段列表
        List<FieldCreateRequest> fields = new ArrayList<>();
        FieldCreateRequest field1 = new FieldCreateRequest();
        field1.setFieldName("id");
        field1.setFieldType("BIGINT");
        field1.setFieldComment("主键ID");
        field1.setFieldOrder(1);
        field1.setIsPrimaryKey(1);
        field1.setIsNullable(0);
        fields.add(field1);

        FieldCreateRequest field2 = new FieldCreateRequest();
        field2.setFieldName("name");
        field2.setFieldType("STRING");
        field2.setFieldComment("姓名");
        field2.setFieldOrder(2);
        field2.setIsPrimaryKey(0);
        field2.setIsNullable(1);
        fields.add(field2);

        createRequest.setFields(fields);

        // 准备Mock实体
        mockTable = new MetadataTable();
        mockTable.setId(1L);
        mockTable.setDataSource(DataSourceTypeEnum.HIVE.getCode());
        mockTable.setDatabaseName("test_db");
        mockTable.setTableName("test_table");
        mockTable.setTableComment("测试表");
        mockTable.setCreateTime(LocalDateTime.now());
        mockTable.setDeleted(0);
    }

    @Test
    void testCreateTable_Success() {
        // given
        when(tableMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null); // 表不存在
        when(sqlGeneratorFactory.getGenerator(DataSourceTypeEnum.HIVE.getCode())).thenReturn(hiveSqlGenerator);
        when(hiveSqlGenerator.generateCreateTableSql(any(TableCreateRequest.class)))
            .thenReturn("CREATE TABLE test_db.test_table ...");

        doAnswer(invocation -> {
            MetadataTable table = invocation.getArgument(0);
            table.setId(1L);
            return true;
        }).when(tableMapper).insert(any(MetadataTable.class));

        doNothing().when(fieldService).batchCreateFields(anyLong(), anyList());
        doNothing().when(historyService).recordOperation(anyLong(), any(OperationTypeEnum.class),
            any(), any(), anyString());

        // when
        Long tableId = tableService.createTable(createRequest);

        // then
        assertNotNull(tableId);
        assertEquals(1L, tableId);

        // 验证方法调用
        verify(tableMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
        verify(tableMapper, times(1)).insert(any(MetadataTable.class));
        verify(fieldService, times(1)).batchCreateFields(eq(1L), anyList());
        verify(historyService, times(1)).recordOperation(eq(1L), eq(OperationTypeEnum.CREATE),
            isNull(), any(MetadataTable.class), contains("创建表"));
    }

    @Test
    void testCreateTable_DuplicateName_ThrowException() {
        // given
        when(tableMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(mockTable); // 表已存在

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            tableService.createTable(createRequest);
        });

        assertTrue(exception.getMessage().contains("已存在"));
        verify(tableMapper, never()).insert(any(MetadataTable.class));
    }

    @Test
    void testGetTableById_Success() {
        // given
        when(tableMapper.selectById(1L)).thenReturn(mockTable);
        when(fieldService.getFieldsByTableId(1L)).thenReturn(Collections.emptyList());

        // when
        MetadataTableVO result = tableService.getTableById(1L);

        // then
        assertNotNull(result);
        assertEquals("test_table", result.getTableName());
        assertEquals("test_db", result.getDatabaseName());
        verify(tableMapper, times(1)).selectById(1L);
        verify(fieldService, times(1)).getFieldsByTableId(1L);
    }

    @Test
    void testGetTableById_NotFound_ThrowException() {
        // given
        when(tableMapper.selectById(999L)).thenReturn(null);

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            tableService.getTableById(999L);
        });

        assertTrue(exception.getMessage().contains("不存在"));
    }

    @Test
    void testGetTableByName_Success() {
        // given
        when(tableMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(mockTable);
        when(fieldService.getFieldsByTableId(1L)).thenReturn(Collections.emptyList());

        // when
        MetadataTableVO result = tableService.getTableByName("test_db", "test_table");

        // then
        assertNotNull(result);
        assertEquals("test_table", result.getTableName());
        verify(tableMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    void testUpdateTable_Success() {
        // given
        TableUpdateRequest updateRequest = new TableUpdateRequest();
        updateRequest.setId(1L);
        updateRequest.setTableComment("更新后的表注释");
        updateRequest.setOwner("new_owner");
        updateRequest.setFields(Collections.emptyList());

        MetadataTable existingTable = new MetadataTable();
        existingTable.setId(1L);
        existingTable.setTableComment("旧注释");
        existingTable.setOwner("old_owner");

        when(tableMapper.selectById(1L)).thenReturn(existingTable);
        when(tableMapper.updateById(any(MetadataTable.class))).thenReturn(1);
        when(fieldService.getFieldsByTableId(1L)).thenReturn(Collections.emptyList());
        doNothing().when(historyService).recordOperation(anyLong(), any(OperationTypeEnum.class),
            any(), any(), anyString());

        // when
        tableService.updateTable(updateRequest);

        // then
        ArgumentCaptor<MetadataTable> captor = ArgumentCaptor.forClass(MetadataTable.class);
        verify(tableMapper, times(1)).updateById(captor.capture());

        MetadataTable updatedTable = captor.getValue();
        assertEquals("更新后的表注释", updatedTable.getTableComment());
        assertEquals("new_owner", updatedTable.getOwner());

        verify(historyService, times(1)).recordOperation(eq(1L), eq(OperationTypeEnum.UPDATE),
            any(MetadataTable.class), any(MetadataTable.class), contains("更新表"));
    }

    @Test
    void testDeleteTable_Success() {
        // given
        when(tableMapper.selectById(1L)).thenReturn(mockTable);
        when(tableMapper.updateById(any(MetadataTable.class))).thenReturn(1);
        doNothing().when(fieldService).deleteFieldsByTableId(1L);
        doNothing().when(historyService).recordOperation(anyLong(), any(OperationTypeEnum.class),
            any(), any(), anyString());

        // when
        tableService.deleteTable(1L);

        // then
        ArgumentCaptor<MetadataTable> captor = ArgumentCaptor.forClass(MetadataTable.class);
        verify(tableMapper, times(1)).updateById(captor.capture());

        MetadataTable deletedTable = captor.getValue();
        assertEquals(1, deletedTable.getDeleted());

        verify(fieldService, times(1)).deleteFieldsByTableId(1L);
        verify(historyService, times(1)).recordOperation(eq(1L), eq(OperationTypeEnum.DELETE),
            any(MetadataTable.class), isNull(), contains("删除表"));
    }

    @Test
    void testPageQuery_Success() {
        // given
        TableSearchRequest searchRequest = new TableSearchRequest();
        searchRequest.setPageNum(1);
        searchRequest.setPageSize(10);
        searchRequest.setDatabaseName("test_db");

        Page<MetadataTable> page = new Page<>(1, 10);
        page.setRecords(Collections.singletonList(mockTable));
        page.setTotal(1);

        when(tableMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        // when
        PageResult<MetadataTableVO> result = tableService.pageQuery(searchRequest);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        verify(tableMapper, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    void testGenerateCreateSql_Success() {
        // given
        when(sqlGeneratorFactory.getGenerator(DataSourceTypeEnum.HIVE.getCode())).thenReturn(hiveSqlGenerator);
        when(hiveSqlGenerator.generateCreateTableSql(createRequest))
            .thenReturn("CREATE TABLE test_db.test_table (id BIGINT, name STRING)");

        // when
        String sql = tableService.generateCreateSql(createRequest);

        // then
        assertNotNull(sql);
        assertTrue(sql.contains("CREATE TABLE"));
        assertTrue(sql.contains("test_table"));
        verify(sqlGeneratorFactory, times(1)).getGenerator(DataSourceTypeEnum.HIVE.getCode());
        verify(hiveSqlGenerator, times(1)).generateCreateTableSql(createRequest);
    }

    @Test
    void testValidateSql_Success() {
        // given
        String sql = "CREATE TABLE test_db.test_table (id BIGINT)";
        when(sqlGeneratorFactory.getGenerator(DataSourceTypeEnum.HIVE.getCode())).thenReturn(hiveSqlGenerator);
        doNothing().when(hiveSqlGenerator).validateSql(sql);

        // when & then
        assertDoesNotThrow(() -> {
            tableService.validateSql(sql, DataSourceTypeEnum.HIVE.getCode());
        });

        verify(hiveSqlGenerator, times(1)).validateSql(sql);
    }

    @Test
    void testValidateSql_InvalidSql_ThrowException() {
        // given
        String dangerousSql = "DROP DATABASE test_db";
        when(sqlGeneratorFactory.getGenerator(DataSourceTypeEnum.HIVE.getCode())).thenReturn(hiveSqlGenerator);
        doThrow(new BusinessException("禁止执行DROP DATABASE操作"))
            .when(hiveSqlGenerator).validateSql(dangerousSql);

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            tableService.validateSql(dangerousSql, DataSourceTypeEnum.HIVE.getCode());
        });

        assertTrue(exception.getMessage().contains("DROP DATABASE"));
    }
}
