package com.hncboy.beehive.web.domain.vo;

import com.hncboy.beehive.base.enums.RecordsEnum;
import lombok.Data;

import java.util.Date;

/**
 * @author ll
 * @date 2023-7-24
 */
@Data
public class HaExpenseRecordsVo {

private Integer id;
    /**
     * 消费金额
     */
    private RecordsEnum amount;

    /**
     * 消费时间
     */
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
    private Integer holdBi;
    private String mark;

}
