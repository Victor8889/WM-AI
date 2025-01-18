package com.hncboy.beehive.web.domain.vo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * @author ll
 * @date 2023-7-24
 */
@Data
public class HaRechargeRecordsVo {
    /**
     * 主键
     */
    private Integer id;


    /**
     * 充值金额
     */
    private Integer amount;

    private Date createTime;
    /**
     * 订单创建时间
     */
    private String status;
    private String mark;
    private int points;
    private String payResult;//超时支付结果备注，自动
}
