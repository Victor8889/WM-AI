package com.hncboy.beehive.base.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author ll
 * @date 2023-9-3
 * 订单状态0未支付1成功2失败3异常6退款
 */
@AllArgsConstructor
public enum PayStatusEnum {
    /**
     * 初始化
     */
    INIT(0),

    /**
     * 成功
     */
    SUCCESS(1),

    /**
     * 失败
     */
    FAILURE(2),
    /**
     * 异常
     */
    OTHER(3),
    /**
     * 异常
     */
    CLOSED(4),
    /**
     * 异常
     */
    RUNNING(5),
    /**
     * 异常
     */
    REBACK(6),//退还
    /**
     * 异常
     */
    NOTHING(7);//无需支付

    @Getter
    @EnumValue
    private final Integer code;
}
