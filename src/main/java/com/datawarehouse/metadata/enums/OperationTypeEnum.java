package com.datawarehouse.metadata.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 操作类型枚举
 *
 * @author System
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum OperationTypeEnum {

    CREATE("CREATE", "创建", "Create"),
    UPDATE("UPDATE", "更新", "Update"),
    DELETE("DELETE", "删除", "Delete"),
    PUBLISH("PUBLISH", "发布", "Publish"),
    APPROVE("APPROVE", "审批通过", "Approve"),
    REJECT("REJECT", "审批拒绝", "Reject"),
    QUERY("QUERY", "查询", "Query");

    @EnumValue
    @JsonValue
    private final String code;

    private final String chineseName;

    private final String englishName;

    /**
     * 根据code获取枚举
     *
     * @param code 操作类型代码
     * @return 枚举值
     */
    public static OperationTypeEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (OperationTypeEnum value : OperationTypeEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
