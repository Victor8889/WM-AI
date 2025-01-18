package com.hncboy.beehive.base.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author ll
 * @date 2023-8-10
 */
@AllArgsConstructor
public enum ChatGptRoomIdEnum {
    /**
     * 初始化
     */
    SWD_ROOM_ID(999),

    /**
     * 成功
     */
    MJ_ROOM_ID(888),

    /**
     * 失败
     */
    DALLE_ROOM_ID(777),
    /**
     * 异常
     */
    SD_ROOM_ID(666);

    @Getter
    @EnumValue
    private final int code;

}
