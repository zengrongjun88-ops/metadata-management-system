package com.datawarehouse.metadata.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.datawarehouse.metadata.dto.request.FieldCreateRequest;
import com.datawarehouse.metadata.dto.request.FieldUpdateRequest;
import com.datawarehouse.metadata.entity.MetadataField;
import com.datawarehouse.metadata.vo.MetadataFieldVO;

import java.util.List;

/**
 * 元数据字段Service接口
 *
 * @author System
 * @since 1.0.0
 */
public interface IMetadataFieldService extends IService<MetadataField> {

    /**
     * 根据表ID获取字段列表
     *
     * @param tableId 表ID
     * @return 字段列表
     */
    List<MetadataFieldVO> getFieldsByTableId(Long tableId);

    /**
     * 批量创建字段
     *
     * @param tableId 表ID
     * @param fields 字段列表
     */
    void batchCreateFields(Long tableId, List<FieldCreateRequest> fields);

    /**
     * 更新字段
     *
     * @param fieldId 字段ID
     * @param request 更新请求
     */
    void updateField(Long fieldId, FieldUpdateRequest request);

    /**
     * 删除字段
     *
     * @param fieldId 字段ID
     */
    void deleteField(Long fieldId);

    /**
     * 根据表ID删除所有字段
     *
     * @param tableId 表ID
     */
    void deleteFieldsByTableId(Long tableId);
}
