package com.hncboy.beehive.base.domain.bo;

import com.hncboy.beehive.base.enums.FrontUserRegisterTypeEnum;
import lombok.Data;

/**
 * @author ll
 * @date 2023-4-16
 * JWT 用户信息业务参数
 */
@Data
public class JwtUserInfoBO {

    /**
     * 注册类型
     */
    private FrontUserRegisterTypeEnum registerType;

    /**
     * 登录账号
     */
    private String username;

    /**
     * 基础用户 id
     */
    private Integer userId;

    /**
     * 扩展用户 id
     */
    private Integer extraUserId;
}
