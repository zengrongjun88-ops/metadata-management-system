package com.datawarehouse.metadata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.datawarehouse.metadata.common.PageResult;
import com.datawarehouse.metadata.converter.MetadataTableConverter;
import com.datawarehouse.metadata.dto.request.TableCreateRequest;
import com.datawarehouse.metadata.dto.request.TableSearchRequest;
import com.datawarehouse.metadata.dto.request.TableUpdateRequest;
import com.datawarehouse.metadata.entity.MetadataField;
import com.datawarehouse.metadata.entity.MetadataTable;
import com.datawarehouse.metadata.enums.OperationTypeEnum;
import com.datawarehouse.metadata.exception.BusinessException;
import com.datawarehouse.metadata.mapper.MetadataTableMapper;
import com.datawarehouse.metadata.service.IMetadataFieldService;
import com.datawarehouse.metadata.service.IMetadataTableService;
import com.datawarehouse.metadata.service.IOperationHistoryService;
import com.datawarehouse.metadata.strategy.SqlGeneratorFactory;
import com.datawarehouse.metadata.vo.MetadataTableVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 元数据表Service实现类
 *
 * @author System
 * @since 1.0.0
 */
@Service
public class MetadataTableServiceImpl extends ServiceImpl<MetadataTableMapper, MetadataTable>
        implements IMetadataTableService {

    @Autowired(required = false)
    private IMetadataFieldService fieldService;

    @Autowired
    private IOperationHistoryService historyService;

    @Autowired(required = false)
    private SqlGeneratorFactory sqlGeneratorFactory;

    @Override
    public Page<MetadataTable> pageMetadataTable(Page<MetadataTable> page, String tableName, String databaseName) {
        LambdaQueryWrapper<MetadataTable> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(tableName), MetadataTable::getTableName, tableName)
               .eq(StringUtils.hasText(databaseName), MetadataTable::getDatabaseName, databaseName)
               .orderByDesc(MetadataTable::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public PageResult<MetadataTableVO> pageQuery(TableSearchRequest request) {
        Page<MetadataTable> page = new Page<>(request.getPageNum(), request.getPageSize());

        LambdaQueryWrapper<MetadataTable> wrapper = new LambdaQueryWrapper<>();

        // 关键词搜索(表名或表描述)
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.and(w -> w.like(MetadataTable::getTableName, request.getKeyword())
                             .or()
                             .like(MetadataTable::getTableComment, request.getKeyword()));
        }

        // 精确匹配条件
        wrapper.eq(StringUtils.hasText(request.getDatabaseName()), MetadataTable::getDatabaseName, request.getDatabaseName())
               .eq(StringUtils.hasText(request.getDataSource()), MetadataTable::getDataSource, request.getDataSource())
               .eq(StringUtils.hasText(request.getHiveAccount()), MetadataTable::getHiveAccount, request.getHiveAccount())
               .eq(StringUtils.hasText(request.getWarehouseLayer()), MetadataTable::getWarehouseLayer, request.getWarehouseLayer())
               .eq(StringUtils.hasText(request.getThemeFirst()), MetadataTable::getThemeFirst, request.getThemeFirst())
               .eq(StringUtils.hasText(request.getThemeSecond()), MetadataTable::getThemeSecond, request.getThemeSecond())
               .eq(StringUtils.hasText(request.getSensitivityLevel()), MetadataTable::getSensitivityLevel, request.getSensitivityLevel())
               .eq(StringUtils.hasText(request.getImportanceLevel()), MetadataTable::getImportanceLevel, request.getImportanceLevel())
               .eq(StringUtils.hasText(request.getUpdateFrequency()), MetadataTable::getUpdateFrequency, request.getUpdateFrequency())
               .eq(StringUtils.hasText(request.getOwner()), MetadataTable::getOwner, request.getOwner());

        // 排序
        if (StringUtils.hasText(request.getSortField())) {
            if ("DESC".equalsIgnoreCase(request.getSortOrder())) {
                wrapper.orderByDesc(getColumnByField(request.getSortField()));
            } else {
                wrapper.orderByAsc(getColumnByField(request.getSortField()));
            }
        } else {
            wrapper.orderByDesc(MetadataTable::getCreateTime);
        }

        Page<MetadataTable> resultPage = this.page(page, wrapper);

        return PageResult.of(resultPage.convert(MetadataTableConverter::toVO));
    }

    @Override
    public MetadataTableVO getTableById(Long id) {
        if (id == null) {
            throw new BusinessException("表ID不能为空");
        }

        MetadataTable table = this.getById(id);
        if (table == null) {
            throw new BusinessException("表不存在: ID=" + id);
        }

        // 获取字段列表
        List<MetadataField> fields = fieldService != null ?
            fieldService.list(new LambdaQueryWrapper<MetadataField>()
                .eq(MetadataField::getTableId, id)
                .orderByAsc(MetadataField::getFieldOrder)) :
            null;

        return MetadataTableConverter.toVOWithFields(table, fields);
    }

    @Override
    public MetadataTableVO getTableByName(String databaseName, String tableName) {
        if (!StringUtils.hasText(databaseName) || !StringUtils.hasText(tableName)) {
            throw new BusinessException("数据库名和表名不能为空");
        }

        LambdaQueryWrapper<MetadataTable> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MetadataTable::getDatabaseName, databaseName)
               .eq(MetadataTable::getTableName, tableName);

        MetadataTable table = this.getOne(wrapper);
        if (table == null) {
            throw new BusinessException("表不存在: " + databaseName + "." + tableName);
        }

        // 获取字段列表
        List<MetadataField> fields = fieldService != null ?
            fieldService.list(new LambdaQueryWrapper<MetadataField>()
                .eq(MetadataField::getTableId, table.getId())
                .orderByAsc(MetadataField::getFieldOrder)) :
            null;

        return MetadataTableConverter.toVOWithFields(table, fields);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTable(TableCreateRequest request) {
        // 1. 校验表名唯一性
        checkTableNameUnique(request.getDatabaseName(), request.getTableName(), null);

        // 2. 生成建表SQL
        String createSql = generateCreateSql(request);

        // 3. 创建表实体
        MetadataTable table = new MetadataTable();
        BeanUtils.copyProperties(request, table);
        table.setCreateSql(createSql);

        // 4. 保存表
        this.save(table);

        // 5. 批量创建字段
        if (fieldService != null && request.getFields() != null && !request.getFields().isEmpty()) {
            fieldService.batchCreateFields(table.getId(), request.getFields());
        }

        // 6. 记录操作历史
        historyService.recordOperation(table.getId(), OperationTypeEnum.CREATE, null, table, "创建表: " + request.getTableName());

        return table.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTable(TableUpdateRequest request) {
        // 1. 校验表是否存在
        MetadataTable oldTable = this.getById(request.getId());
        if (oldTable == null) {
            throw new BusinessException("表不存在: ID=" + request.getId());
        }

        // 2. 更新表信息
        MetadataTable table = new MetadataTable();
        BeanUtils.copyProperties(request, table);

        this.updateById(table);

        // 3. 更新字段(如果提供)
        if (fieldService != null && request.getFields() != null) {
            // 先删除旧字段
            fieldService.deleteFieldsByTableId(request.getId());
            // 批量创建新字段
            fieldService.batchCreateFields(request.getId(),
                request.getFields().stream()
                    .map(f -> {
                        com.datawarehouse.metadata.dto.request.FieldCreateRequest createReq =
                            new com.datawarehouse.metadata.dto.request.FieldCreateRequest();
                        BeanUtils.copyProperties(f, createReq);
                        return createReq;
                    })
                    .collect(java.util.stream.Collectors.toList())
            );
        }

        // 4. 记录操作历史
        // 记录操作历史
        historyService.recordOperation(request.getId(), OperationTypeEnum.UPDATE, oldTable, table, "更新表: " + oldTable.getTableName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTable(Long id) {
        // 1. 校验表是否存在
        MetadataTable table = this.getById(id);
        if (table == null) {
            throw new BusinessException("表不存在: ID=" + id);
        }

        // 2. 删除表(软删除,MyBatis Plus会自动处理)
        this.removeById(id);

        // 3. 删除字段(级联删除由数据库外键处理,这里只是为了保险)
        if (fieldService != null) {
            fieldService.deleteFieldsByTableId(id);
        }

        // 4. 记录操作历史
        historyService.recordOperation(id, OperationTypeEnum.DELETE, table, null, "删除表: " + table.getTableName());
    }

    @Override
    public String generateCreateSql(TableCreateRequest request) {
        if (sqlGeneratorFactory == null) {
            throw new BusinessException("SQL生成器未配置");
        }

        try {
            return sqlGeneratorFactory.getGenerator(request.getDataSource())
                    .generateCreateTableSql(request);
        } catch (Exception e) {
            throw new BusinessException("生成SQL失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void validateSql(String sql, String dataSource) {
        if (!StringUtils.hasText(sql)) {
            throw new BusinessException("SQL不能为空");
        }

        if (!StringUtils.hasText(dataSource)) {
            throw new BusinessException("数据源类型不能为空");
        }

        if (sqlGeneratorFactory == null) {
            throw new BusinessException("SQL生成器未配置");
        }

        try {
            sqlGeneratorFactory.getGenerator(dataSource).validateSql(sql);
        } catch (Exception e) {
            throw new BusinessException("SQL校验失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查表名唯一性
     */
    private void checkTableNameUnique(String databaseName, String tableName, Long excludeId) {
        LambdaQueryWrapper<MetadataTable> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MetadataTable::getDatabaseName, databaseName)
               .eq(MetadataTable::getTableName, tableName);

        if (excludeId != null) {
            wrapper.ne(MetadataTable::getId, excludeId);
        }

        long count = this.count(wrapper);
        if (count > 0) {
            throw new BusinessException("表已存在: " + databaseName + "." + tableName);
        }
    }

    /**
     * 根据字段名获取数据库列(用于排序)
     */
    private com.baomidou.mybatisplus.core.toolkit.support.SFunction<MetadataTable, ?> getColumnByField(String field) {
        switch (field) {
            case "table_name":
                return MetadataTable::getTableName;
            case "database_name":
                return MetadataTable::getDatabaseName;
            case "create_time":
                return MetadataTable::getCreateTime;
            case "update_time":
                return MetadataTable::getUpdateTime;
            case "table_size":
                return MetadataTable::getTableSize;
            default:
                return MetadataTable::getCreateTime;
        }
    }
}
