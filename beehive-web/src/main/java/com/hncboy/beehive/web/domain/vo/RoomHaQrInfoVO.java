package com.hncboy.beehive.web.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author ll
 * @date 2023/7/31
 * OpenAi 展示参数
 */
@Data
@Schema(title = "Qr 生成二维码参数")
public class RoomHaQrInfoVO {


    @Schema(title = "消息内容")
    private String model; // 风格
    @Schema(title = "消息内容")
    private String userUrl; // 用户URL
    @Schema(title = "消息内容")
    private String description; // 描述
    @Schema(title = "消息内容")
    private String artNumber; // 艺术编号
    @Schema(title = "消息内容")
    private String style; // 风格
    @Schema(title = "消息内容")
    private String version; // 版本

    @Schema(title = "消息内容")
    private String prompt; // 提示
    @Schema(title = "消息内容")
    private String barcode; // 码眼

    @Schema(title = "消息内容")
    private String proportion; // 比例

    @Schema(title = "消息内容")
    private String qrFormat; // 二维码样式
}
