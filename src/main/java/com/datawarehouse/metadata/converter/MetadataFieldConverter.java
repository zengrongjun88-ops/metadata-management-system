package com.datawarehouse.metadata.converter;

import com.datawarehouse.metadata.entity.MetadataField;
import com.datawarehouse.metadata.vo.MetadataFieldVO;
import org.springframework.beans.BeanUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 元数据字段转换器
 *
 * @author System
 * @since 1.0.0
 */
public class MetadataFieldConverter {

    /**
     * 实体转VO
     *
     * @param entity 实体
     * @return VO
     */
    public static MetadataFieldVO toVO(MetadataField entity) {
        if (entity == null) {
            return null;
        }

        MetadataFieldVO vo = new MetadataFieldVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    /**
     * 实体列表转VO列表
     *
     * @param entities 实体列表
     * @return VO列表
     */
    public static List<MetadataFieldVO> toVOList(List<MetadataField> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }

        return entities.stream()
                .map(MetadataFieldConverter::toVO)
                .collect(Collectors.toList());
    }
}
