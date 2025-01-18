package com.hncboy.beehive.cell.openai.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author ll
 * @date 2023/6/3
 * OpenAi 图像房间消息发送参数
 */
@Data
@Schema(title = "OpenAi 图像房间消息发送参数")
public class RoomOpenAiImageSendRequest {

    @NotNull(message = "房间 id 不能为空")
    @Schema(title = "房间 id")
    private Long roomId;

    @Size(min = 1, max = 4000, message = "提示词长度必须在 1-4000 之间")
    @NotNull(message = "提示词不能为空")
    @Schema(title = "提示词")
    private String prompt;
    private String runParams;
}
