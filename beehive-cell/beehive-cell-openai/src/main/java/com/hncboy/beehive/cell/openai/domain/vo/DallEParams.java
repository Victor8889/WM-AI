package com.hncboy.beehive.cell.openai.domain.vo;

import lombok.Data;

/**
 * @author ll
 * @date 2024-1-11
 */
@Data
public class DallEParams {
    private String model;
    private String size;
    private String style;
    private String quality;

    public DallEParams(String model, String size, String style, String quality) {
        this.model = model;
        this.size = size;
        this.style = style;
        this.quality = quality;
    }

}
