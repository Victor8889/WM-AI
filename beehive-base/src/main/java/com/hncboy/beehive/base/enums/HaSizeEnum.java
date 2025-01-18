package com.hncboy.beehive.base.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author ll
 * @date 2023-8-10
 */
public enum HaSizeEnum {
    V2("2"),
    V1_1("1.1"),
    V1("1"),
    V0("");

    @EnumValue
    @Getter
    private final String code;

    HaSizeEnum(String code) {
        this.code = code;
    }

    HaSizeEnum() {
        this.code = "";
    }
}
