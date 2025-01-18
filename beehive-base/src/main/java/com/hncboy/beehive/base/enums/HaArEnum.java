package com.hncboy.beehive.base.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author ll
 * @date 2023-8-10
 */
public enum HaArEnum {
    RATIO_1_1("1:1"),
    RATIO_9_16("9:16"),
    RATIO_16_9("16:9"),
    RATIO_3_4("3:4"),
    RATIO_4_3("4:3"),
    ISNULL("");

    @EnumValue
    @Getter
    private final String code;

    HaArEnum(String code) {
        this.code = code;
    }

    HaArEnum() {
        this.code = "";
    }
}
