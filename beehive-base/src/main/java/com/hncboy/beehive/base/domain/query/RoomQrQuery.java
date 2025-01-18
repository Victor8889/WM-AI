package com.hncboy.beehive.base.domain.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author ll
 * @date 2023/7/26
 * 房间消息通用查询参数
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(title = "QR查询参数")
public class RoomQrQuery extends CursorQuery {

    @Schema(title = "QR id")
    private String id;
}

