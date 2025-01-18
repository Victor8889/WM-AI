package com.hncboy.beehive.base.constant;

import com.hncboy.beehive.base.domain.entity.HaUserParamDo;
import com.hncboy.beehive.base.domain.entity.HaUserPermissionsDo;

import java.util.Date;

/**
 * @author ll
 * @date 2023-7-25
 */
public class HaUserConstant {
    // chat参数默认值常量
    public static final String DEFAULT_APIKEY = null;  // 用户自己的key默认值
    public static final Integer DEFAULT_MAX_TOKENS = 16384;  // 最大tokens默认值
    public static final Float DEFAULT_TEMPERATURE = 0.2f;  // 随机性默认值
    public static final String DEFAULT_KEY_STRATEGY = "balance";  // 策略默认值
    public static final Integer DEFAULT_CONTEXT_COUNT = 5;  // 上下文数量默认值
    public static final String DEFAULT_SYSTEM_MESSAGE = "你好,请用中文交流。";  // 默认消息默认值
    public static final Float DEFAULT_PRESENCE_PENALTY = 0.00f;  // 话题新鲜度默认值
    public static final Integer DEFAULT_CONTEXT_RELATED_TIME_HOUR = 24;  // 上下文相关时间（小时）默认值
    public static final Integer DEFAULT_ENABLE_LOCAL_SENSITIVE_WORD = 1;  // 是否启用本地敏感词默认值，1启用，2不启用
    public static final String DEFAULT_CHAT_MODEL = "gpt-3.5-turbo";  // 模型默认值
    public static final Integer DEFAULT_IS_CONTEXT = 0;  // 模型默认值

    //权限默认参数
    // 默认值常量及注释
    public static final Integer DEFAULT_PERMISSION_LEVEL = 0;  // 权限级别默认值
    public static final Integer DEFAULT_IS_ENABLE = 1;  // 是否可用默认值 (1表示可用)
    public static final Integer DEFAULT_REMAIN_HOLD_BI = 0;  // 剩余hold币默认值
    public static final Integer DEFAULT_REMAIN_HOLD_COUNT = 0;  // 剩余次数默认值
    public static final Integer DEFAULT_RECHARGE_COUNT = 0;  // 充值次数默认值
    public static final Date DEFAULT_VALIDY_DATE = new Date(new Date().getTime() - 1000*60*60);  // 有效期默认值
    public static final Integer DEFAULT_IS_COUNT = 2;  // 次卡，默认不是 (2表示不是次卡)
    public static final Integer DEFAULT_IS_VIP = 2;  // 会员卡，默认不是 (2表示不是会员卡)
    public static final Integer DEFAULT_HOLD_BI = 30;  // 积分
    public static final Integer NO_GIVING = 2;  // 未赠送积分

    // 方法：将常量的默认值赋值给HaUserPermissions实体类
    public static HaUserPermissionsDo setPermissionDefaultValues(int userId) {
        HaUserPermissionsDo haUserPermissions = new HaUserPermissionsDo();
        haUserPermissions.setUserId(userId);
        haUserPermissions.setPermissionLevel(DEFAULT_PERMISSION_LEVEL);
        haUserPermissions.setIsEnable(DEFAULT_IS_ENABLE);
        haUserPermissions.setRemainHoldBi(DEFAULT_REMAIN_HOLD_BI);
        haUserPermissions.setRemainHoldCount(DEFAULT_REMAIN_HOLD_COUNT);
        haUserPermissions.setRechargeCount(DEFAULT_RECHARGE_COUNT);
        haUserPermissions.setValidyDate(DEFAULT_VALIDY_DATE);
        haUserPermissions.setIsCount(DEFAULT_IS_COUNT);
        haUserPermissions.setIsVip(DEFAULT_IS_VIP);
        haUserPermissions.setInviteEncode((int)(userId+userId/10+1));
        haUserPermissions.setRemainHoldBi(DEFAULT_HOLD_BI);
        haUserPermissions.setIsGiving(NO_GIVING);

        return haUserPermissions;
    }


    // 方法：将常量的默认值赋值给HaUserParam实体类
    public static HaUserParamDo setParamDefaultValues(int userId) {
        HaUserParamDo haUserParam = new HaUserParamDo();
        haUserParam.setUserid(userId);
        haUserParam.setApikey(DEFAULT_APIKEY);
        haUserParam.setMaxTokens(DEFAULT_MAX_TOKENS);
        haUserParam.setTemperature(DEFAULT_TEMPERATURE);
        haUserParam.setKeyStrategy(DEFAULT_KEY_STRATEGY);
        haUserParam.setContextCount(DEFAULT_CONTEXT_COUNT);
        haUserParam.setSystemMessage(DEFAULT_SYSTEM_MESSAGE);
        haUserParam.setPresencePenalty(DEFAULT_PRESENCE_PENALTY);
        haUserParam.setContextRelatedTimeHour(DEFAULT_CONTEXT_RELATED_TIME_HOUR);
        haUserParam.setEnableLocalSensitiveWord(DEFAULT_ENABLE_LOCAL_SENSITIVE_WORD);
        haUserParam.setChatModel(DEFAULT_CHAT_MODEL);
                haUserParam.setIsContext(DEFAULT_IS_CONTEXT);

        return haUserParam;
    }
}
