package com.datawarehouse.metadata.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 元数据字段视图对象
 *
 * @author System
 * @since 1.0.0
 */
@Data
@ApiModel(description = "元数据字段视图对象")
public class MetadataFieldVO {

    @ApiModelProperty(value = "主键ID")
    private Long id;

    @ApiModelProperty(value = "表ID")
    private Long tableId;

    @ApiModelProperty(value = "字段序号")
    private Integer fieldOrder;

    @ApiModelProperty(value = "字段名称")
    private String fieldName;

    @ApiModelProperty(value = "字段描述(富文本)")
    private String fieldComment;

    @ApiModelProperty(value = "字段类型")
    private String fieldType;

    @ApiModelProperty(value = "是否主键(0-否,1-是)")
    private Integer isPrimaryKey;

    @ApiModelProperty(value = "是否可为空(0-否,1-是)")
    private Integer isNullable;

    @ApiModelProperty(value = "是否加密(0-否,1-是)")
    private Integer isEncrypted;

    @ApiModelProperty(value = "是否分区键(0-否,1-是)")
    private Integer isPartitionKey;

    @ApiModelProperty(value = "敏感等级")
    private String sensitivityLevel;

    @ApiModelProperty(value = "默认值")
    private String defaultValue;

    @ApiModelProperty(value = "创建人")
    private String createBy;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新人")
    private String updateBy;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;
}
