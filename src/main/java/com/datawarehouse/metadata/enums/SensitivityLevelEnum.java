package com.datawarehouse.metadata.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 敏感等级枚举
 *
 * @author System
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum SensitivityLevelEnum {

    L1("L1", "公开", "Public", "无敏感信息,可公开访问"),
    L2("L2", "内部", "Internal", "内部数据,需登录访问"),
    L3("L3", "敏感", "Sensitive", "敏感数据,需申请权限"),
    L4("L4", "高度敏感", "Highly Sensitive", "高度敏感,严格权限控制");

    @EnumValue
    @JsonValue
    private final String code;

    private final String name;

    private final String englishName;

    private final String description;

    /**
     * 根据code获取枚举
     *
     * @param code 敏感等级代码
     * @return 枚举值
     */
    public static SensitivityLevelEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (SensitivityLevelEnum value : SensitivityLevelEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
