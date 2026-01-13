package com.datawarehouse.metadata.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.datawarehouse.metadata.common.PageResult;
import com.datawarehouse.metadata.entity.OperationHistory;
import com.datawarehouse.metadata.enums.OperationTypeEnum;
import com.datawarehouse.metadata.vo.OperationHistoryVO;

/**
 * 操作历史Service接口
 *
 * @author System
 * @since 1.0.0
 */
public interface IOperationHistoryService extends IService<OperationHistory> {

    /**
     * 记录操作历史
     *
     * @param tableId 表ID
     * @param type 操作类型
     * @param before 变更前内容
     * @param after 变更后内容
     * @param operationDesc 操作描述
     */
    void recordOperation(Long tableId, OperationTypeEnum type, Object before, Object after, String operationDesc);

    /**
     * 获取表的操作历史
     *
     * @param tableId 表ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResult<OperationHistoryVO> getHistory(Long tableId, Integer pageNum, Integer pageSize);

    /**
     * 获取用户的操作历史
     *
     * @param operator 操作人
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResult<OperationHistoryVO> getHistoryByOperator(String operator, Integer pageNum, Integer pageSize);
}
