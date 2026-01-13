package com.datawarehouse.metadata.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 重要等级枚举
 *
 * @author System
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum ImportanceLevelEnum {

    P0("P0", "核心", "核心业务表,影响线上服务"),
    P1("P1", "重要", "重要业务表,影响核心报表"),
    P2("P2", "一般", "一般业务表,影响部分分析"),
    P3("P3", "低", "临时表或测试表,影响较小");

    @EnumValue
    @JsonValue
    private final String code;

    private final String name;

    private final String description;

    /**
     * 根据code获取枚举
     *
     * @param code 重要等级代码
     * @return 枚举值
     */
    public static ImportanceLevelEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (ImportanceLevelEnum value : ImportanceLevelEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
