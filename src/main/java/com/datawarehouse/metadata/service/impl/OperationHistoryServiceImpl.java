package com.datawarehouse.metadata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.datawarehouse.metadata.common.PageResult;
import com.datawarehouse.metadata.entity.OperationHistory;
import com.datawarehouse.metadata.enums.OperationTypeEnum;
import com.datawarehouse.metadata.exception.BusinessException;
import com.datawarehouse.metadata.mapper.OperationHistoryMapper;
import com.datawarehouse.metadata.service.IOperationHistoryService;
import com.datawarehouse.metadata.vo.OperationHistoryVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 操作历史Service实现类
 *
 * @author System
 * @since 1.0.0
 */
@Service
public class OperationHistoryServiceImpl extends ServiceImpl<OperationHistoryMapper, OperationHistory>
        implements IOperationHistoryService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    public void recordOperation(Long tableId, OperationTypeEnum type, Object before, Object after, String operationDesc) {
        if (tableId == null) {
            throw new BusinessException("表ID不能为空");
        }

        if (type == null) {
            throw new BusinessException("操作类型不能为空");
        }

        OperationHistory history = new OperationHistory();
        history.setTableId(tableId);
        history.setOperationType(type.getCode());
        history.setOperationTime(LocalDateTime.now());
        history.setOperationDesc(operationDesc);

        // 将对象转换为JSON
        try {
            if (before != null) {
                history.setBeforeContent(OBJECT_MAPPER.writeValueAsString(before));
            }

            if (after != null) {
                history.setAfterContent(OBJECT_MAPPER.writeValueAsString(after));
            }
        } catch (Exception e) {
            throw new BusinessException("序列化操作内容失败: " + e.getMessage());
        }

        // 设置操作人(从当前登录用户获取,这里暂时使用system)
        history.setOperator("system");

        boolean saved = this.save(history);
        if (!saved) {
            throw new BusinessException("保存操作历史失败");
        }
    }

    @Override
    public PageResult<OperationHistoryVO> getHistory(Long tableId, Integer pageNum, Integer pageSize) {
        if (tableId == null) {
            throw new BusinessException("表ID不能为空");
        }

        Page<OperationHistory> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<OperationHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OperationHistory::getTableId, tableId)
               .orderByDesc(OperationHistory::getOperationTime);

        Page<OperationHistory> resultPage = this.page(page, wrapper);

        return PageResult.of(resultPage.convert(this::convertToVO));
    }

    @Override
    public PageResult<OperationHistoryVO> getHistoryByOperator(String operator, Integer pageNum, Integer pageSize) {
        if (operator == null || operator.trim().isEmpty()) {
            throw new BusinessException("操作人不能为空");
        }

        Page<OperationHistory> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<OperationHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OperationHistory::getOperator, operator)
               .orderByDesc(OperationHistory::getOperationTime);

        Page<OperationHistory> resultPage = this.page(page, wrapper);

        return PageResult.of(resultPage.convert(this::convertToVO));
    }

    /**
     * 实体转VO
     */
    private OperationHistoryVO convertToVO(OperationHistory entity) {
        if (entity == null) {
            return null;
        }

        OperationHistoryVO vo = new OperationHistoryVO();
        BeanUtils.copyProperties(entity, vo);

        // 可以在这里关联查询表名和数据库名(暂不实现)
        return vo;
    }
}
