package com.datawarehouse.metadata.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 元数据字段实体类
 *
 * @author System
 * @since 1.0.0
 */
@Data
@TableName("metadata_field")
@ApiModel(description = "元数据字段")
public class MetadataField implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "表ID")
    @TableField("table_id")
    private Long tableId;

    @ApiModelProperty(value = "字段序号")
    @TableField("field_order")
    private Integer fieldOrder;

    @ApiModelProperty(value = "字段名称")
    @TableField("field_name")
    private String fieldName;

    @ApiModelProperty(value = "字段描述(富文本)")
    @TableField("field_comment")
    private String fieldComment;

    @ApiModelProperty(value = "字段类型")
    @TableField("field_type")
    private String fieldType;

    @ApiModelProperty(value = "是否主键(0-否,1-是)")
    @TableField("is_primary_key")
    private Integer isPrimaryKey;

    @ApiModelProperty(value = "是否可为空(0-否,1-是)")
    @TableField("is_nullable")
    private Integer isNullable;

    @ApiModelProperty(value = "是否加密(0-否,1-是)")
    @TableField("is_encrypted")
    private Integer isEncrypted;

    @ApiModelProperty(value = "是否分区键(0-否,1-是)")
    @TableField("is_partition_key")
    private Integer isPartitionKey;

    @ApiModelProperty(value = "敏感等级(L1/L2/L3/L4)")
    @TableField("sensitivity_level")
    private String sensitivityLevel;

    @ApiModelProperty(value = "默认值")
    @TableField("default_value")
    private String defaultValue;

    @ApiModelProperty(value = "创建人")
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;

    @ApiModelProperty(value = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新人")
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @ApiModelProperty(value = "更新时间")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "是否删除(0-未删除,1-已删除)")
    @TableField("deleted")
    @TableLogic
    private Integer deleted;
}
