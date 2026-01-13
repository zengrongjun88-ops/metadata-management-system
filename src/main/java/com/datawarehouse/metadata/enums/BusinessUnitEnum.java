package com.datawarehouse.metadata.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业务单元枚举
 *
 * @author System
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum BusinessUnitEnum {

    FLIGHT("flight", "机票事业部"),
    HOTEL("hotel", "酒店事业部"),
    TRAIN("train", "火车票事业部"),
    IBU("ibu", "国际业务事业部"),
    CONTENT("content", "内容事业部");

    @EnumValue
    @JsonValue
    private final String code;

    private final String name;

    /**
     * 根据code获取枚举
     *
     * @param code 业务单元代码
     * @return 枚举值
     */
    public static BusinessUnitEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (BusinessUnitEnum value : BusinessUnitEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
