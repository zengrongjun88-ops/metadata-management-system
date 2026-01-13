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

    @ApiModelProperty(value = "表描述(富文本)")
    @TableField("table_comment")
    private String tableComment;

    @ApiModelProperty(value = "数据库名")
    @TableField("database_name")
    private String databaseName;

    @ApiModelProperty(value = "数据源类型(Hive/Paimon/Iceberg/ClickHouse/BigQuery/StarRocks)")
    @TableField("data_source")
    private String dataSource;

    @ApiModelProperty(value = "Hive账号(租户)")
    @TableField("hive_account")
    private String hiveAccount;

    @ApiModelProperty(value = "表大小(字节)")
    @TableField("table_size")
    private Long tableSize;

    @ApiModelProperty(value = "数仓分层(ods/edw/cdm/mid/dim/dwd/dws/ads)")
    @TableField("warehouse_layer")
    private String warehouseLayer;

    @ApiModelProperty(value = "一级主题(usr/mkt/ord/fin/prd/prj/trf/srv)")
    @TableField("theme_first")
    private String themeFirst;

    @ApiModelProperty(value = "二级主题")
    @TableField("theme_second")
    private String themeSecond;

    @ApiModelProperty(value = "敏感等级(L1/L2/L3/L4)")
    @TableField("sensitivity_level")
    private String sensitivityLevel;

    @ApiModelProperty(value = "重要等级(P0/P1/P2/P3)")
    @TableField("importance_level")
    private String importanceLevel;

    @ApiModelProperty(value = "分区类型(FULL/INCR/NONE)")
    @TableField("partition_type")
    private String partitionType;

    @ApiModelProperty(value = "分区保留天数")
    @TableField("partition_retention_days")
    private Integer partitionRetentionDays;

    @ApiModelProperty(value = "更新频率(REALTIME/HOURLY/DAILY/WEEKLY/MONTHLY/ON_DEMAND)")
    @TableField("update_frequency")
    private String updateFrequency;

    @ApiModelProperty(value = "责任人(域账号)")
    @TableField("owner")
    private String owner;

    @ApiModelProperty(value = "自定义标签(JSON数组)")
    @TableField("custom_tags")
    private String customTags;

    @ApiModelProperty(value = "建表SQL")
    @TableField("create_sql")
    private String createSql;

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
