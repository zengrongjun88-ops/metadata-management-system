package com.datawarehouse.metadata.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.*;

/**
 * 元数据字段更新请求
 *
 * @author System
 * @since 1.0.0
 */
@Data
@ApiModel(description = "元数据字段更新请求")
public class FieldUpdateRequest {

    @ApiModelProperty(value = "字段ID(更新已有字段时必填)", example = "1")
    private Long id;

    @ApiModelProperty(value = "字段序号", example = "1")
    @Min(value = 1, message = "字段序号必须大于0")
    private Integer fieldOrder;

    @ApiModelProperty(value = "字段名称", example = "user_id")
    @Size(max = 200, message = "字段名称长度不能超过200")
    @Pattern(regexp = "^[a-z][a-z0-9_]*$", message = "字段名称必须以小写字母开头,只能包含小写字母、数字和下划线")
    private String fieldName;

    @ApiModelProperty(value = "字段描述(支持富文本)", example = "<p>用户ID</p>")
    private String fieldComment;

    @ApiModelProperty(value = "字段类型", example = "BIGINT")
    @Size(max = 100, message = "字段类型长度不能超过100")
    private String fieldType;

    @ApiModelProperty(value = "是否主键(0-否,1-是)", example = "0")
    @Min(value = 0, message = "是否主键必须为0或1")
    @Max(value = 1, message = "是否主键必须为0或1")
    private Integer isPrimaryKey;

    @ApiModelProperty(value = "是否可为空(0-否,1-是)", example = "1")
    @Min(value = 0, message = "是否可为空必须为0或1")
    @Max(value = 1, message = "是否可为空必须为0或1")
    private Integer isNullable;

    @ApiModelProperty(value = "是否加密(0-否,1-是)", example = "0")
    @Min(value = 0, message = "是否加密必须为0或1")
    @Max(value = 1, message = "是否加密必须为0或1")
    private Integer isEncrypted;

    @ApiModelProperty(value = "是否分区键(0-否,1-是)", example = "0")
    @Min(value = 0, message = "是否分区键必须为0或1")
    @Max(value = 1, message = "是否分区键必须为0或1")
    private Integer isPartitionKey;

    @ApiModelProperty(value = "敏感等级", example = "L2")
    @Pattern(regexp = "^(L1|L2|L3|L4)?$", message = "敏感等级必须为: L1/L2/L3/L4")
    private String sensitivityLevel;

    @ApiModelProperty(value = "默认值", example = "0")
    @Size(max = 500, message = "默认值长度不能超过500")
    private String defaultValue;
}
