package com.datawarehouse.metadata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.datawarehouse.metadata.entity.MetadataField;
import org.apache.ibatis.annotations.Mapper;

/**
 * 元数据字段Mapper接口
 *
 * @author System
 * @since 1.0.0
 */
@Mapper
public interface MetadataFieldMapper extends BaseMapper<MetadataField> {

}
