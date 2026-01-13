package com.datawarehouse.metadata.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审批流程视图对象
 *
 * @author System
 * @since 1.0.0
 */
@Data
@ApiModel(description = "审批流程视图对象")
public class ApprovalFlowVO {

    @ApiModelProperty(value = "主键ID")
    private Long id;

    @ApiModelProperty(value = "审批单号")
    private String flowNo;

    @ApiModelProperty(value = "表ID")
    private Long tableId;

    @ApiModelProperty(value = "表名(关联查询)")
    private String tableName;

    @ApiModelProperty(value = "数据库名(关联查询)")
    private String databaseName;

    @ApiModelProperty(value = "审批类型")
    private String approvalType;

    @ApiModelProperty(value = "审批状态")
    private String status;

    @ApiModelProperty(value = "提交人")
    private String submitter;

    @ApiModelProperty(value = "提交时间")
    private LocalDateTime submitTime;

    @ApiModelProperty(value = "审批人")
    private String approver;

    @ApiModelProperty(value = "审批时间")
    private LocalDateTime approveTime;

    @ApiModelProperty(value = "审批意见")
    private String approveComment;

    @ApiModelProperty(value = "变更内容(JSON)")
    private String changeContent;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;
}
