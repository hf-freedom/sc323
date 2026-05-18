package com.preorder.enums;

public enum OrderStatus {
    DEPOSIT_PENDING("待支付定金"),
    DEPOSIT_PAID("定金期(已付定金)"),
    BALANCE_PERIOD("尾款期(待付尾款)"),
    BALANCE_PAID("待发货"),
    SHIPPED("已发货"),
    COMPLETED("已完成"),
    CANCELLED("已取消"),
    DEPOSIT_TIMEOUT("定金超时"),
    BALANCE_TIMEOUT("尾款超时");

    private final String desc;

    OrderStatus(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
