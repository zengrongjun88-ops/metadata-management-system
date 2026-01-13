package com.datawarehouse.metadata.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 审批操作请求(通过/拒绝)
 *
 * @author System
 * @since 1.0.0
 */
@Data
@ApiModel(description = "审批操作请求")
public class ApprovalActionRequest {

    @ApiModelProperty(value = "操作类型", required = true, example = "APPROVE")
    @NotBlank(message = "操作类型不能为空")
    @Pattern(regexp = "^(APPROVE|REJECT)$", message = "操作类型必须为: APPROVE/REJECT")
    private String action;

    @ApiModelProperty(value = "审批意见", example = "同意创建该表")
    @Size(max = 500, message = "审批意见长度不能超过500")
    private String comment;
}
