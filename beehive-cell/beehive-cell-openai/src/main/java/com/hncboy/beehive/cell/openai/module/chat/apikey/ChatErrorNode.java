package com.hncboy.beehive.cell.openai.module.chat.apikey;

import cn.hutool.core.lang.Pair;
import com.hncboy.beehive.base.domain.entity.RoomOpenAiChatMsgDO;

/**
 * @author ll
 * @date 2023/6/10
 * Openai 对话错误链路
 */
public interface ChatErrorNode {

    /**
     * 处理问题消息
     *
     * @param questionMessage      问题消息
     * @param enabled 房间配置参数
     * @return 结果 key：是否通过 value：错误内容
     */
    Pair<Boolean, String> doHandle(RoomOpenAiChatMsgDO questionMessage, Boolean enabled);//Map<OpenAiChatCellConfigCodeEnum, DataWrapper> roomConfigParamAsMap

    String getWords(String str);
    String getFirstLetter(String str);
}
