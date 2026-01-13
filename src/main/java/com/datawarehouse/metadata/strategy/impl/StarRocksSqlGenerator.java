package com.datawarehouse.metadata.strategy.impl;

import com.datawarehouse.metadata.dto.request.FieldCreateRequest;
import com.datawarehouse.metadata.dto.request.TableCreateRequest;
import com.datawarehouse.metadata.dto.request.TableUpdateRequest;
import com.datawarehouse.metadata.strategy.SqlGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * StarRocks SQL生成器
 *
 * @author System
 * @since 1.0.0
 */
@Slf4j
@Component("starrocksSqlGenerator")
public class StarRocksSqlGenerator implements SqlGenerator {

    @Override
    public String generateCreateTableSql(TableCreateRequest request) {
        StringBuilder sql = new StringBuilder();

        // CREATE TABLE
        sql.append("CREATE TABLE IF NOT EXISTS ");
        sql.append(request.getDatabaseName()).append(".").append(request.getTableName());
        sql.append(" (\n");

        // 字段定义
        boolean firstField = true;
        FieldCreateRequest keyField = null;

        for (FieldCreateRequest field : request.getFields()) {
            if (!firstField) {
                sql.append(",\n");
            }
            firstField = false;

            sql.append("  `").append(field.getFieldName()).append("` ");
            sql.append(mapFieldType(field.getFieldType()));

            // NOT NULL约束
            if (field.getIsNullable() != null && field.getIsNullable() == 0) {
                sql.append(" NOT NULL");
            }

            // 默认值
            if (field.getDefaultValue() != null && !field.getDefaultValue().isEmpty()) {
                sql.append(" DEFAULT \"").append(escapeSqlString(field.getDefaultValue())).append("\"");
            }

            // 注释
            if (field.getFieldComment() != null && !field.getFieldComment().isEmpty()) {
                String comment = stripHtmlTags(field.getFieldComment());
                sql.append(" COMMENT '").append(escapeSqlString(comment)).append("'");
            }

            // 记录主键字段
            if (field.getIsPrimaryKey() != null && field.getIsPrimaryKey() == 1) {
                keyField = field;
            }
        }

        sql.append("\n)");

        // 引擎(默认使用Duplicate Key)
        if (keyField != null) {
            sql.append("\nPRIMARY KEY (`").append(keyField.getFieldName()).append("`)");
        } else {
            // 没有主键时使用DUPLICATE KEY
            sql.append("\nDUPLICATE KEY (`").append(request.getFields().get(0).getFieldName()).append("`)");
        }

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
            sql.append("\nPARTITION BY RANGE(`").append(partitionField.getFieldName()).append("`)()");
        }

        // 分桶
        sql.append("\nDISTRIBUTED BY HASH(");
        if (keyField != null) {
            sql.append("`").append(keyField.getFieldName()).append("`");
        } else {
            sql.append("`").append(request.getFields().get(0).getFieldName()).append("`");
        }
        sql.append(") BUCKETS 10");

        // 表属性
        sql.append("\nPROPERTIES (");
        sql.append("\n  \"replication_num\" = \"3\"");
        sql.append(",\n  \"storage_format\" = \"DEFAULT\"");
        sql.append("\n);");

        return sql.toString();
    }

    @Override
    public String generateAlterTableSql(TableUpdateRequest request) {
        return "-- StarRocks支持ALTER TABLE\n" +
               "-- ALTER TABLE db.table ADD COLUMN new_column VARCHAR(100);";
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

        log.info("StarRocks SQL语法校验通过");
    }

    @Override
    public void executeDdl(String sql) {
        log.info("执行StarRocks DDL: {}", sql);
        throw new UnsupportedOperationException("StarRocks DDL执行功能待实现，需要配置StarRocks连接");
    }

    private String mapFieldType(String type) {
        if (type == null) {
            return "VARCHAR(255)";
        }

        String upperType = type.toUpperCase();
        // StarRocks类型映射
        switch (upperType) {
            case "BIGINT":
            case "LONG":
                return "BIGINT";
            case "INT":
            case "INTEGER":
                return "INT";
            case "TEXT":
                return "STRING";
            case "VARCHAR":
                return "VARCHAR(65533)";
            case "DOUBLE":
                return "DOUBLE";
            case "FLOAT":
                return "FLOAT";
            case "BOOLEAN":
            case "BOOL":
                return "BOOLEAN";
            case "TIMESTAMP":
                return "DATETIME";
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
        return str.replace("'", "\\'");
    }
}
