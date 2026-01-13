package com.datawarehouse.metadata.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * 元数据表搜索请求
 *
 * @author System
 * @since 1.0.0
 */
@Data
@ApiModel(description = "元数据表搜索请求")
public class TableSearchRequest {

    @ApiModelProperty(value = "当前页码", example = "1")
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;

    @ApiModelProperty(value = "每页大小", example = "10")
    @Min(value = 1, message = "每页大小必须大于0")
    @Max(value = 100, message = "每页大小不能超过100")
    private Integer pageSize = 10;

    @ApiModelProperty(value = "关键词(搜索表名、表描述)", example = "user")
    private String keyword;

    @ApiModelProperty(value = "数据库名", example = "test_db")
    private String databaseName;

    @ApiModelProperty(value = "数据源类型", example = "Hive")
    private String dataSource;

    @ApiModelProperty(value = "Hive账号", example = "flight")
    private String hiveAccount;

    @ApiModelProperty(value = "数仓分层", example = "cdm")
    private String warehouseLayer;

    @ApiModelProperty(value = "一级主题", example = "ord")
    private String themeFirst;

    @ApiModelProperty(value = "二级主题", example = "flight")
    private String themeSecond;

    @ApiModelProperty(value = "敏感等级", example = "L2")
    private String sensitivityLevel;

    @ApiModelProperty(value = "重要等级", example = "P1")
    private String importanceLevel;

    @ApiModelProperty(value = "更新频率", example = "DAILY")
    private String updateFrequency;

    @ApiModelProperty(value = "责任人", example = "admin")
    private String owner;

    @ApiModelProperty(value = "排序字段", example = "create_time")
    private String sortField;

    @ApiModelProperty(value = "排序方向(ASC/DESC)", example = "DESC")
    private String sortOrder = "DESC";
}
