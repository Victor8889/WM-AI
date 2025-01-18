package com.hncboy.beehive.cell.core.domain.request;

import com.hncboy.beehive.base.enums.CellCodeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * @author ll
 * @date 2023/5/29
 * 房间创建请求参数
 */
@Data
@Schema(title = "房间创建请求参数")
public class RoomAddRequest {



    @NotNull(message = "房间信息不能为空")
    @Schema(title = "房间名称")
    private String name;

    @NotNull(message = "房间信息不能为空")
    @Schema(title = "房间名称")
    private String color;

    @NotNull(message = "配置项 code 不能为空")
    @Schema(title = "配置项 code")
    private String cellConfigCode;


}
