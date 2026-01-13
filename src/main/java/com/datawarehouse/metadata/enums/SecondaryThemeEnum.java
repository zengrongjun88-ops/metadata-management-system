package com.datawarehouse.metadata.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 二级主题枚举(与一级主题级联)
 *
 * @author System
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum SecondaryThemeEnum {

    // 用户域(usr)二级主题
    USR_CON("con", "Consumer", "消费者", PrimaryThemeEnum.USR),
    USR_MEM("mem", "Member", "会员", PrimaryThemeEnum.USR),
    USR_UBT("ubt", "User Behavior Tracking", "用户行为追踪", PrimaryThemeEnum.USR),
    USR_PROFILE("profile", "User Profile", "用户画像", PrimaryThemeEnum.USR),

    // 营销域(mkt)二级主题
    MKT_CAMPAIGN("campaign", "Campaign", "营销活动", PrimaryThemeEnum.MKT),
    MKT_COUPON("coupon", "Coupon", "优惠券", PrimaryThemeEnum.MKT),
    MKT_CHANNEL("channel", "Channel", "渠道", PrimaryThemeEnum.MKT),

    // 订单域(ord)二级主题
    ORD_FLIGHT("flight", "Flight Order", "机票订单", PrimaryThemeEnum.ORD),
    ORD_HOTEL("hotel", "Hotel Order", "酒店订单", PrimaryThemeEnum.ORD),
    ORD_TRAIN("train", "Train Order", "火车票订单", PrimaryThemeEnum.ORD),
    ORD_PAY("pay", "Payment", "支付", PrimaryThemeEnum.ORD),
    ORD_REFUND("refund", "Refund", "退款", PrimaryThemeEnum.ORD),

    // 财务域(fin)二级主题
    FIN_SETTLE("settle", "Settlement", "结算", PrimaryThemeEnum.FIN),
    FIN_INVOICE("invoice", "Invoice", "发票", PrimaryThemeEnum.FIN),
    FIN_COST("cost", "Cost", "成本", PrimaryThemeEnum.FIN),

    // 产品域(prd)二级主题
    PRD_INFO("info", "Product Info", "产品信息", PrimaryThemeEnum.PRD),
    PRD_INVENTORY("inventory", "Inventory", "库存", PrimaryThemeEnum.PRD),
    PRD_PRICE("price", "Price", "价格", PrimaryThemeEnum.PRD),

    // 流量域(trf)二级主题
    TRF_ACCESS("access", "Access", "访问", PrimaryThemeEnum.TRF),
    TRF_CLICK("click", "Click Stream", "点击流", PrimaryThemeEnum.TRF),

    // 服务域(srv)二级主题
    SRV_TICKET("ticket", "Service Ticket", "服务工单", PrimaryThemeEnum.SRV),
    SRV_COMPLAINT("complaint", "Complaint", "投诉", PrimaryThemeEnum.SRV);

    @EnumValue
    @JsonValue
    private final String code;

    private final String englishName;

    private final String chineseName;

    private final PrimaryThemeEnum primaryTheme;

    /**
     * 根据code获取枚举
     *
     * @param code 二级主题代码
     * @return 枚举值
     */
    public static SecondaryThemeEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (SecondaryThemeEnum value : SecondaryThemeEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 根据一级主题获取所有二级主题
     *
     * @param primaryTheme 一级主题
     * @return 二级主题列表
     */
    public static SecondaryThemeEnum[] getByPrimaryTheme(PrimaryThemeEnum primaryTheme) {
        if (primaryTheme == null) {
            return new SecondaryThemeEnum[0];
        }
        return java.util.Arrays.stream(SecondaryThemeEnum.values())
                .filter(item -> item.getPrimaryTheme() == primaryTheme)
                .toArray(SecondaryThemeEnum[]::new);
    }
}
