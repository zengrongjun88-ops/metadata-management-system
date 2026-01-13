package com.datawarehouse.metadata.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据源类型枚举
 *
 * @author System
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum DataSourceTypeEnum {

    /**
     * Apache Hive
     */
    HIVE("Hive", "Apache Hive"),

    /**
     * Apache Paimon（流批一体）
     */
    PAIMON("Paimon", "Apache Paimon"),

    /**
     * Apache Iceberg（数据湖）
     */
    ICEBERG("Iceberg", "Apache Iceberg"),

    /**
     * ClickHouse（OLAP数据库）
     */
    CLICKHOUSE("ClickHouse", "ClickHouse"),

    /**
     * Google BigQuery（云原生数仓）
     */
    BIGQUERY("BigQuery", "Google BigQuery"),

    /**
     * StarRocks（MPP数据库）
     */
    STARROCKS("StarRocks", "StarRocks");

    /**
     * 数据源代码（存储到数据库的值）
     */
    @EnumValue
    @JsonValue
    private final String code;

    /**
     * 数据源描述
     */
    private final String description;

    /**
     * 根据code获取枚举
     *
     * @param code 数据源代码
     * @return 枚举值
     */
    public static DataSourceTypeEnum getByCode(String code) {
        for (DataSourceTypeEnum dataSourceType : values()) {
            if (dataSourceType.getCode().equals(code)) {
                return dataSourceType;
            }
        }
        throw new IllegalArgumentException("Invalid DataSourceType code: " + code);
    }
}
