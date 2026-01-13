package com.datawarehouse.metadata.strategy;

import com.datawarehouse.metadata.dto.request.TableCreateRequest;
import com.datawarehouse.metadata.dto.request.TableUpdateRequest;

/**
 * SQL生成器策略接口
 *
 * @author System
 * @since 1.0.0
 */
public interface SqlGenerator {

    /**
     * 生成建表SQL
     *
     * @param request 建表请求
     * @return 建表SQL语句
     */
    String generateCreateTableSql(TableCreateRequest request);

    /**
     * 生成修改表SQL
     *
     * @param request 修改表请求
     * @return 修改表SQL语句
     */
    String generateAlterTableSql(TableUpdateRequest request);

    /**
     * 生成删除表SQL
     *
     * @param databaseName 数据库名
     * @param tableName 表名
     * @return 删除表SQL语句
     */
    String generateDropTableSql(String databaseName, String tableName);

    /**
     * 校验SQL语法
     *
     * @param sql SQL语句
     * @throws IllegalArgumentException 如果SQL语法错误
     */
    void validateSql(String sql);

    /**
     * 执行DDL语句
     *
     * @param sql DDL语句
     * @throws RuntimeException 如果执行失败
     */
    void executeDdl(String sql);
}
