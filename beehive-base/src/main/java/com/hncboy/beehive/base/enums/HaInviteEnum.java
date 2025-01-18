package com.hncboy.beehive.base.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


public enum HaInviteEnum {

    INVITE_2(2, 100),
    INVITE_10(10, 520),
    INVITE_20(20, 1050),
    INVITE_35(35, 1855),
    INVITE_50(50, 2675),
    INVITE_100(100, 5400);

    @Getter
    private final int code;
    @Getter
    private final int points;

    HaInviteEnum(int code, int points) {
        this.code = code;
        this.points = points;
    }
    public static int getPointsByCode(int code) {
        for (HaInviteEnum inviteEnum : HaInviteEnum.values()) {
            if (inviteEnum.getCode() == code) {
                return inviteEnum.getPoints();
            }
        }
        return 0; // 如果没有匹配的code，则返回默认值，或者抛出异常
    }
}
