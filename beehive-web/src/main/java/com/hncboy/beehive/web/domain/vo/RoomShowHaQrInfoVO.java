package com.hncboy.beehive.web.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * @author ll
 * @date 2023/7/31
 * OpenAi 展示参数
 */
@Data
@Schema(title = "Qr 生成二维码参数")
public class RoomShowHaQrInfoVO {


    @Schema(title = "消息内容")
    private String id; // 描述

    @Schema(title = "创建时间")
    private Date createTime;

    @Schema(title = "消息内容")
    private String description; // 描述

    @Schema(title = "消息内容")
    private String qrUrl; // 二维码URL
    @Schema(title = "消息内容")
    private String isCompleted; // 二维码URL

}
