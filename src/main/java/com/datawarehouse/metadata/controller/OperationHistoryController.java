package com.datawarehouse.metadata.controller;

import com.datawarehouse.metadata.common.PageResult;
import com.datawarehouse.metadata.common.Result;
import com.datawarehouse.metadata.service.IOperationHistoryService;
import com.datawarehouse.metadata.vo.OperationHistoryVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 操作历史Controller
 *
 * @author System
 * @since 1.0.0
 */
@Api(tags = "操作历史管理")
@RestController
@RequestMapping("/api/metadata/history")
@RequiredArgsConstructor
public class OperationHistoryController {

    private final IOperationHistoryService historyService;

    @ApiOperation("获取表的操作历史")
    @GetMapping("/table/{tableId}")
    public Result<PageResult<OperationHistoryVO>> getByTableId(
            @ApiParam("表ID") @PathVariable Long tableId,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<OperationHistoryVO> result = historyService.getHistory(tableId, pageNum, pageSize);
        return Result.success(result);
    }

    @ApiOperation("获取用户的操作历史")
    @GetMapping("/operator/{operator}")
    public Result<PageResult<OperationHistoryVO>> getByOperator(
            @ApiParam("操作人") @PathVariable String operator,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<OperationHistoryVO> result = historyService.getHistoryByOperator(operator, pageNum, pageSize);
        return Result.success(result);
    }
}
