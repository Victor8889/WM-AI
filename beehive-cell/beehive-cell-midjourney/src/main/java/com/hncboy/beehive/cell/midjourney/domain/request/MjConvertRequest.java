package com.hncboy.beehive.cell.midjourney.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author ll
 * @date 2023/5/18
 * Midjourney UV 转换请求参数
 */
@Data
@Schema(title = "Midjourney UV 转换请求参数")
public class MjConvertRequest {

    @NotNull(message = "房间 id 不能为空")
    @Schema(title = "房间 id")
    private Long roomId;

    @NotNull(message = "消息 id 不能为空")
    @Schema(title = "消息 id")
    private Long msgId;

    @Min(value = 1, message = "下标最小为 1")
    @Max(value = 4, message = "下标最大为 4")
    @Schema(title = "下标")
    private Integer index;

    @Schema(title = "类型")
    private Integer mjType;

    @Schema(title = "类型")
    private String action;

    @Schema(title = "参数")
    private String maskBase64;

    @Schema(title = "参数")
    private String actionName;

    @Schema(title = "参数")
    private String prompt;
}
