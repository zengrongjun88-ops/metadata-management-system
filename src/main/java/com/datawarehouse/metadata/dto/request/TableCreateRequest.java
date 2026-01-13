package com.datawarehouse.metadata.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.List;

/**
 * 元数据表创建请求
 *
 * @author System
 * @since 1.0.0
 */
@Data
@ApiModel(description = "元数据表创建请求")
public class TableCreateRequest {

    @ApiModelProperty(value = "表名", required = true, example = "user_order_info")
    @NotBlank(message = "表名不能为空")
    @Size(max = 200, message = "表名长度不能超过200")
    @Pattern(regexp = "^[a-z][a-z0-9_]*$", message = "表名必须以小写字母开头,只能包含小写字母、数字和下划线")
    private String tableName;

    @ApiModelProperty(value = "表描述(支持富文本)", example = "<p>用户订单信息表</p>")
    private String tableComment;

    @ApiModelProperty(value = "数据库名", required = true, example = "test_db")
    @NotBlank(message = "数据库名不能为空")
    @Size(max = 200, message = "数据库名长度不能超过200")
    private String databaseName;

    @ApiModelProperty(value = "数据源类型", required = true, example = "Hive")
    @NotBlank(message = "数据源类型不能为空")
    @Pattern(regexp = "^(Hive|Paimon|Iceberg|ClickHouse|BigQuery|StarRocks)$",
             message = "数据源类型必须为: Hive/Paimon/Iceberg/ClickHouse/BigQuery/StarRocks")
    private String dataSource;

    @ApiModelProperty(value = "Hive账号(租户)", example = "flight")
    @Size(max = 100, message = "Hive账号长度不能超过100")
    private String hiveAccount;

    @ApiModelProperty(value = "表大小(字节)", example = "1073741824")
    @Min(value = 0, message = "表大小不能为负数")
    private Long tableSize;

    @ApiModelProperty(value = "数仓分层", example = "cdm")
    @Pattern(regexp = "^(ods|edw|cdm|mid|dim|dwd|dws|ads)?$",
             message = "数仓分层必须为: ods/edw/cdm/mid/dim/dwd/dws/ads")
    private String warehouseLayer;

    @ApiModelProperty(value = "一级主题", example = "ord")
    @Pattern(regexp = "^(usr|mkt|ord|fin|prd|prj|trf|srv)?$",
             message = "一级主题必须为: usr/mkt/ord/fin/prd/prj/trf/srv")
    private String themeFirst;

    @ApiModelProperty(value = "二级主题", example = "flight")
    @Size(max = 50, message = "二级主题长度不能超过50")
    private String themeSecond;

    @ApiModelProperty(value = "敏感等级", example = "L2")
    @Pattern(regexp = "^(L1|L2|L3|L4)?$", message = "敏感等级必须为: L1/L2/L3/L4")
    private String sensitivityLevel;

    @ApiModelProperty(value = "重要等级", example = "P1")
    @Pattern(regexp = "^(P0|P1|P2|P3)?$", message = "重要等级必须为: P0/P1/P2/P3")
    private String importanceLevel;

    @ApiModelProperty(value = "分区类型", example = "INCR")
    @Pattern(regexp = "^(FULL|INCR|NONE)?$", message = "分区类型必须为: FULL/INCR/NONE")
    private String partitionType;

    @ApiModelProperty(value = "分区保留天数", example = "90")
    @Min(value = 1, message = "分区保留天数必须大于0")
    @Max(value = 3650, message = "分区保留天数不能超过3650天(10年)")
    private Integer partitionRetentionDays;

    @ApiModelProperty(value = "更新频率", example = "DAILY")
    @Pattern(regexp = "^(REALTIME|HOURLY|DAILY|WEEKLY|MONTHLY|ON_DEMAND)?$",
             message = "更新频率必须为: REALTIME/HOURLY/DAILY/WEEKLY/MONTHLY/ON_DEMAND")
    private String updateFrequency;

    @ApiModelProperty(value = "责任人(域账号)", example = "admin")
    @Size(max = 100, message = "责任人长度不能超过100")
    private String owner;

    @ApiModelProperty(value = "自定义标签(JSON数组)", example = "[\"核心表\",\"每日更新\"]")
    @Size(max = 500, message = "自定义标签长度不能超过500")
    private String customTags;

    @ApiModelProperty(value = "建表SQL")
    private String createSql;

    @ApiModelProperty(value = "字段列表", required = true)
    @NotNull(message = "字段列表不能为空")
    @Size(min = 1, message = "至少需要一个字段")
    @Valid
    private List<FieldCreateRequest> fields;
}
