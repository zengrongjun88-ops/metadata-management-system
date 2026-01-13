package com.datawarehouse.metadata.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 分区类型枚举
 *
 * @author System
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum PartitionTypeEnum {

    FULL("FULL", "全量分区", "每个分区包含全量数据快照"),
    INCR("INCR", "增量分区", "每个分区仅包含增量变更数据"),
    NONE("NONE", "无分区", "表不分区");

    @EnumValue
    @JsonValue
    private final String code;

    private final String name;

    private final String description;

    /**
     * 根据code获取枚举
     *
     * @param code 分区类型代码
     * @return 枚举值
     */
    public static PartitionTypeEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (PartitionTypeEnum value : PartitionTypeEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
