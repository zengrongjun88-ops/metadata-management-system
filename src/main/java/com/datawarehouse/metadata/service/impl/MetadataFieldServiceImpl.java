package com.datawarehouse.metadata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.datawarehouse.metadata.converter.MetadataFieldConverter;
import com.datawarehouse.metadata.dto.request.FieldCreateRequest;
import com.datawarehouse.metadata.dto.request.FieldUpdateRequest;
import com.datawarehouse.metadata.entity.MetadataField;
import com.datawarehouse.metadata.exception.BusinessException;
import com.datawarehouse.metadata.mapper.MetadataFieldMapper;
import com.datawarehouse.metadata.service.IMetadataFieldService;
import com.datawarehouse.metadata.vo.MetadataFieldVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 元数据字段Service实现类
 *
 * @author System
 * @since 1.0.0
 */
@Service
public class MetadataFieldServiceImpl extends ServiceImpl<MetadataFieldMapper, MetadataField>
        implements IMetadataFieldService {

    @Override
    public List<MetadataFieldVO> getFieldsByTableId(Long tableId) {
        if (tableId == null) {
            throw new BusinessException("表ID不能为空");
        }

        LambdaQueryWrapper<MetadataField> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MetadataField::getTableId, tableId)
               .orderByAsc(MetadataField::getFieldOrder);

        List<MetadataField> fields = this.list(wrapper);
        return MetadataFieldConverter.toVOList(fields);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchCreateFields(Long tableId, List<FieldCreateRequest> fields) {
        if (tableId == null) {
            throw new BusinessException("表ID不能为空");
        }

        if (fields == null || fields.isEmpty()) {
            return;
        }

        // 校验字段序号唯一性和字段名唯一性
        for (int i = 0; i < fields.size(); i++) {
            FieldCreateRequest field = fields.get(i);

            // 校验字段名唯一性
            for (int j = i + 1; j < fields.size(); j++) {
                if (field.getFieldName().equals(fields.get(j).getFieldName())) {
                    throw new BusinessException("字段名重复: " + field.getFieldName());
                }
            }

            // 创建字段实体
            MetadataField entity = new MetadataField();
            BeanUtils.copyProperties(field, entity);
            entity.setTableId(tableId);

            // 保存字段
            this.save(entity);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateField(Long fieldId, FieldUpdateRequest request) {
        if (fieldId == null) {
            throw new BusinessException("字段ID不能为空");
        }

        MetadataField field = this.getById(fieldId);
        if (field == null) {
            throw new BusinessException("字段不存在: ID=" + fieldId);
        }

        // 如果更新字段名，校验同表内字段名唯一性
        if (request.getFieldName() != null && !request.getFieldName().equals(field.getFieldName())) {
            checkFieldNameUnique(field.getTableId(), request.getFieldName(), fieldId);
        }

        // 更新字段
        MetadataField updateEntity = new MetadataField();
        BeanUtils.copyProperties(request, updateEntity);
        updateEntity.setId(fieldId);

        this.updateById(updateEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteField(Long fieldId) {
        if (fieldId == null) {
            throw new BusinessException("字段ID不能为空");
        }

        MetadataField field = this.getById(fieldId);
        if (field == null) {
            throw new BusinessException("字段不存在: ID=" + fieldId);
        }

        this.removeById(fieldId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFieldsByTableId(Long tableId) {
        if (tableId == null) {
            throw new BusinessException("表ID不能为空");
        }

        LambdaQueryWrapper<MetadataField> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MetadataField::getTableId, tableId);

        this.remove(wrapper);
    }

    /**
     * 校验字段名唯一性
     */
    private void checkFieldNameUnique(Long tableId, String fieldName, Long excludeId) {
        LambdaQueryWrapper<MetadataField> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MetadataField::getTableId, tableId)
               .eq(MetadataField::getFieldName, fieldName);

        if (excludeId != null) {
            wrapper.ne(MetadataField::getId, excludeId);
        }

        long count = this.count(wrapper);
        if (count > 0) {
            throw new BusinessException("字段名已存在: " + fieldName);
        }
    }
}
