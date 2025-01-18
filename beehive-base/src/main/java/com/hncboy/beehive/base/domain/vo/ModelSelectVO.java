package com.hncboy.beehive.base.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author ll
 * @date 2024/3/29
 * selectModel 展示对象
 */
@Data
@Schema(title = "selectModel 展示对象")
public class ModelSelectVO {

    @Schema(title = "label")
    private String label;

    @Schema(title = "value")
    private String value;

}
