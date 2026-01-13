package com.datawarehouse.metadata.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 更新频率枚举
 *
 * @author System
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum UpdateFrequencyEnum {

    REALTIME("REALTIME", "实时", "Real-time", "分钟级实时更新"),
    HOURLY("HOURLY", "小时", "Hourly", "每小时更新"),
    DAILY("DAILY", "天", "Daily", "每天更新(T+1)"),
    WEEKLY("WEEKLY", "周", "Weekly", "每周更新"),
    MONTHLY("MONTHLY", "月", "Monthly", "每月更新"),
    ON_DEMAND("ON_DEMAND", "按需", "On Demand", "手动触发更新");

    @EnumValue
    @JsonValue
    private final String code;

    private final String name;

    private final String englishName;

    private final String description;

    /**
     * 根据code获取枚举
     *
     * @param code 更新频率代码
     * @return 枚举值
     */
    public static UpdateFrequencyEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (UpdateFrequencyEnum value : UpdateFrequencyEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
