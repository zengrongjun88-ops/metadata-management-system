package com.datawarehouse.metadata.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 血缘图谱视图对象(预留)
 *
 * @author System
 * @since 1.0.0
 */
@Data
@ApiModel(description = "血缘图谱视图对象")
public class LineageGraphVO {

    @ApiModelProperty(value = "当前表ID")
    private Long tableId;

    @ApiModelProperty(value = "当前表名")
    private String tableName;

    @ApiModelProperty(value = "数据库名")
    private String databaseName;

    @ApiModelProperty(value = "图谱类型(UPSTREAM-上游/DOWNSTREAM-下游)")
    private String lineageType;

    @ApiModelProperty(value = "查询深度")
    private Integer depth;

    @ApiModelProperty(value = "节点列表")
    private List<LineageNode> nodes;

    @ApiModelProperty(value = "边列表")
    private List<LineageEdge> edges;

    /**
     * 血缘节点
     */
    @Data
    @ApiModel(description = "血缘节点")
    public static class LineageNode {
        @ApiModelProperty(value = "节点ID")
        private String nodeId;

        @ApiModelProperty(value = "节点类型(TABLE/FIELD)")
        private String nodeType;

        @ApiModelProperty(value = "表ID")
        private Long tableId;

        @ApiModelProperty(value = "表名")
        private String tableName;

        @ApiModelProperty(value = "数据库名")
        private String databaseName;

        @ApiModelProperty(value = "字段名(节点类型为FIELD时)")
        private String fieldName;

        @ApiModelProperty(value = "层级")
        private Integer level;
    }

    /**
     * 血缘边
     */
    @Data
    @ApiModel(description = "血缘边")
    public static class LineageEdge {
        @ApiModelProperty(value = "边ID")
        private String edgeId;

        @ApiModelProperty(value = "源节点ID")
        private String sourceNodeId;

        @ApiModelProperty(value = "目标节点ID")
        private String targetNodeId;

        @ApiModelProperty(value = "血缘类型(DERIVES_FROM/FEEDS_INTO)")
        private String edgeType;

        @ApiModelProperty(value = "转换逻辑(SQL片段)")
        private String transformLogic;
    }
}
