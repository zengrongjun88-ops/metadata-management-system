package com.datawarehouse.metadata.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 元数据表实体类
 *
 * @author System
 * @since 1.0.0
 */
@Data
@TableName("metadata_table")
@ApiModel(description = "元数据表")
public class MetadataTable implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "表名")
    @TableField("table_name")
    private String tableName;

    @ApiModelProperty(value = "表注释")
    @TableField("table_comment")
    private String tableComment;

    @ApiModelProperty(value = "数据库名")
    @TableField("database_name")
    private String databaseName;

    @ApiModelProperty(value = "表类型")
    @TableField("table_type")
    private String tableType;

    @ApiModelProperty(value = "存储引擎")
    @TableField("engine")
    private String engine;

    @ApiModelProperty(value = "字符集")
    @TableField("charset")
    private String charset;

    @ApiModelProperty(value = "行数")
    @TableField("row_count")
    private Long rowCount;

    @ApiModelProperty(value = "数据大小(MB)")
    @TableField("data_size")
    private Double dataSize;

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
