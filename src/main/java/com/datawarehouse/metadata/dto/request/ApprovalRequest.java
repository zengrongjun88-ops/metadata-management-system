package com.datawarehouse.metadata.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * 审批请求
 *
 * @author System
 * @since 1.0.0
 */
@Data
@ApiModel(description = "审批请求")
public class ApprovalRequest {

    @ApiModelProperty(value = "表ID", required = true, example = "1")
    @NotNull(message = "表ID不能为空")
    private Long tableId;

    @ApiModelProperty(value = "审批类型", required = true, example = "CREATE")
    @NotBlank(message = "审批类型不能为空")
    @Pattern(regexp = "^(CREATE|UPDATE|DELETE)$", message = "审批类型必须为: CREATE/UPDATE/DELETE")
    private String approvalType;

    @ApiModelProperty(value = "变更内容(JSON)", example = "{\"tableName\":\"test_table\"}")
    private String changeContent;

    @ApiModelProperty(value = "提交说明", example = "新增用户订单表")
    private String submitComment;
}
