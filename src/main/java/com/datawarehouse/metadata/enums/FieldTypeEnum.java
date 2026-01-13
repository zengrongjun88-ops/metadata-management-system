package com.datawarehouse.metadata.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 字段类型枚举(通用基础类型)
 *
 * @author System
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum FieldTypeEnum {

    // 数值类型
    TINYINT("TINYINT", "TINYINT"),
    SMALLINT("SMALLINT", "SMALLINT"),
    INT("INT", "INT"),
    BIGINT("BIGINT", "BIGINT"),
    FLOAT("FLOAT", "FLOAT"),
    DOUBLE("DOUBLE", "DOUBLE"),
    DECIMAL("DECIMAL", "DECIMAL"),

    // 字符串类型
    STRING("STRING", "STRING"),
    VARCHAR("VARCHAR", "VARCHAR"),
    CHAR("CHAR", "CHAR"),
    TEXT("TEXT", "TEXT"),

    // 日期时间类型
    DATE("DATE", "DATE"),
    DATETIME("DATETIME", "DATETIME"),
    TIMESTAMP("TIMESTAMP", "TIMESTAMP"),
    TIME("TIME", "TIME"),

    // 布尔类型
    BOOLEAN("BOOLEAN", "BOOLEAN"),

    // 二进制类型
    BINARY("BINARY", "BINARY"),

    // 复杂类型
    ARRAY("ARRAY", "ARRAY"),
    MAP("MAP", "MAP"),
    STRUCT("STRUCT", "STRUCT"),
    JSON("JSON", "JSON");

    @EnumValue
    @JsonValue
    private final String code;

    private final String description;

    /**
     * 根据code获取枚举
     *
     * @param code 字段类型代码
     * @return 枚举值
     */
    public static FieldTypeEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (FieldTypeEnum value : FieldTypeEnum.values()) {
            if (value.getCode().equalsIgnoreCase(code)) {
                return value;
            }
        }
        return null;
    }
}
