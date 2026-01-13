package com.datawarehouse.metadata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.datawarehouse.metadata.entity.OperationHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作历史Mapper接口
 *
 * @author System
 * @since 1.0.0
 */
@Mapper
public interface OperationHistoryMapper extends BaseMapper<OperationHistory> {

}
