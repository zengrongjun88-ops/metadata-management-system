package com.datawarehouse.metadata.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 表搜索结果视图对象
 *
 * @author System
 * @since 1.0.0
 */
@Data
@ApiModel(description = "表搜索结果视图对象")
public class TableSearchVO {

    @ApiModelProperty(value = "主键ID")
    private Long id;

    @ApiModelProperty(value = "表名")
    private String tableName;

    @ApiModelProperty(value = "表名(高亮)")
    private String tableNameHighlight;

    @ApiModelProperty(value = "表描述")
    private String tableComment;

    @ApiModelProperty(value = "表描述(高亮)")
    private String tableCommentHighlight;

    @ApiModelProperty(value = "数据库名")
    private String databaseName;

    @ApiModelProperty(value = "数据源类型")
    private String dataSource;

    @ApiModelProperty(value = "Hive账号")
    private String hiveAccount;

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

    @ApiModelProperty(value = "更新频率")
    private String updateFrequency;

    @ApiModelProperty(value = "责任人")
    private String owner;

    @ApiModelProperty(value = "表大小(字节)")
    private Long tableSize;

    @ApiModelProperty(value = "字段数量")
    private Integer fieldCount;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "搜索得分(ES相关性得分)")
    private Double score;
}
