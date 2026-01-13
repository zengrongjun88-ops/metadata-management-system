package com.datawarehouse.metadata.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数仓分层枚举
 *
 * @author System
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum WarehouseLayerEnum {

    ODS("ods", "操作数据层", "Operational Data Store", 1),
    EDW("edw", "企业数据仓库层", "Enterprise Data Warehouse", 2),
    CDM("cdm", "公共数据模型层", "Common Data Model", 3),
    MID("mid", "中间层", "Middle Layer", 4),
    DIM("dim", "维度层", "Dimension Layer", 5),
    DWD("dwd", "明细数据层", "Data Warehouse Detail", 6),
    DWS("dws", "汇总数据层", "Data Warehouse Summary", 7),
    ADS("ads", "应用数据层", "Application Data Store", 8);

    @EnumValue
    @JsonValue
    private final String code;

    private final String name;

    private final String fullName;

    private final Integer level;

    /**
     * 根据code获取枚举
     *
     * @param code 分层代码
     * @return 枚举值
     */
    public static WarehouseLayerEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (WarehouseLayerEnum value : WarehouseLayerEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
