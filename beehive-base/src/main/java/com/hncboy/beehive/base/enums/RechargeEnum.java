package com.hncboy.beehive.base.enums;

import com.hncboy.beehive.base.domain.entity.HaShopsDo;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * OpenAi 消费信息状态枚举
 */
@AllArgsConstructor
public enum RechargeEnum {

    /**
     * 针对问题
     * 初始化，未发送
     * 因为发送和接收的速度很快，所以这种初始状态基本上不会有，除非异常情况
     */
    RECHARGE_2(1,5,50,2),
    RECHARGE_10(2,10,260,15),
    RECHARGE_20(3,20,525,22),
    RECHARGE_35(4,35,920,2),
    RECHARGE_50(5,50,1320,0),
    RECHARGE_100(6,100,2670,60),
    RECHARGE_1000(7,1000,27000,60),
    RECHARGE_20000(8,20000,54500,60);


    @Getter
    private final Integer id;
    @Getter
    private final Integer amount;
    @Getter
    private final Integer points;

    /**
     * 最大 token 上限
     */
    @Getter
    private final Integer tokens;

    public static HaShopsDo getByamount(Integer amount) {
        for (HaShopsDo rechargeEnum : lhsd) {
            if (rechargeEnum.getAmount().equals(amount)) {
                return rechargeEnum;
            }
        }
        return null;
    }

    public static List<HaShopsDo> lhsd = new ArrayList<>();


    public static boolean isValid(Integer amount,int points) {
        for (HaShopsDo rechargeEnum : lhsd) {
            if (rechargeEnum.getAmount().equals(amount)) {
                if(rechargeEnum.getPoints().intValue() == points)
                return true;
            }
        }
        return false;
    }

}
