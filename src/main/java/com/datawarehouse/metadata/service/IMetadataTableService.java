package com.datawarehouse.metadata.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.datawarehouse.metadata.common.PageResult;
import com.datawarehouse.metadata.dto.request.TableCreateRequest;
import com.datawarehouse.metadata.dto.request.TableSearchRequest;
import com.datawarehouse.metadata.dto.request.TableUpdateRequest;
import com.datawarehouse.metadata.entity.MetadataTable;
import com.datawarehouse.metadata.vo.MetadataTableVO;

/**
 * 元数据表Service接口
 *
 * @author System
 * @since 1.0.0
 */
public interface IMetadataTableService extends IService<MetadataTable> {

    /**
     * 分页查询元数据表
     *
     * @param page 分页对象
     * @param tableName 表名
     * @param databaseName 数据库名
     * @return 分页结果
     */
    Page<MetadataTable> pageMetadataTable(Page<MetadataTable> page, String tableName, String databaseName);

    /**
     * 分页查询元数据表(返回VO)
     *
     * @param request 搜索请求
     * @return 分页结果
     */
    PageResult<MetadataTableVO> pageQuery(TableSearchRequest request);

    /**
     * 根据ID获取表详情
     *
     * @param id 表ID
     * @return 表详情
     */
    MetadataTableVO getTableById(Long id);

    /**
     * 根据数据库名和表名获取表详情
     *
     * @param databaseName 数据库名
     * @param tableName 表名
     * @return 表详情
     */
    MetadataTableVO getTableByName(String databaseName, String tableName);

    /**
     * 创建元数据表
     *
     * @param request 创建请求
     * @return 表ID
     */
    Long createTable(TableCreateRequest request);

    /**
     * 更新元数据表
     *
     * @param request 更新请求
     */
    void updateTable(TableUpdateRequest request);

    /**
     * 删除元数据表
     *
     * @param id 表ID
     */
    void deleteTable(Long id);

    /**
     * 生成建表SQL
     *
     * @param request 建表请求
     * @return SQL语句
     */
    String generateCreateSql(TableCreateRequest request);

    /**
     * 校验SQL语法
     *
     * @param sql SQL语句
     * @param dataSource 数据源类型
     */
    void validateSql(String sql, String dataSource);
}
