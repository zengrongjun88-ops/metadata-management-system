package com.datawarehouse.metadata.controller;

import com.datawarehouse.metadata.dto.request.TableCreateRequest;
import com.datawarehouse.metadata.dto.request.TableSearchRequest;
import com.datawarehouse.metadata.dto.request.TableUpdateRequest;
import com.datawarehouse.metadata.service.IMetadataTableService;
import com.datawarehouse.metadata.vo.MetadataTableVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MetadataTableController MockMvc测试
 *
 * @author System
 * @since 1.0.0
 */
@WebMvcTest(MetadataTableController.class)
@DisplayName("元数据表Controller测试")
class MetadataTableControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IMetadataTableService tableService;

    @Test
    @DisplayName("POST /api/metadata/tables - 创建表成功")
    void testCreateTable_Success() throws Exception {
        // given
        TableCreateRequest request = new TableCreateRequest();
        request.setDataSource("Hive");
        request.setDatabaseName("test_db");
        request.setTableName("test_table");
        request.setTableComment("测试表");
        request.setWarehouseLayer("dwd");
        request.setThemeFirst("usr");
        request.setThemeSecond("mem");
        request.setSensitivityLevel("L1");
        request.setImportanceLevel("P1");
        request.setOwner("admin");
        request.setFields(new ArrayList<>());

        when(tableService.createTable(any(TableCreateRequest.class))).thenReturn(1L);

        // when & then
        mockMvc.perform(post("/api/metadata/tables")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("创建成功"))
                .andExpect(jsonPath("$.data").value(1));

        verify(tableService, times(1)).createTable(any(TableCreateRequest.class));
    }

    @Test
    @DisplayName("GET /api/metadata/tables/{id} - 获取表详情成功")
    void testGetTableById_Success() throws Exception {
        // given
        MetadataTableVO tableVO = new MetadataTableVO();
        tableVO.setId(1L);
        tableVO.setDatabaseName("test_db");
        tableVO.setTableName("test_table");
        tableVO.setTableComment("测试表");
        tableVO.setCreateTime(LocalDateTime.now());
        tableVO.setFields(new ArrayList<>());

        when(tableService.getTableById(1L)).thenReturn(tableVO);

        // when & then
        mockMvc.perform(get("/api/metadata/tables/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.databaseName").value("test_db"))
                .andExpect(jsonPath("$.data.tableName").value("test_table"));

        verify(tableService, times(1)).getTableById(1L);
    }

    @Test
    @DisplayName("GET /api/metadata/tables/name - 按名称查询表成功")
    void testGetTableByName_Success() throws Exception {
        // given
        MetadataTableVO tableVO = new MetadataTableVO();
        tableVO.setId(1L);
        tableVO.setDatabaseName("test_db");
        tableVO.setTableName("test_table");
        tableVO.setFields(new ArrayList<>());

        when(tableService.getTableByName("test_db", "test_table")).thenReturn(tableVO);

        // when & then
        mockMvc.perform(get("/api/metadata/tables/name")
                .param("databaseName", "test_db")
                .param("tableName", "test_table"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(tableService, times(1)).getTableByName("test_db", "test_table");
    }

    @Test
    @DisplayName("PUT /api/metadata/tables/{id} - 更新表成功")
    void testUpdateTable_Success() throws Exception {
        // given
        TableUpdateRequest request = new TableUpdateRequest();
        request.setId(1L);
        request.setTableComment("更新后的表注释");
        request.setOwner("new_owner");
        request.setFields(new ArrayList<>());

        doNothing().when(tableService).updateTable(any(TableUpdateRequest.class));

        // when & then
        mockMvc.perform(put("/api/metadata/tables/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("更新成功"));

        verify(tableService, times(1)).updateTable(any(TableUpdateRequest.class));
    }

    @Test
    @DisplayName("DELETE /api/metadata/tables/{id} - 删除表成功")
    void testDeleteTable_Success() throws Exception {
        // given
        doNothing().when(tableService).deleteTable(1L);

        // when & then
        mockMvc.perform(delete("/api/metadata/tables/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("删除成功"));

        verify(tableService, times(1)).deleteTable(1L);
    }

    @Test
    @DisplayName("POST /api/metadata/tables/generate-sql - 生成SQL成功")
    void testGenerateSql_Success() throws Exception {
        // given
        TableCreateRequest request = new TableCreateRequest();
        request.setDataSource("Hive");
        request.setDatabaseName("test_db");
        request.setTableName("test_table");
        request.setTableComment("测试表");
        request.setFields(new ArrayList<>());

        String expectedSql = "CREATE TABLE IF NOT EXISTS test_db.test_table (\n" +
                "  id BIGINT COMMENT '主键'\n" +
                ")\n" +
                "STORED AS PARQUET;";

        when(tableService.generateCreateSql(any(TableCreateRequest.class))).thenReturn(expectedSql);

        // when & then
        mockMvc.perform(post("/api/metadata/tables/generate-sql")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(expectedSql));

        verify(tableService, times(1)).generateCreateSql(any(TableCreateRequest.class));
    }

    @Test
    @DisplayName("POST /api/metadata/tables/validate-sql - 校验SQL成功")
    void testValidateSql_Success() throws Exception {
        // given
        doNothing().when(tableService).validateSql(anyString(), anyString());

        String requestBody = "{\"sql\":\"CREATE TABLE test (id BIGINT)\",\"dataSource\":\"Hive\"}";

        // when & then
        mockMvc.perform(post("/api/metadata/tables/validate-sql")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("SQL校验通过"));

        verify(tableService, times(1)).validateSql(anyString(), anyString());
    }

    @Test
    @DisplayName("POST /api/metadata/tables/validate-sql - 校验SQL失败")
    void testValidateSql_Failure() throws Exception {
        // given
        doThrow(new IllegalArgumentException("禁止执行DROP DATABASE操作"))
                .when(tableService).validateSql(anyString(), anyString());

        String requestBody = "{\"sql\":\"DROP DATABASE test_db\",\"dataSource\":\"Hive\"}";

        // when & then
        mockMvc.perform(post("/api/metadata/tables/validate-sql")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));

        verify(tableService, times(1)).validateSql(anyString(), anyString());
    }
}
