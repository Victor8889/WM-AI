package com.hncboy.beehive.base.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author ll
 * @date 2023-9-3
 * 1pc支付宝，2手机网站支付宝，3pc扫码微信，4移动微信
 */
@AllArgsConstructor
public enum PayTypeEnum {
    /**
     * PCZFB
     */
    PCALIPAY(1),

    /**
     * WANGZ
     */
    MOBILEALIPAY(2),

    /**
     * WX
     */
    PCWXPAY(3),
    MONILEWXPAY(4);

    @Getter
    @EnumValue
    private final Integer code;
}
