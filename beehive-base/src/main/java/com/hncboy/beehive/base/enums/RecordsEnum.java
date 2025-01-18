package com.hncboy.beehive.base.enums;

import com.hncboy.beehive.base.domain.entity.HaProductsDo;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * OpenAi 消费信息状态枚举
 */
@AllArgsConstructor
public enum RecordsEnum {

    /**
     * 针对问题
     * 初始化，未发送
     * 因为发送和接收的速度很快，所以这种初始状态基本上不会有，除非异常情况
     */
    GPT_3_5_TURBO(1,"聊天","CHAT3.5_TURBO","gpt-3.5-turbo",1,0),
    GPT_3_5_TURBO_1106(1,"聊天","CHAT3.5_TURBO_1106","gpt-3.5-turbo-1106",1,0),
    GPT_3_5_TURBO_16K(1,"聊天","CHAT3.5_TURBO_16K","gpt-3.5-turbo-16k",1,0),
    GPT_4(2,"聊天","CHAT4.0","gpt-4",15,0),
    GPT_4_32K(2,"聊天","CHAT4.0_32K","gpt-4-32k",15,0),
    GPT_4_128K(2,"聊天","CHAT4.0_128K","gpt-4-1106-preview",15,0),
    GPT_4_VISION(2,"聊天","CHAT_4.0_多模态","gpt-4-vision-preview",15,0),
    GPT_4_ALL(2,"聊天","CHAT_4.0_ALL","gpt-4-all",15,0),
    GPT_4_TURBO_PREVIEW(2,"聊天","CHAT4.0-TURBO-PREVIEW","gpt-4-turbo-preview",15,0),


    IMG_MJ(3,"绘画","Midjourney","mj",20,11),
    IMG_MJ_TOWEN(3,"绘画","Midjourney-图生文","mj-t",5,11),
    IMG_MJ_V(3,"绘画","Midjourney-仿制","mj-f",20,11),
    IMG_MJ_U(3,"绘画","Midjourney-放大","mj-d",5,11),
    IMG_MJ_BLEND(3,"绘画","Midjourney-混图","mj-b",5,11),
    IMG_MJ_FACE(3,"绘画","Midjourney-换脸","mj-f",5,11),

    IMG_MJ_RELAX(3,"绘画","Midjourney-relax","mj",20,11),
    IMG_MJ_V_RELAX(3,"绘画","Midjourney-仿制-relax","mj-f",20,11),
    IMG_MJ_U_RELAX(3,"绘画","Midjourney-放大-relax","mj-d",5,11),
    IMG_MJ_BLEND_RELAX(3,"绘画","Midjourney-混图-relax","mj-b",5,11),
    IMG_MJ_FACE_RELAX(3,"绘画","Midjourney-换脸-relax","mj-f",5,11),

    IMG_DALLE(4,"绘画","DALLE","dall-e-2",5,1),
    IMG_DALLE_3(4,"绘画","DALLE-3","dall-e-3",5,1),
    IMG_SD(5,"绘画","StableDiffusion","sd",0,0),
    IMG_QR(6,"绘画","AI二维码","qr",20,29),
    ERWEIMA(7,"绘画","AI二维码","er",60,29),
    SIWEIDAOTU(8,"绘画","思维导图","swd",5,0),


    GEMINI_PRO_VISION(2,"聊天","GEMINI_PRO_VISION","gemini-pro-vision",15,0),
    GEMINI_PRO(2,"聊天","GEMINI_PRO","gemini-pro",15,0),


    CLAUDE_3_SONNET(2,"聊天","CLAUDE_3_SONNET","claude-3-sonnet-20240229",15,0),
    CLAUDE_3_OPUS(2,"聊天","CLAUDE_3_OPUS","claude-3-opus-20240229",15,0),

    STABLE_DIFFUSION(2,"绘画","STABLE_DIFFUSION","stable-diffusion",15,0);


    @Getter
    private final Integer id;
    @Getter
    private final String model;
    @Getter
    private final String name;
    @Getter
    private final String apiModel;

    /**
     * 最大 token 上限
     */
    @Getter
    private final Integer records;
    @Getter
    private final Integer vipRrecords;

    public static RecordsEnum getById(Integer id) {
        for (RecordsEnum recordsEnum : RecordsEnum.values()) {
            if (recordsEnum.getId().equals(id)) {
                return recordsEnum;
            }
        }
        return null;
    }

    public static HaProductsDo getByName(RecordsEnum re) {

        for (HaProductsDo phd : lhd) {
            if (re.getName().equals(phd.getName())) {
                return phd;
            }
        }
        return null;
    }
    public static RecordsEnum fromApiModel(String apiModel) {
        for (RecordsEnum recordsEnum : RecordsEnum.values()) {
            if (recordsEnum.getApiModel().equals(apiModel)) {
                return recordsEnum;
            }
        }
        throw new IllegalArgumentException("Invalid apiModel: " + apiModel);
    }

    public static List<HaProductsDo> lhd = new ArrayList<>();
    public static List<HaProductsDo> showLhd = new ArrayList<>();
    public static int getPointsByName(RecordsEnum re) {
        for (HaProductsDo phd : lhd) {
            if (re.getName().equals(phd.getName())) {
                return phd.getRecords();
            }
        }
        return -1;
    }
    //只适用于product中is_chat==1的
    public static int getPointsByApiModel(String re) {
        for (HaProductsDo phd : lhd) {
            if (re.equals(phd.getApiModel())) {
                return phd.getRecords();
            }
        }
        return -1;
    }
    public static HaProductsDo getProductByName(String re) {
        for (HaProductsDo phd : lhd) {
            if (re.equals(phd.getName())) {
                return phd;
            }
        }
        return null;
    }


}
