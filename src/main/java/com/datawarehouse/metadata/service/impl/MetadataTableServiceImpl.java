package com.datawarehouse.metadata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.datawarehouse.metadata.entity.MetadataTable;
import com.datawarehouse.metadata.mapper.MetadataTableMapper;
import com.datawarehouse.metadata.service.IMetadataTableService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 元数据表Service实现类
 *
 * @author System
 * @since 1.0.0
 */
@Service
public class MetadataTableServiceImpl extends ServiceImpl<MetadataTableMapper, MetadataTable>
        implements IMetadataTableService {

    @Override
    public Page<MetadataTable> pageMetadataTable(Page<MetadataTable> page, String tableName, String databaseName) {
        LambdaQueryWrapper<MetadataTable> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(tableName), MetadataTable::getTableName, tableName)
               .eq(StringUtils.hasText(databaseName), MetadataTable::getDatabaseName, databaseName)
               .orderByDesc(MetadataTable::getCreateTime);
        return this.page(page, wrapper);
    }

}
