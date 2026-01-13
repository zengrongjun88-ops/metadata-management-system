package com.datawarehouse.metadata.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 一级主题枚举
 *
 * @author System
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum PrimaryThemeEnum {

    USR("usr", "User", "用户域", "用户相关数据"),
    MKT("mkt", "Marketing", "营销域", "营销活动相关数据"),
    ORD("ord", "Order", "订单域", "订单交易相关数据"),
    FIN("fin", "Finance", "财务域", "财务结算相关数据"),
    PRD("prd", "Product", "产品域", "产品信息相关数据"),
    PRJ("prj", "Project Data", "项目数据域", "项目分析相关数据"),
    TRF("trf", "Traffic", "流量域", "流量行为相关数据"),
    SRV("srv", "Service", "服务域", "客户服务相关数据");

    @EnumValue
    @JsonValue
    private final String code;

    private final String englishName;

    private final String chineseName;

    private final String description;

    /**
     * 根据code获取枚举
     *
     * @param code 主题代码
     * @return 枚举值
     */
    public static PrimaryThemeEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (PrimaryThemeEnum value : PrimaryThemeEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
