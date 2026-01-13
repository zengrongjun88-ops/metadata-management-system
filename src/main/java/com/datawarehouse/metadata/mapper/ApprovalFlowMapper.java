package com.datawarehouse.metadata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.datawarehouse.metadata.entity.ApprovalFlow;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审批流程Mapper接口
 *
 * @author System
 * @since 1.0.0
 */
@Mapper
public interface ApprovalFlowMapper extends BaseMapper<ApprovalFlow> {

}
