package com.datawarehouse.metadata.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审批流程实体类
 *
 * @author System
 * @since 1.0.0
 */
@Data
@TableName("approval_flow")
@ApiModel(description = "审批流程")
public class ApprovalFlow implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "审批单号")
    @TableField("flow_no")
    private String flowNo;

    @ApiModelProperty(value = "表ID")
    @TableField("table_id")
    private Long tableId;

    @ApiModelProperty(value = "审批类型(CREATE/UPDATE/DELETE)")
    @TableField("approval_type")
    private String approvalType;

    @ApiModelProperty(value = "审批状态(PENDING/APPROVED/REJECTED/CANCELLED/PUBLISHED)")
    @TableField("status")
    private String status;

    @ApiModelProperty(value = "提交人")
    @TableField("submitter")
    private String submitter;

    @ApiModelProperty(value = "提交时间")
    @TableField("submit_time")
    private LocalDateTime submitTime;

    @ApiModelProperty(value = "审批人")
    @TableField("approver")
    private String approver;

    @ApiModelProperty(value = "审批时间")
    @TableField("approve_time")
    private LocalDateTime approveTime;

    @ApiModelProperty(value = "审批意见")
    @TableField("approve_comment")
    private String approveComment;

    @ApiModelProperty(value = "变更内容(JSON)")
    @TableField("change_content")
    private String changeContent;

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
