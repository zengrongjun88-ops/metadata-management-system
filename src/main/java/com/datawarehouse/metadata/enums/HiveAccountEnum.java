package com.datawarehouse.metadata.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Hive账号(租户)枚举
 *
 * @author System
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum HiveAccountEnum {

    // 机票相关
    FLIGHT("flight", "机票业务账号"),
    FLIGHT_TECH("flight_tech", "机票技术账号"),

    // 酒店相关
    HOTEL("hotel", "酒店业务账号"),
    HOTEL_TECH("hotel_tech", "酒店技术账号"),

    // 火车票相关
    TRAIN("train", "火车票业务账号"),
    TRAIN_TECH("train_tech", "火车票技术账号"),

    // BI工程相关
    BIENG("bieng", "BI工程业务账号"),
    BIENG_TECH("bieng_tech", "BI工程技术账号"),

    // 其他
    COMMON("common", "公共账号");

    @EnumValue
    @JsonValue
    private final String account;

    private final String description;

    /**
     * 根据账号获取枚举
     *
     * @param account Hive账号
     * @return 枚举值
     */
    public static HiveAccountEnum getByAccount(String account) {
        if (account == null) {
            return null;
        }
        for (HiveAccountEnum value : HiveAccountEnum.values()) {
            if (value.getAccount().equals(account)) {
                return value;
            }
        }
        return null;
    }
}
