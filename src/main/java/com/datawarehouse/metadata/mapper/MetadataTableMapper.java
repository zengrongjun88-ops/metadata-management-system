package com.datawarehouse.metadata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.datawarehouse.metadata.entity.MetadataTable;
import org.apache.ibatis.annotations.Mapper;

/**
 * 元数据表Mapper接口
 *
 * @author System
 * @since 1.0.0
 */
@Mapper
public interface MetadataTableMapper extends BaseMapper<MetadataTable> {

}
