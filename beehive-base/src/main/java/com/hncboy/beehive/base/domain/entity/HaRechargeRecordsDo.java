package com.hncboy.beehive.base.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.hncboy.beehive.base.enums.PayStatusEnum;
import com.hncboy.beehive.base.enums.PayTypeEnum;
import lombok.Data;

import java.util.Date;

/**
 * @author ll
 * @date 2023-7-24
 */
@TableName("ha_recharge_records")
@Data
public class HaRechargeRecordsDo {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 用户id
     */
    @TableField("user_id")
    private Integer userId;

    /**
     * 充值金额
     */
    private Integer amount;

    /**
     * 订单创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;/**
     * 支付时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;/**
     * 订单创建时间
     */
    private PayStatusEnum status;
    private String mark;
    private Integer points;
    private PayTypeEnum type;//支付类型，1pc支付宝，2手机网站支付宝，3pc扫码微信，4移动微信
    private String orderId;//自定义订单id
    private String payMark;//支付接口返回状态，异步通知
    private String tradeNo;//支付交易号
    private String buyerLogonId; //对方支付宝账号
    private Integer isAddPoints;//可能是：1添加积分了，0未添加
    private String payResult;//超时支付结果备注，自动
}
