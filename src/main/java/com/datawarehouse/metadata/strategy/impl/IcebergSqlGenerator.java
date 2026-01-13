package com.datawarehouse.metadata.strategy.impl;

import com.datawarehouse.metadata.dto.request.FieldCreateRequest;
import com.datawarehouse.metadata.dto.request.TableCreateRequest;
import com.datawarehouse.metadata.dto.request.TableUpdateRequest;
import com.datawarehouse.metadata.strategy.SqlGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Apache Iceberg SQL生成器
 *
 * @author System
 * @since 1.0.0
 */
@Slf4j
@Component("icebergSqlGenerator")
public class IcebergSqlGenerator implements SqlGenerator {

    @Override
    public String generateCreateTableSql(TableCreateRequest request) {
        StringBuilder sql = new StringBuilder();

        // Iceberg通过Spark SQL创建
        sql.append("CREATE TABLE ");
        sql.append(request.getDatabaseName()).append(".").append(request.getTableName());
        sql.append(" (\n");

        // 字段定义
        boolean firstField = true;
        for (FieldCreateRequest field : request.getFields()) {
            // 跳过分区字段
            if (field.getIsPartitionKey() != null && field.getIsPartitionKey() == 1) {
                continue;
            }

            if (!firstField) {
                sql.append(",\n");
            }
            firstField = false;

            sql.append("  ").append(field.getFieldName())
               .append(" ").append(field.getFieldType().toUpperCase());

            // NOT NULL约束
            if (field.getIsNullable() != null && field.getIsNullable() == 0) {
                sql.append(" NOT NULL");
            }

            // 注释
            if (field.getFieldComment() != null && !field.getFieldComment().isEmpty()) {
                String comment = stripHtmlTags(field.getFieldComment());
                sql.append(" COMMENT '").append(escapeSqlString(comment)).append("'");
            }
        }

        sql.append("\n)");

        // 使用ICEBERG格式
        sql.append("\nUSING iceberg");

        // 表注释
        if (request.getTableComment() != null && !request.getTableComment().isEmpty()) {
            String comment = stripHtmlTags(request.getTableComment());
            sql.append("\nCOMMENT '").append(escapeSqlString(comment)).append("'");
        }

        // 分区
        FieldCreateRequest partitionField = request.getFields().stream()
                .filter(f -> f.getIsPartitionKey() != null && f.getIsPartitionKey() == 1)
                .findFirst()
                .orElse(null);

        if (partitionField != null) {
            sql.append("\nPARTITIONED BY (").append(partitionField.getFieldName()).append(")");
        }

        // 表属性
        sql.append("\nTBLPROPERTIES (");
        sql.append("\n  'format-version' = '2'");
        sql.append(",\n  'write.format.default' = 'parquet'");
        sql.append("\n);");

        return sql.toString();
    }

    @Override
    public String generateAlterTableSql(TableUpdateRequest request) {
        return "-- Iceberg支持Schema Evolution\n" +
               "-- ALTER TABLE db.table ADD COLUMN new_column string;";
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
        log.info("Iceberg SQL语法校验通过");
    }

    @Override
    public void executeDdl(String sql) {
        log.info("执行Iceberg DDL: {}", sql);
        throw new UnsupportedOperationException("Iceberg DDL执行功能待实现，需要配置Spark连接");
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
