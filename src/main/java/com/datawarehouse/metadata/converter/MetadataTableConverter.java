package com.datawarehouse.metadata.converter;

import com.datawarehouse.metadata.entity.MetadataField;
import com.datawarehouse.metadata.entity.MetadataTable;
import com.datawarehouse.metadata.vo.MetadataFieldVO;
import com.datawarehouse.metadata.vo.MetadataTableVO;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 元数据表转换器
 *
 * @author System
 * @since 1.0.0
 */
public class MetadataTableConverter {

    /**
     * 实体转VO
     *
     * @param entity 实体
     * @return VO
     */
    public static MetadataTableVO toVO(MetadataTable entity) {
        if (entity == null) {
            return null;
        }

        MetadataTableVO vo = new MetadataTableVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    /**
     * 实体列表转VO列表
     *
     * @param entities 实体列表
     * @return VO列表
     */
    public static List<MetadataTableVO> toVOList(List<MetadataTable> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }

        return entities.stream()
                .map(MetadataTableConverter::toVO)
                .collect(Collectors.toList());
    }

    /**
     * 实体转VO(包含字段列表)
     *
     * @param entity 实体
     * @param fields 字段列表
     * @return VO
     */
    public static MetadataTableVO toVOWithFields(MetadataTable entity, List<MetadataField> fields) {
        MetadataTableVO vo = toVO(entity);
        if (vo != null && fields != null) {
            vo.setFields(MetadataFieldConverter.toVOList(fields));
        }
        return vo;
    }
}
