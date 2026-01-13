package com.datawarehouse.metadata.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作历史视图对象
 *
 * @author System
 * @since 1.0.0
 */
@Data
@ApiModel(description = "操作历史视图对象")
public class OperationHistoryVO {

    @ApiModelProperty(value = "主键ID")
    private Long id;

    @ApiModelProperty(value = "表ID")
    private Long tableId;

    @ApiModelProperty(value = "表名(关联查询)")
    private String tableName;

    @ApiModelProperty(value = "数据库名(关联查询)")
    private String databaseName;

    @ApiModelProperty(value = "操作类型")
    private String operationType;

    @ApiModelProperty(value = "操作人")
    private String operator;

    @ApiModelProperty(value = "操作时间")
    private LocalDateTime operationTime;

    @ApiModelProperty(value = "变更前内容(JSON)")
    private String beforeContent;

    @ApiModelProperty(value = "变更后内容(JSON)")
    private String afterContent;

    @ApiModelProperty(value = "操作描述")
    private String operationDesc;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;
}
