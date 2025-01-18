package com.hncboy.beehive.base.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * @author ll
 * @date 2023-7-24
 */
@TableName("ha_user_permissions")
@Data
public class HaUserPermissionsDo {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 权限级别
     */
    private Integer permissionLevel;

    /**
     * 是否可用，1可，2不可
     */
    @TableField(value="is_enable")
    private Integer isEnable;

    /**
     * 剩余hold币
     */
    @TableField("remain_hold_bi")
    private Integer remainHoldBi;

    /**
     * 免费赠送的基础对话积分
     */
    @TableField("remain_hold_count")
    private Integer remainHoldCount;

    /**
     * 充值次数
     */
    @TableField("recharge_count")
    private Integer rechargeCount;

    /**
     * 有效期
     */
    @TableField("validy_date")
    private Date validyDate;

    /**
     * 次卡，1是，2不是
     */
    @TableField("is_count")
    private Integer isCount;

    /**
     * 会员卡，1是，2不是
     */
    @TableField("is_vip")
    private Integer isVip;
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    private Date vipTime;//第一次充值vip的时间，如果续费当月已赠送则不在赠送

    private Integer inviteEncode;//自己的邀请码
    private Integer otherUserId;//邀请自己的人

    private Integer isGiving;//1赠送，2没赠送
}
