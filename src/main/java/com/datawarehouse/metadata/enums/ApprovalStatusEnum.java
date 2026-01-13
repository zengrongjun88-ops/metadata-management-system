package com.datawarehouse.metadata.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审批状态枚举
 *
 * @author System
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum ApprovalStatusEnum {

    DRAFT("DRAFT", "草稿", "Draft"),
    PENDING("PENDING", "待审批", "Pending"),
    APPROVED("APPROVED", "已通过", "Approved"),
    REJECTED("REJECTED", "已拒绝", "Rejected"),
    CANCELLED("CANCELLED", "已取消", "Cancelled"),
    PUBLISHED("PUBLISHED", "已发布", "Published");

    @EnumValue
    @JsonValue
    private final String code;

    private final String chineseName;

    private final String englishName;

    /**
     * 根据code获取枚举
     *
     * @param code 审批状态代码
     * @return 枚举值
     */
    public static ApprovalStatusEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (ApprovalStatusEnum value : ApprovalStatusEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 判断是否可以进行审批操作
     *
     * @return 是否可以审批
     */
    public boolean canApprove() {
        return this == PENDING;
    }

    /**
     * 判断是否可以发布
     *
     * @return 是否可以发布
     */
    public boolean canPublish() {
        return this == APPROVED;
    }
}
