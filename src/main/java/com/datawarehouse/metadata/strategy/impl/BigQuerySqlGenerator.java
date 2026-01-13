package com.datawarehouse.metadata.strategy.impl;

import com.datawarehouse.metadata.dto.request.FieldCreateRequest;
import com.datawarehouse.metadata.dto.request.TableCreateRequest;
import com.datawarehouse.metadata.dto.request.TableUpdateRequest;
import com.datawarehouse.metadata.strategy.SqlGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * BigQuery SQL生成器
 *
 * @author System
 * @since 1.0.0
 */
@Slf4j
@Component("bigquerySqlGenerator")
public class BigQuerySqlGenerator implements SqlGenerator {

    @Override
    public String generateCreateTableSql(TableCreateRequest request) {
        StringBuilder sql = new StringBuilder();

        // CREATE TABLE
        sql.append("CREATE TABLE IF NOT EXISTS `");
        sql.append(request.getDatabaseName()).append(".").append(request.getTableName());
        sql.append("` (\n");

        // 字段定义
        boolean firstField = true;
        for (FieldCreateRequest field : request.getFields()) {
            if (!firstField) {
                sql.append(",\n");
            }
            firstField = false;

            sql.append("  ").append(field.getFieldName())
               .append(" ").append(mapFieldType(field.getFieldType()));

            // BigQuery的NOT NULL约束
            if (field.getIsNullable() != null && field.getIsNullable() == 0) {
                sql.append(" NOT NULL");
            }

            // OPTIONS中添加description
            if (field.getFieldComment() != null && !field.getFieldComment().isEmpty()) {
                String comment = stripHtmlTags(field.getFieldComment());
                sql.append(" OPTIONS(description=\"").append(escapeSqlString(comment)).append("\")");
            }
        }

        sql.append("\n)");

        // 分区设置
        FieldCreateRequest partitionField = request.getFields().stream()
                .filter(f -> f.getIsPartitionKey() != null && f.getIsPartitionKey() == 1)
                .findFirst()
                .orElse(null);

        if (partitionField != null) {
            sql.append("\nPARTITION BY ").append(partitionField.getFieldName());
        }

        // 表选项
        sql.append("\nOPTIONS(");
        if (request.getTableComment() != null && !request.getTableComment().isEmpty()) {
            String comment = stripHtmlTags(request.getTableComment());
            sql.append("\n  description=\"").append(escapeSqlString(comment)).append("\"");
        }
        sql.append("\n);");

        return sql.toString();
    }

    @Override
    public String generateAlterTableSql(TableUpdateRequest request) {
        return "-- BigQuery支持ALTER TABLE，但字段修改有限制\n" +
               "-- ALTER TABLE dataset.table ADD COLUMN new_column STRING;";
    }

    @Override
    public String generateDropTableSql(String databaseName, String tableName) {
        return String.format("DROP TABLE IF EXISTS `%s.%s`;", databaseName, tableName);
    }

    @Override
    public void validateSql(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL不能为空");
        }

        String upperSql = sql.trim().toUpperCase();

        if (upperSql.contains("DROP SCHEMA") || upperSql.contains("DROP DATABASE")) {
            throw new IllegalArgumentException("禁止使用DROP SCHEMA/DATABASE操作");
        }

        log.info("BigQuery SQL语法校验通过");
    }

    @Override
    public void executeDdl(String sql) {
        log.info("执行BigQuery DDL: {}", sql);
        throw new UnsupportedOperationException("BigQuery DDL执行功能待实现，需要配置BigQuery连接");
    }

    /**
     * 映射字段类型到BigQuery类型
     */
    private String mapFieldType(String type) {
        if (type == null) {
            return "STRING";
        }

        String upperType = type.toUpperCase();
        // BIGINT -> INT64, VARCHAR -> STRING等映射
        switch (upperType) {
            case "BIGINT":
            case "LONG":
                return "INT64";
            case "INT":
            case "INTEGER":
                return "INT64";
            case "VARCHAR":
            case "TEXT":
                return "STRING";
            case "DOUBLE":
            case "FLOAT":
                return "FLOAT64";
            case "BOOLEAN":
            case "BOOL":
                return "BOOL";
            case "TIMESTAMP":
                return "TIMESTAMP";
            case "DATE":
                return "DATE";
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
        return str.replace("\"", "\\\"");
    }
}
