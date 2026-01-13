package com.datawarehouse.metadata.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作历史实体类
 *
 * @author System
 * @since 1.0.0
 */
@Data
@TableName("operation_history")
@ApiModel(description = "操作历史")
public class OperationHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "表ID")
    @TableField("table_id")
    private Long tableId;

    @ApiModelProperty(value = "操作类型(CREATE/UPDATE/DELETE/PUBLISH/APPROVE/REJECT)")
    @TableField("operation_type")
    private String operationType;

    @ApiModelProperty(value = "操作人")
    @TableField("operator")
    private String operator;

    @ApiModelProperty(value = "操作时间")
    @TableField("operation_time")
    private LocalDateTime operationTime;

    @ApiModelProperty(value = "变更前内容(JSON)")
    @TableField("before_content")
    private String beforeContent;

    @ApiModelProperty(value = "变更后内容(JSON)")
    @TableField("after_content")
    private String afterContent;

    @ApiModelProperty(value = "操作描述")
    @TableField("operation_desc")
    private String operationDesc;

    @ApiModelProperty(value = "创建人")
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;

    @ApiModelProperty(value = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty(value = "是否删除(0-未删除,1-已删除)")
    @TableField("deleted")
    @TableLogic
    private Integer deleted;
}
