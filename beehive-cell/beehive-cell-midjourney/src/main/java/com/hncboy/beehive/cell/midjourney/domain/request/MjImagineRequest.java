package com.hncboy.beehive.cell.midjourney.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author ll
 * @date 2023/5/18
 * Midjourney 创建图像参数
 */
@Data
@Schema(title = "Midjourney 创建图像参数")
public class MjImagineRequest {

    @NotNull(message = "房间 id 不能为空")
    @Schema(title = "房间 id")
    private Long roomId;

    @Size(max = 2000, message = "提示语不能超过 2000 个字")
    @NotEmpty(message = "提示语不能为空")
    @Schema(title = "提示语")
    private String prompt;

    @Size(max = 2000, message = "参考图地址")
    @Schema(title = "url")
    private String baseImg;

    @Size(max = 1000, message = "参数信息")
    @Schema(title = "参数")
    private String params;

    @Schema(title = "类型")
    private Integer mjType;


}
