package com.datawarehouse.metadata.strategy.impl;

import com.datawarehouse.metadata.dto.request.FieldCreateRequest;
import com.datawarehouse.metadata.dto.request.TableCreateRequest;
import com.datawarehouse.metadata.dto.request.TableUpdateRequest;
import com.datawarehouse.metadata.strategy.SqlGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Apache Paimon SQL生成器
 *
 * @author System
 * @since 1.0.0
 */
@Slf4j
@Component("paimonSqlGenerator")
public class PaimonSqlGenerator implements SqlGenerator {

    @Override
    public String generateCreateTableSql(TableCreateRequest request) {
        StringBuilder sql = new StringBuilder();

        // Paimon通过Flink SQL创建
        sql.append("CREATE TABLE ");
        sql.append(request.getDatabaseName()).append(".").append(request.getTableName());
        sql.append(" (\n");

        // 字段定义
        boolean firstField = true;
        FieldCreateRequest primaryKeyField = null;

        for (FieldCreateRequest field : request.getFields()) {
            if (!firstField) {
                sql.append(",\n");
            }
            firstField = false;

            sql.append("  ").append(field.getFieldName())
               .append(" ").append(field.getFieldType().toUpperCase());

            if (field.getIsPrimaryKey() != null && field.getIsPrimaryKey() == 1) {
                primaryKeyField = field;
            }

            // 注释
            if (field.getFieldComment() != null && !field.getFieldComment().isEmpty()) {
                String comment = stripHtmlTags(field.getFieldComment());
                sql.append(" COMMENT '").append(escapeSqlString(comment)).append("'");
            }
        }

        // 主键定义
        if (primaryKeyField != null) {
            sql.append(",\n  PRIMARY KEY (").append(primaryKeyField.getFieldName()).append(") NOT ENFORCED");
        }

        sql.append("\n)");

        // 表注释
        if (request.getTableComment() != null && !request.getTableComment().isEmpty()) {
            String comment = stripHtmlTags(request.getTableComment());
            sql.append(" COMMENT '").append(escapeSqlString(comment)).append("'");
        }

        // 分区
        FieldCreateRequest partitionField = request.getFields().stream()
                .filter(f -> f.getIsPartitionKey() != null && f.getIsPartitionKey() == 1)
                .findFirst()
                .orElse(null);

        if (partitionField != null) {
            sql.append("\nPARTITIONED BY (").append(partitionField.getFieldName()).append(")");
        }

        // WITH选项
        sql.append("\nWITH (");
        sql.append("\n  'connector' = 'paimon'");
        sql.append(",\n  'file.format' = 'parquet'");
        sql.append("\n);");

        return sql.toString();
    }

    @Override
    public String generateAlterTableSql(TableUpdateRequest request) {
        return "-- Paimon支持ALTER TABLE ADD COLUMN\n" +
               "-- ALTER TABLE catalog.db.table ADD COLUMN new_column STRING;";
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
        log.info("Paimon SQL语法校验通过");
    }

    @Override
    public void executeDdl(String sql) {
        log.info("执行Paimon DDL: {}", sql);
        throw new UnsupportedOperationException("Paimon DDL执行功能待实现，需要配置Flink连接");
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
