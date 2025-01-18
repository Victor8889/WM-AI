package com.hncboy.beehive.base.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author ll
 * @date 2023/5/19
 * Midjourney 动作枚举
 */
@AllArgsConstructor
public enum MjMsgActionEnum {

    /**
     * 生成图片
     */
    IMAGINE("imagine"),

    /**
     * 选中放大
     */
    UPSCALE("upscale"),

    /**
     * 选中其中的一张图，生成四张相似的
     */
    VARIATION("variation"),

    /**
     * 融图
     */
    BLEND("blend"),
    /**
     * 换脸
     */
    FACE("face"),
    /**
     * 重新生成
     */
    REROLL("reroll"),

    //变焦
    ZOOM("CustomZoom"),
    //扩展
    PAN("pan"),
    //局部重绘
    MODAL("modal"),
    //局部重绘
    INPAINT("Inpaint"),
    /**
     * 图转 prompt
     */
    DESCRIBE("describe");

    @Getter
    @EnumValue
    private final String action;
}
