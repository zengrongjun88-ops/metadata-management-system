package com.datawarehouse.metadata.strategy.impl;

import com.datawarehouse.metadata.dto.request.FieldCreateRequest;
import com.datawarehouse.metadata.dto.request.TableCreateRequest;
import com.datawarehouse.metadata.dto.request.TableUpdateRequest;
import com.datawarehouse.metadata.strategy.SqlGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Hive SQL生成器
 *
 * @author System
 * @since 1.0.0
 */
@Slf4j
@Component("hiveSqlGenerator")
public class HiveSqlGenerator implements SqlGenerator {

    @Override
    public String generateCreateTableSql(TableCreateRequest request) {
        StringBuilder sql = new StringBuilder();

        // CREATE TABLE
        sql.append("CREATE TABLE IF NOT EXISTS ");
        sql.append(request.getDatabaseName()).append(".").append(request.getTableName());
        sql.append(" (\n");

        // 字段定义(排除分区字段)
        boolean firstField = true;
        for (FieldCreateRequest field : request.getFields()) {
            if (field.getIsPartitionKey() != null && field.getIsPartitionKey() == 1) {
                continue; // 分区字段稍后处理
            }

            if (!firstField) {
                sql.append(",\n");
            }
            firstField = false;

            sql.append("  ").append(field.getFieldName())
               .append(" ").append(field.getFieldType().toUpperCase());

            // 添加注释
            if (field.getFieldComment() != null && !field.getFieldComment().isEmpty()) {
                String comment = stripHtmlTags(field.getFieldComment());
                sql.append(" COMMENT '").append(escapeSqlString(comment)).append("'");
            }
        }

        sql.append("\n)");

        // 表注释
        if (request.getTableComment() != null && !request.getTableComment().isEmpty()) {
            String comment = stripHtmlTags(request.getTableComment());
            sql.append("\nCOMMENT '").append(escapeSqlString(comment)).append("'");
        }

        // 分区字段
        boolean hasPartition = request.getFields().stream()
                .anyMatch(f -> f.getIsPartitionKey() != null && f.getIsPartitionKey() == 1);

        if (hasPartition) {
            sql.append("\nPARTITIONED BY (");
            boolean firstPartition = true;
            for (FieldCreateRequest field : request.getFields()) {
                if (field.getIsPartitionKey() != null && field.getIsPartitionKey() == 1) {
                    if (!firstPartition) {
                        sql.append(", ");
                    }
                    firstPartition = false;

                    sql.append(field.getFieldName()).append(" ").append(field.getFieldType().toUpperCase());
                }
            }
            sql.append(")");
        }

        // 存储格式(Hive默认使用Parquet)
        sql.append("\nSTORED AS PARQUET");

        // 表属性
        sql.append("\nTBLPROPERTIES (");
        sql.append("\n  'creator'='").append(request.getOwner() != null ? request.getOwner() : "system").append("'");

        if (request.getSensitivityLevel() != null) {
            sql.append(",\n  'sensitivity_level'='").append(request.getSensitivityLevel()).append("'");
        }

        if (request.getImportanceLevel() != null) {
            sql.append(",\n  'importance_level'='").append(request.getImportanceLevel()).append("'");
        }

        sql.append("\n);");

        return sql.toString();
    }

    @Override
    public String generateAlterTableSql(TableUpdateRequest request) {
        // Hive不支持直接修改表结构，这里返回提示信息
        return "-- Hive不支持ALTER TABLE修改已有字段类型\n" +
               "-- 请使用DROP TABLE后重新CREATE TABLE，或创建新表后迁移数据";
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

        // 基础语法检查
        String upperSql = sql.trim().toUpperCase();

        // 检查是否包含危险操作
        if (upperSql.contains("DROP DATABASE")) {
            throw new IllegalArgumentException("禁止使用DROP DATABASE操作");
        }

        // 检查是否是DDL语句
        if (!upperSql.startsWith("CREATE") &&
            !upperSql.startsWith("ALTER") &&
            !upperSql.startsWith("DROP TABLE")) {
            throw new IllegalArgumentException("仅支持DDL语句(CREATE/ALTER/DROP TABLE)");
        }

        log.info("Hive SQL语法校验通过");
    }

    @Override
    public void executeDdl(String sql) {
        // 实际执行需要连接Hive服务器，这里暂时只记录日志
        log.info("执行Hive DDL: {}", sql);
        // TODO: 集成Hive JDBC连接，执行SQL
        throw new UnsupportedOperationException("Hive DDL执行功能待实现，需要配置Hive连接");
    }

    /**
     * 去除HTML标签
     */
    private String stripHtmlTags(String html) {
        if (html == null) {
            return "";
        }
        return html.replaceAll("<[^>]*>", "").trim();
    }

    /**
     * 转义SQL字符串中的单引号
     */
    private String escapeSqlString(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("'", "\\'");
    }
}
