package com.hncboy.beehive.base.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author ll
 * @date 2023-8-10
 */
public enum HaIllustrationEnum {
    CARTOON("67"),//卡通插画,
    CANDY_AVATAR( "51"),//糖果头像,
    INK_PAINTING( "69"),//水墨画,
    AMERICAN_COMIC("70"),//美漫,
    ISNULL("");//WU ,

    @EnumValue
    @Getter
    private final String code;

    HaIllustrationEnum(String code) {
        this.code = code;
    }

    HaIllustrationEnum() {
        this.code = "";
    }
}
