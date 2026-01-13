package com.datawarehouse.metadata.strategy;

import com.datawarehouse.metadata.dto.request.FieldCreateRequest;
import com.datawarehouse.metadata.dto.request.TableCreateRequest;
import com.datawarehouse.metadata.exception.BusinessException;
import com.datawarehouse.metadata.strategy.impl.HiveSqlGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HiveSqlGenerator单元测试
 *
 * @author System
 * @since 1.0.0
 */
class HiveSqlGeneratorTest {

    private HiveSqlGenerator sqlGenerator;
    private TableCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        sqlGenerator = new HiveSqlGenerator();

        // 准备测试数据
        createRequest = new TableCreateRequest();
        createRequest.setDatabaseName("test_db");
        createRequest.setTableName("user_info");
        createRequest.setTableComment("用户信息表");
        createRequest.setPartitionType("DAILY");

        // 创建字段列表
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
        field3.setFieldName("age");
        field3.setFieldType("INT");
        field3.setFieldComment("年龄");
        field3.setFieldOrder(3);
        field3.setIsPrimaryKey(0);
        field3.setIsNullable(1);
        fields.add(field3);

        createRequest.setFields(fields);
    }

    @Test
    void testGenerateCreateTableSql_WithPartition_Success() {
        // 添加分区字段
        FieldCreateRequest partitionField = new FieldCreateRequest();
        partitionField.setFieldName("dt");
        partitionField.setFieldType("STRING");
        partitionField.setFieldComment("分区字段");
        partitionField.setFieldOrder(4);
        partitionField.setIsPartitionKey(1);
        createRequest.getFields().add(partitionField);

        // when
        String sql = sqlGenerator.generateCreateTableSql(createRequest);

        // then
        assertNotNull(sql);
        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS test_db.user_info"));
        assertTrue(sql.contains("user_id BIGINT COMMENT '用户ID'"));
        assertTrue(sql.contains("user_name STRING COMMENT '用户姓名'"));
        assertTrue(sql.contains("age INT COMMENT '年龄'"));
        assertTrue(sql.contains("COMMENT '用户信息表'"));
        assertTrue(sql.contains("PARTITIONED BY (dt STRING)"));
        assertTrue(sql.contains("STORED AS PARQUET"));
    }

    @Test
    void testGenerateCreateTableSql_WithoutPartition_Success() {
        // given - 默认没有分区字段

        // when
        String sql = sqlGenerator.generateCreateTableSql(createRequest);

        // then
        assertNotNull(sql);
        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS"));
        assertFalse(sql.contains("PARTITIONED BY"));
    }

    @Test
    void testGenerateCreateTableSql_EmptyFields_GenerateEmptyColumns() {
        // given
        createRequest.getFields().clear();

        // when
        String sql = sqlGenerator.generateCreateTableSql(createRequest);

        // then - 允许创建空表
        assertNotNull(sql);
        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS"));
    }

    @Test
    void testGenerateCreateTableSql_FieldsOrderedCorrectly() {
        // when
        String sql = sqlGenerator.generateCreateTableSql(createRequest);

        // then
        int userIdIndex = sql.indexOf("user_id");
        int userNameIndex = sql.indexOf("user_name");
        int ageIndex = sql.indexOf("age");

        assertTrue(userIdIndex > 0);
        assertTrue(userNameIndex > userIdIndex);
        assertTrue(ageIndex > userNameIndex);
    }

    @Test
    void testValidateSql_ValidSql_Success() {
        // given
        String validSql = "CREATE TABLE test_db.test_table (id BIGINT, name STRING)";

        // when & then
        assertDoesNotThrow(() -> {
            sqlGenerator.validateSql(validSql);
        });
    }

    @Test
    void testValidateSql_EmptySql_ThrowException() {
        // given
        String emptySql = "";

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            sqlGenerator.validateSql(emptySql);
        });

        assertTrue(exception.getMessage().contains("SQL不能为空"));
    }

    @Test
    void testValidateSql_DropDatabaseSql_ThrowException() {
        // given
        String dangerousSql = "DROP DATABASE test_db";

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            sqlGenerator.validateSql(dangerousSql);
        });

        assertTrue(exception.getMessage().contains("DROP DATABASE"));
    }

    @Test
    void testValidateSql_DropTableSql_Success() {
        // given - DROP TABLE是允许的
        String dropTableSql = "DROP TABLE test_db.test_table";

        // when & then
        assertDoesNotThrow(() -> {
            sqlGenerator.validateSql(dropTableSql);
        });
    }

    @Test
    void testValidateSql_TruncateTableSql_ThrowException() {
        // given
        String dangerousSql = "TRUNCATE TABLE test_db.test_table";

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            sqlGenerator.validateSql(dangerousSql);
        });

        assertTrue(exception.getMessage().contains("仅支持DDL"));
    }

    @Test
    void testValidateSql_CaseInsensitive_ThrowException() {
        // given
        String dangerousSql = "drop database test_db";

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            sqlGenerator.validateSql(dangerousSql);
        });

        assertTrue(exception.getMessage().contains("DROP DATABASE"));
    }

    @Test
    void testGenerateAlterTableSql_ReturnHint() {
        // when
        String sql = sqlGenerator.generateAlterTableSql(null);

        // then - 返回提示信息
        assertNotNull(sql);
        assertTrue(sql.contains("Hive不支持ALTER TABLE"));
    }

    @Test
    void testGenerateDropTableSql_Success() {
        // when
        String sql = sqlGenerator.generateDropTableSql("test_db", "test_table");

        // then
        assertNotNull(sql);
        assertTrue(sql.contains("DROP TABLE IF EXISTS test_db.test_table"));
    }

    @Test
    void testExecuteDdl_NotImplemented() {
        // when & then
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> {
            sqlGenerator.executeDdl("CREATE TABLE test (id BIGINT)");
        });

        assertTrue(exception.getMessage().contains("待实现"));
    }

    @Test
    void testGenerateCreateTableSql_WithSpecialCharacters_Success() {
        // given
        createRequest.setTableComment("包含'特殊'字符的\"注释\"");

        // when
        String sql = sqlGenerator.generateCreateTableSql(createRequest);

        // then
        assertNotNull(sql);
        assertTrue(sql.contains("COMMENT"));
    }

    @Test
    void testGenerateCreateTableSql_MultipleDataTypes_Success() {
        // given
        List<FieldCreateRequest> fields = new ArrayList<>();

        String[] dataTypes = {"TINYINT", "SMALLINT", "INT", "BIGINT", "FLOAT", "DOUBLE",
                              "DECIMAL", "STRING", "VARCHAR", "CHAR", "BOOLEAN", "DATE",
                              "TIMESTAMP", "ARRAY", "MAP", "STRUCT"};

        for (int i = 0; i < dataTypes.length; i++) {
            FieldCreateRequest field = new FieldCreateRequest();
            field.setFieldName("field_" + i);
            field.setFieldType(dataTypes[i]);
            field.setFieldComment("字段" + i);
            field.setFieldOrder(i + 1);
            fields.add(field);
        }

        createRequest.setFields(fields);

        // when
        String sql = sqlGenerator.generateCreateTableSql(createRequest);

        // then
        assertNotNull(sql);
        for (String dataType : dataTypes) {
            assertTrue(sql.contains(dataType), "SQL should contain data type: " + dataType);
        }
    }
}
