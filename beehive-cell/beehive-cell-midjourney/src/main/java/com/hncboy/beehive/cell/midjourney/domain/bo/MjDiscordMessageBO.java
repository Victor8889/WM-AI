package com.hncboy.beehive.cell.midjourney.domain.bo;

import com.hncboy.beehive.base.enums.MjMsgActionEnum;
import lombok.Data;

/**
 * @author ll
 * @date 2023/5/20
 * Midjourney Discord 消息业务对象
 */
@Data
public class MjDiscordMessageBO {

    /**
     * 消息指令动作
     */
    private MjMsgActionEnum action;

    /**
     * 消息内容
     */
    private String prompt;

    /**
     * 位置
     */
    private Integer index;

    /**
     * 消息状态
     */
    private String status;
}
