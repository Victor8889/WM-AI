package com.hncboy.beehive.cell.openai.enums;

import com.hncboy.beehive.base.domain.entity.HaProductsDo;
import com.hncboy.beehive.base.enums.CommonEnum;
import com.hncboy.beehive.base.enums.OpenAiApiKeyUseSceneEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author ll
 * @date 2023/6/7
 * OpenAI Chat API 模型
 * @see <a href="https://platform.openai.com/docs/models/gpt-3-5"/>
 */
@AllArgsConstructor
public enum OpenAiChatApiModelEnum {

    /**
     * GPT-3.5
     */
    GPT_3_5_TURBO("gpt-3.5-turbo", 4096, OpenAiChatApiModelEnum.GPT_3_5, OpenAiApiKeyUseSceneEnum.GPT_3_5),
    GPT_3_5_TURBO_1106("gpt-3.5-turbo-1106", 4096, OpenAiChatApiModelEnum.GPT_3_5, OpenAiApiKeyUseSceneEnum.GPT_3_5),
    GPT_3_5_TURBO_16K("gpt-3.5-turbo-16k", 16384, OpenAiChatApiModelEnum.GPT_3_5, OpenAiApiKeyUseSceneEnum.GPT_3_5),

    //已过期
    GPT_3_5_TURBO_0613("gpt-3.5-turbo-0613", 4096, OpenAiChatApiModelEnum.GPT_3_5, OpenAiApiKeyUseSceneEnum.GPT_3_5),
    GPT_3_5_TURBO_16k_0613("gpt-3.5-turbo-16K-0613", 16384, OpenAiChatApiModelEnum.GPT_3_5, OpenAiApiKeyUseSceneEnum.GPT_3_5),

    /**
     * GPT-4
     */
    GPT_4("gpt-4", 8192, OpenAiChatApiModelEnum.GPT4, OpenAiApiKeyUseSceneEnum.GPT_4),
    GPT_4_32K("gpt-4-32k", 32768, OpenAiChatApiModelEnum.GPT4, OpenAiApiKeyUseSceneEnum.GPT_4),
    GPT_4_128K("gpt-4-1106-preview", 12800 , OpenAiChatApiModelEnum.GPT4, OpenAiApiKeyUseSceneEnum.GPT_4),
    GPT_4_VISION("gpt-4-vision-preview", 32768 , OpenAiChatApiModelEnum.GPT4, OpenAiApiKeyUseSceneEnum.GPT_4),
    GPT_4_ALL("gpt-4-all", 128000 , OpenAiChatApiModelEnum.GPT4, OpenAiApiKeyUseSceneEnum.GPT_4),

    GPT_4_TURBO_PREVIEW("gpt-4-turbo-preview", 4096 , OpenAiChatApiModelEnum.GPT4, OpenAiApiKeyUseSceneEnum.GPT_4),


    GEMINI_PRO_VISION("gemini-pro-vision", 8000 , OpenAiChatApiModelEnum.GEMINI, OpenAiApiKeyUseSceneEnum.GEMINI),
    GEMINI_PRO("gemini-pro", 8000 , OpenAiChatApiModelEnum.GEMINI, OpenAiApiKeyUseSceneEnum.GEMINI),

    STABLE_DIFFUSION("stable-diffusion", 8000 , OpenAiChatApiModelEnum.STABLE_DIFF, OpenAiApiKeyUseSceneEnum.STABLE_DIFF),


    CLAUDE_3_SONNET("claude-3-sonnet-20240229", 4096 , OpenAiChatApiModelEnum.CLAUDE, OpenAiApiKeyUseSceneEnum.CLAUDE),
    CLAUDE_3_OPUS("claude-3-opus-20240229", 4096 , OpenAiChatApiModelEnum.CLAUDE, OpenAiApiKeyUseSceneEnum.CLAUDE),

    GPT_4_0("gpt-4.0", 8192, OpenAiChatApiModelEnum.GPT4, OpenAiApiKeyUseSceneEnum.GPT_4_0),//只是用来获取key和url无实际用处

    //已过期
    GPT_4_0613("gpt-4-0613", 8192, OpenAiChatApiModelEnum.GPT4, OpenAiApiKeyUseSceneEnum.GPT_4),
    GPT_4_32K_0613("gpt-4-32k-0613", 32768, OpenAiChatApiModelEnum.GPT4, OpenAiApiKeyUseSceneEnum.GPT_4);

    /**
     * 模型名称
     */
    @Getter
    private final String name;

    /**
     * 最大 token 上限
     */
    @Getter
    private final Integer maxTokens;

    /**
     * 用于 token 计算的模型名称
     */
    @Getter
    private final String calcTokenModelName;

    /**
     * 使用场景
     */
    @Getter
    private final OpenAiApiKeyUseSceneEnum useSceneEnum;

    private static final String GPT_3_5 = "gpt-3.5-turbo";
    private static final String GPT4 = "gpt-4";
    private static final String GPT4_0 = "gpt-4.0";
    private static final String GEMINI = "gemini";
    private static final String STABLE_DIFF = "stable-diffusion";
    private static final String CLAUDE = "claude";

    /**
     * name 作为 key，封装为 Map
     */
    public static final Map<String, OpenAiChatApiModelEnum> NAME_MAP = Stream
            .of(OpenAiChatApiModelEnum.values())
            .collect(Collectors.toMap(OpenAiChatApiModelEnum::getName, Function.identity()));


    /**
     * 根据模型名称获取最大 token 上限
     *
     * @param modelName 模型名称
     * @return 最大 token 上限
     */
    public static Integer maxTokens(String modelName) {
        //if(NAME_MAP.get(modelName) != null)
        //    return NAME_MAP.get(modelName).getMaxTokens();
        if(CommonEnum.NAME_MAP_PRODUCT.get(modelName) != null)
            return CommonEnum.NAME_MAP_PRODUCT.get(modelName).getMaxTokens();
        else{
            HaProductsDo hpdo = CommonEnum.getProductByApiModel(modelName);
            return hpdo.getMaxTokens();
        }
    }
}
