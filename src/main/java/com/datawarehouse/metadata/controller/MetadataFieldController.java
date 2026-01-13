package com.datawarehouse.metadata.controller;

import com.datawarehouse.metadata.common.Result;
import com.datawarehouse.metadata.dto.request.FieldCreateRequest;
import com.datawarehouse.metadata.dto.request.FieldUpdateRequest;
import com.datawarehouse.metadata.service.IMetadataFieldService;
import com.datawarehouse.metadata.vo.MetadataFieldVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 元数据字段Controller
 *
 * @author System
 * @since 1.0.0
 */
@Api(tags = "元数据字段管理")
@RestController
@RequestMapping("/api/metadata/fields")
@RequiredArgsConstructor
public class MetadataFieldController {

    private final IMetadataFieldService fieldService;

    @ApiOperation("根据表ID获取字段列表")
    @GetMapping("/table/{tableId}")
    public Result<List<MetadataFieldVO>> getByTableId(@ApiParam("表ID") @PathVariable Long tableId) {
        List<MetadataFieldVO> fields = fieldService.getFieldsByTableId(tableId);
        return Result.success(fields);
    }

    @ApiOperation("批量创建字段")
    @PostMapping("/batch")
    public Result<Void> batchCreate(
            @ApiParam("表ID") @RequestParam Long tableId,
            @Valid @RequestBody List<FieldCreateRequest> fields) {
        fieldService.batchCreateFields(tableId, fields);
        return Result.success("创建成功", null);
    }

    @ApiOperation("更新字段")
    @PutMapping("/{id}")
    public Result<Void> update(
            @ApiParam("字段ID") @PathVariable Long id,
            @Valid @RequestBody FieldUpdateRequest request) {
        fieldService.updateField(id, request);
        return Result.success("更新成功", null);
    }

    @ApiOperation("删除字段")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@ApiParam("字段ID") @PathVariable Long id) {
        fieldService.deleteField(id);
        return Result.success("删除成功", null);
    }
}
