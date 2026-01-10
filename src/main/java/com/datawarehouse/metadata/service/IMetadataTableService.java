package com.datawarehouse.metadata.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.datawarehouse.metadata.entity.MetadataTable;

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

}
