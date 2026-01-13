package com.datawarehouse.metadata.strategy.impl;

import com.datawarehouse.metadata.dto.request.FieldCreateRequest;
import com.datawarehouse.metadata.dto.request.TableCreateRequest;
import com.datawarehouse.metadata.dto.request.TableUpdateRequest;
import com.datawarehouse.metadata.strategy.SqlGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * ClickHouse SQL生成器
 *
 * @author System
 * @since 1.0.0
 */
@Slf4j
@Component("clickhouseSqlGenerator")
public class ClickHouseSqlGenerator implements SqlGenerator {

    @Override
    public String generateCreateTableSql(TableCreateRequest request) {
        StringBuilder sql = new StringBuilder();

        // CREATE TABLE
        sql.append("CREATE TABLE IF NOT EXISTS ");
        sql.append(request.getDatabaseName()).append(".").append(request.getTableName());
        sql.append(" (\n");

        // 字段定义
        boolean firstField = true;
        for (FieldCreateRequest field : request.getFields()) {
            if (!firstField) {
                sql.append(",\n");
            }
            firstField = false;

            sql.append("  `").append(field.getFieldName()).append("` ");
            sql.append(mapFieldType(field.getFieldType()));

            // 默认值
            if (field.getDefaultValue() != null && !field.getDefaultValue().isEmpty()) {
                sql.append(" DEFAULT ").append(field.getDefaultValue());
            }

            // 注释
            if (field.getFieldComment() != null && !field.getFieldComment().isEmpty()) {
                String comment = stripHtmlTags(field.getFieldComment());
                sql.append(" COMMENT '").append(escapeSqlString(comment)).append("'");
            }
        }

        sql.append("\n)");

        // 引擎(默认使用MergeTree)
        sql.append("\nENGINE = MergeTree()");

        // ORDER BY(使用第一个字段作为排序键)
        if (!request.getFields().isEmpty()) {
            sql.append("\nORDER BY ").append(request.getFields().get(0).getFieldName());
        }

        // 分区
        FieldCreateRequest partitionField = request.getFields().stream()
                .filter(f -> f.getIsPartitionKey() != null && f.getIsPartitionKey() == 1)
                .findFirst()
                .orElse(null);

        if (partitionField != null) {
            sql.append("\nPARTITION BY ").append(partitionField.getFieldName());
        }

        // 表注释
        if (request.getTableComment() != null && !request.getTableComment().isEmpty()) {
            String comment = stripHtmlTags(request.getTableComment());
            sql.append("\nCOMMENT '").append(escapeSqlString(comment)).append("'");
        }

        sql.append(";");

        return sql.toString();
    }

    @Override
    public String generateAlterTableSql(TableUpdateRequest request) {
        return "-- ClickHouse支持ALTER TABLE\n" +
               "-- ALTER TABLE db.table ADD COLUMN new_column String;";
    }

    @Override
    public String generateDropTableSql(String databaseName, String tableName) {
        return String.format("DROP TABLE IF EXISTS %s.%s;", databaseName, tableName);
    }

    @Override
    public void validateSql(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL不能为空");
        }

        String upperSql = sql.trim().toUpperCase();

        if (upperSql.contains("DROP DATABASE")) {
            throw new IllegalArgumentException("禁止使用DROP DATABASE操作");
        }

        log.info("ClickHouse SQL语法校验通过");
    }

    @Override
    public void executeDdl(String sql) {
        log.info("执行ClickHouse DDL: {}", sql);
        throw new UnsupportedOperationException("ClickHouse DDL执行功能待实现，需要配置ClickHouse连接");
    }

    private String mapFieldType(String type) {
        if (type == null) {
            return "String";
        }

        String upperType = type.toUpperCase();
        // ClickHouse类型映射: BIGINT->Int64, VARCHAR->String等
        switch (upperType) {
            case "BIGINT":
            case "LONG":
                return "Int64";
            case "INT":
            case "INTEGER":
                return "Int32";
            case "VARCHAR":
            case "TEXT":
                return "String";
            case "DOUBLE":
                return "Float64";
            case "FLOAT":
                return "Float32";
            case "BOOLEAN":
            case "BOOL":
                return "UInt8";
            case "TIMESTAMP":
                return "DateTime";
            case "DATE":
                return "Date";
            default:
                return upperType;
        }
    }

    private String stripHtmlTags(String html) {
        if (html == null) {
            return "";
        }
        return html.replaceAll("<[^>]*>", "").trim();
    }

    private String escapeSqlString(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("'", "\\'");
    }
}
