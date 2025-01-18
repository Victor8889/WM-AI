package com.hncboy.beehive.base.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author ll
 * @date 2023/6/30
 * OpenAi ApiKey 使用场景枚举
 * 括号里面对应数据库keyList里面的UseScene
 */
@AllArgsConstructor
public enum OpenAiApiKeyUseSceneEnum {

    /**
     * GPT 3.5
     */
    GPT_3_5("GPT3.5"),

    /**
     * GPT 4
     */
    GPT_4("GPT4"),

    /**
     * GPT 4
     */
    GPT_4_0("GPT_4.0"),

    /**
     * GPT 4
     */
    STABLE_DIFF("STABLE_DIFFUSION"),

    /**
     * google gemini
     */
    GEMINI("GEMINI"),
    /**
     * claude
     */
    CLAUDE("claude"),

    /**
     * 绘图
     */
    IMAGE("IMAGE"),

    /**
     * 绘图
     */
    DALLE3("DALLE3");

    @EnumValue
    @Getter
    private final String code;
}
