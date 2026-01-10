package com.datawarehouse.metadata.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.datawarehouse.metadata.common.Result;
import com.datawarehouse.metadata.entity.MetadataTable;
import com.datawarehouse.metadata.service.IMetadataTableService;
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
@RequestMapping("/metadata/table")
@RequiredArgsConstructor
public class MetadataTableController {

    private final IMetadataTableService metadataTableService;

    @ApiOperation("分页查询元数据表")
    @GetMapping("/page")
    public Result<Page<MetadataTable>> page(
            @ApiParam("当前页") @RequestParam(defaultValue = "1") Long current,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Long size,
            @ApiParam("表名") @RequestParam(required = false) String tableName,
            @ApiParam("数据库名") @RequestParam(required = false) String databaseName) {
        Page<MetadataTable> page = new Page<>(current, size);
        Page<MetadataTable> result = metadataTableService.pageMetadataTable(page, tableName, databaseName);
        return Result.success(result);
    }

    @ApiOperation("根据ID查询元数据表")
    @GetMapping("/{id}")
    public Result<MetadataTable> getById(@ApiParam("主键ID") @PathVariable Long id) {
        MetadataTable metadataTable = metadataTableService.getById(id);
        return Result.success(metadataTable);
    }

    @ApiOperation("新增元数据表")
    @PostMapping
    public Result<Void> save(@Valid @RequestBody MetadataTable metadataTable) {
        metadataTableService.save(metadataTable);
        return Result.success("新增成功", null);
    }

    @ApiOperation("更新元数据表")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody MetadataTable metadataTable) {
        metadataTableService.updateById(metadataTable);
        return Result.success("更新成功", null);
    }

    @ApiOperation("删除元数据表")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@ApiParam("主键ID") @PathVariable Long id) {
        metadataTableService.removeById(id);
        return Result.success("删除成功", null);
    }

}
