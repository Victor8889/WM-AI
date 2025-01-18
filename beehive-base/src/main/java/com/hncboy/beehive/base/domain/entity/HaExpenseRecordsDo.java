package com.hncboy.beehive.base.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * @author ll
 * @date 2023-7-24
 */
@TableName("ha_expense_records")
@Data
public class HaExpenseRecordsDo {
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
     * 用户id
     */
    private Long modelId;

    /**
     * 消费金额
     */
    private Integer amount;

    /**
     * 消费时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 消费类型
     * 1聊天
     * 2画画
     * 3二维码
     * 4思维导图
     */
    private String model;

    /**
     * hold币
     */
    @TableField("hold_bi")
    private Integer holdBi;

    /**
     *
     */
    private String mark;
    /**
     * 消费次数token
     */
    private Integer token;
}
