package com.datawarehouse.metadata.controller;

import com.datawarehouse.metadata.common.PageResult;
import com.datawarehouse.metadata.common.Result;
import com.datawarehouse.metadata.dto.request.TableCreateRequest;
import com.datawarehouse.metadata.dto.request.TableSearchRequest;
import com.datawarehouse.metadata.dto.request.TableUpdateRequest;
import com.datawarehouse.metadata.service.IMetadataTableService;
import com.datawarehouse.metadata.vo.MetadataTableVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 元数据表Controller
 *
 * @author System
 * @since 1.0.0
 */
@Api(tags = "元数据表管理")
@RestController
@RequestMapping("/api/metadata/tables")
@RequiredArgsConstructor
public class MetadataTableController {

    private final IMetadataTableService metadataTableService;

    @ApiOperation("分页查询元数据表")
    @GetMapping("/page")
    public Result<PageResult<MetadataTableVO>> page(@Valid TableSearchRequest request) {
        PageResult<MetadataTableVO> result = metadataTableService.pageQuery(request);
        return Result.success(result);
    }

    @ApiOperation("根据ID查询元数据表详情")
    @GetMapping("/{id}")
    public Result<MetadataTableVO> getById(@ApiParam("表ID") @PathVariable Long id) {
        MetadataTableVO vo = metadataTableService.getTableById(id);
        return Result.success(vo);
    }

    @ApiOperation("根据数据库名和表名查询表详情")
    @GetMapping("/name")
    public Result<MetadataTableVO> getByName(
            @ApiParam("数据库名") @RequestParam String databaseName,
            @ApiParam("表名") @RequestParam String tableName) {
        MetadataTableVO vo = metadataTableService.getTableByName(databaseName, tableName);
        return Result.success(vo);
    }

    @ApiOperation("创建元数据表")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody TableCreateRequest request) {
        Long tableId = metadataTableService.createTable(request);
        return Result.success("创建成功", tableId);
    }

    @ApiOperation("更新元数据表")
    @PutMapping("/{id}")
    public Result<Void> update(
            @ApiParam("表ID") @PathVariable Long id,
            @Valid @RequestBody TableUpdateRequest request) {
        request.setId(id);
        metadataTableService.updateTable(request);
        return Result.success("更新成功", null);
    }

    @ApiOperation("删除元数据表")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@ApiParam("表ID") @PathVariable Long id) {
        metadataTableService.deleteTable(id);
        return Result.success("删除成功", null);
    }

    @ApiOperation("生成建表SQL")
    @PostMapping("/generate-sql")
    public Result<String> generateSql(@Valid @RequestBody TableCreateRequest request) {
        String sql = metadataTableService.generateCreateSql(request);
        return Result.success("生成成功", sql);
    }

    @ApiOperation("校验SQL语法")
    @PostMapping("/validate-sql")
    public Result<Void> validateSql(
            @ApiParam("SQL语句") @RequestParam String sql,
            @ApiParam("数据源类型") @RequestParam String dataSource) {
        metadataTableService.validateSql(sql, dataSource);
        return Result.success("校验通过", null);
    }
}

