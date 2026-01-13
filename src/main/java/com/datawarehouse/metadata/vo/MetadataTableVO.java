package com.datawarehouse.metadata.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 元数据表视图对象
 *
 * @author System
 * @since 1.0.0
 */
@Data
@ApiModel(description = "元数据表视图对象")
public class MetadataTableVO {

    @ApiModelProperty(value = "主键ID")
    private Long id;

    @ApiModelProperty(value = "表名")
    private String tableName;

    @ApiModelProperty(value = "表描述(富文本)")
    private String tableComment;

    @ApiModelProperty(value = "数据库名")
    private String databaseName;

    @ApiModelProperty(value = "数据源类型")
    private String dataSource;

    @ApiModelProperty(value = "Hive账号(租户)")
    private String hiveAccount;

    @ApiModelProperty(value = "表大小(字节)")
    private Long tableSize;

    @ApiModelProperty(value = "数仓分层")
    private String warehouseLayer;

    @ApiModelProperty(value = "一级主题")
    private String themeFirst;

    @ApiModelProperty(value = "二级主题")
    private String themeSecond;

    @ApiModelProperty(value = "敏感等级")
    private String sensitivityLevel;

    @ApiModelProperty(value = "重要等级")
    private String importanceLevel;

    @ApiModelProperty(value = "分区类型")
    private String partitionType;

    @ApiModelProperty(value = "分区保留天数")
    private Integer partitionRetentionDays;

    @ApiModelProperty(value = "更新频率")
    private String updateFrequency;

    @ApiModelProperty(value = "责任人")
    private String owner;

    @ApiModelProperty(value = "自定义标签(JSON数组)")
    private String customTags;

    @ApiModelProperty(value = "建表SQL")
    private String createSql;

    @ApiModelProperty(value = "创建人")
    private String createBy;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新人")
    private String updateBy;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "字段列表")
    private List<MetadataFieldVO> fields;
}
