package com.hncboy.beehive.base.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author ll
 * @date 2023-8-10
 */
public enum HaShapeEnum {
    SQUARE("square"),
    CIRCLE("circle"),
    PLUS("plus"),
    BOX("box"),
    OCTAGON("octagon"),
    RANDOM("random"),
    TINY_PLUS("tiny-plus"),
    ISNULL("");
    //SQUARE("正方形"),1
    //CIRCLE("圆形"),1
    //PLUS("加号"),1
    //BOX("方框"),1
    //OCTAGON("八边形"),1
    //RANDOM("随机"),
    //TINY_PLUS("小加号");

    @EnumValue
    @Getter
    private final String code;
    HaShapeEnum(String code) {
        this.code = code;
    }

    HaShapeEnum() {
        this.code = "";
    }
}
