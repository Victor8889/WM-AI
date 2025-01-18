package com.hncboy.beehive.cell.openai.module.chat.apikey;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hncboy.beehive.base.domain.entity.RoomOpenAiChatMsgDO;
import com.hncboy.beehive.base.enums.CommonEnum;
import com.hncboy.beehive.base.enums.MessageTypeEnum;
import com.hncboy.beehive.base.enums.RoomOpenAiChatMsgStatusEnum;
import com.hncboy.beehive.cell.core.hander.strategy.DataWrapper;
import com.hncboy.beehive.cell.openai.enums.OpenAiChatApiModelEnum;
import com.hncboy.beehive.cell.openai.enums.OpenAiChatCellConfigCodeEnum;
import com.hncboy.beehive.cell.openai.service.RoomOpenAiChatMsgService;
import com.unfbx.chatgpt.entity.chat.*;

import java.util.*;

/**
 * @author ll
 * @date 2023/6/10
 * OpenAi 对话消息构建工具
 */
public class ChatCompletionBuildUtil {

    /**
     * 构建聊天对话请求参数
     *
     * @param questionMessage      问题消息
     * @param roomConfigParamAsMap 房间配置参数
     * @param contentMessages      上下文消息
     * @return 聊天对话请求参数
     */
    public static ChatCompletion buildChatCompletion(RoomOpenAiChatMsgDO questionMessage, Map<OpenAiChatCellConfigCodeEnum, DataWrapper> roomConfigParamAsMap, LinkedList<Message> contentMessages) {
        // 最终的 maxTokens
        int finalMaxTokens;
        // 本次对话剩余的 maxTokens 最大值 = 模型的最大上限 - 本次 prompt 消耗的 tokens - 1
        int currentRemainMaxTokens = OpenAiChatApiModelEnum.maxTokens(questionMessage.getModelName()) - questionMessage.getPromptTokens() - 1;
        // 获取 maxTokens
        DataWrapper maxTokensDataWrapper = roomConfigParamAsMap.get(OpenAiChatCellConfigCodeEnum.MAX_TOKENS);
        // 如果 maxTokens 为空或者大于当前剩余的 maxTokens
        if (maxTokensDataWrapper.isNull() || maxTokensDataWrapper.asInt() > currentRemainMaxTokens) {
            finalMaxTokens = currentRemainMaxTokens;
        } else {
            finalMaxTokens = maxTokensDataWrapper.asInt();
        }

        // 构建聊天参数
        return ChatCompletion.builder()
                // 最大的 tokens = 模型的最大上线 - 本次 prompt 消耗的 tokens
                .maxTokens(finalMaxTokens)
                .model(questionMessage.getModelName())
                // [0, 2] 越低越精准
                .temperature(roomConfigParamAsMap.get(OpenAiChatCellConfigCodeEnum.TEMPERATURE).asBigDecimal().doubleValue())
                .topP(1.0)
                // 每次生成一条
                .n(1)
                .presencePenalty(roomConfigParamAsMap.get(OpenAiChatCellConfigCodeEnum.PRESENCE_PENALTY).asBigDecimal().doubleValue())
                .messages(contentMessages)
                .stream(true)
                .build();
    }

    /**
     * 构建上下文消息
     *
     * @param questionMessage      当前消息
     * @param roomConfigParamAsMap 房间配置参数
     */
    public static LinkedList<Message> buildContextMessage(RoomOpenAiChatMsgDO questionMessage, Map<OpenAiChatCellConfigCodeEnum, DataWrapper> roomConfigParamAsMap) {
        LinkedList<Message> contextMessages = new LinkedList<>();

        // 系统角色消息
        DataWrapper systemMessageDataWrapper = roomConfigParamAsMap.get(OpenAiChatCellConfigCodeEnum.SYSTEM_MESSAGE);
        if (Objects.nonNull(systemMessageDataWrapper) && StrUtil.isNotBlank(systemMessageDataWrapper.asString())) {
            Message systemMessage = Message.builder()
                    .role(Message.Role.SYSTEM)
                    .content("你的身份是一个由万码AI开发地人工智能助手。"+systemMessageDataWrapper.asString())
                    .build();
            contextMessages.add(systemMessage);
        }

        // 上下文条数
        int contextCount = roomConfigParamAsMap.get(OpenAiChatCellConfigCodeEnum.CONTEXT_COUNT).asInt();
        // 不关联上下文，构建当前消息就直接返回
        if (contextCount == 0) {
            contextMessages.add(Message.builder()
                    .role(Message.Role.USER)
                    .content(questionMessage.getContent())
                    .build());
            return contextMessages;
        }

        // 上下文关联时间
        int relatedTimeHour = roomConfigParamAsMap.get(OpenAiChatCellConfigCodeEnum.CONTEXT_RELATED_TIME_HOUR).asInt();

        // 查询上下文消息
        List<RoomOpenAiChatMsgDO> historyMessages = SpringUtil.getBean(RoomOpenAiChatMsgService.class).list(new LambdaQueryWrapper<RoomOpenAiChatMsgDO>()
                // 查询需要的字段
                .select(RoomOpenAiChatMsgDO::getMessageType, RoomOpenAiChatMsgDO::getContent)
                // 当前房间
                .eq(RoomOpenAiChatMsgDO::getRoomId, questionMessage.getRoomId())
                // 查询消息为成功的
                .eq(RoomOpenAiChatMsgDO::getStatus, RoomOpenAiChatMsgStatusEnum.COMPLETE_SUCCESS)
                // 上下文的时间范围
                .gt(relatedTimeHour > 0, RoomOpenAiChatMsgDO::getCreateTime, DateUtil.offsetHour(new Date(), -relatedTimeHour))
                // 限制上下文条数
                .last("limit " + contextCount)
                // 按主键降序
                .orderByDesc(RoomOpenAiChatMsgDO::getId));
        // 这里降序用来取出最新的上下文消息，然后再反转
        Collections.reverse(historyMessages);
        for (RoomOpenAiChatMsgDO historyMessage : historyMessages) {
            Message.Role role = (historyMessage.getMessageType() == MessageTypeEnum.ANSWER) ? Message.Role.ASSISTANT : Message.Role.USER;
            contextMessages.add(Message.builder()
                    .role(role)
                    .content(historyMessage.getContent())
                    .build());
        }

        // 查询当前用户消息
        contextMessages.add(Message.builder()
                .role(Message.Role.USER)
                .content(questionMessage.getContent())
                .build());

        return contextMessages;
    }
    /**
     * 构建上下文消息
     *
     * @param questionMessage      当前消息
     * @param roomConfigParamAsMap 房间配置参数
     */
    public static LinkedList<MessagePicture> build4VPContextMessage(RoomOpenAiChatMsgDO questionMessage, Map<OpenAiChatCellConfigCodeEnum, DataWrapper> roomConfigParamAsMap) {
        LinkedList<MessagePicture> contextMessages = new LinkedList<>();
        //LinkedList<Content> contentList = new LinkedList<>();
        // 系统角色消息
        DataWrapper systemMessageDataWrapper = roomConfigParamAsMap.get(OpenAiChatCellConfigCodeEnum.SYSTEM_MESSAGE);
        if (Objects.nonNull(systemMessageDataWrapper) && StrUtil.isNotBlank(systemMessageDataWrapper.asString())) {

            Content textContent = Content.builder().text("你的身份是一个由万码AI开发地人工智能助手。" + systemMessageDataWrapper.asString()).type(Content.Type.TEXT.getName()).build();
            List<Content> contentList = new ArrayList<>();
            contentList.add(textContent);
            MessagePicture message = MessagePicture.builder().role(Message.Role.SYSTEM).content(contentList).build();

            contextMessages.add(message);
        }

        // 上下文条数
        int contextCount = roomConfigParamAsMap.get(OpenAiChatCellConfigCodeEnum.CONTEXT_COUNT).asInt();
        List<String> urls = CommonEnum.getUrl(questionMessage.getContent());

        // 不关联上下文，构建当前消息就直接返回
        if (contextCount == 0) {
            List<Content> contentList1 = new ArrayList<>();
            if(urls.isEmpty()) {
                Content textContent1 = Content.builder().text(questionMessage.getContent()).type(Content.Type.TEXT.getName()).build();
                contentList1.add(textContent1);
            }else {
                String str = questionMessage.getContent();
                for(String url:urls){
                    str = str.replaceAll(url,"");
                    //
                    ImageUrl imageUrl1 = ImageUrl.builder().url(url).build();//urlToBase64Array(url)
                    Content imageContent1 = Content.builder().imageUrl(imageUrl1).type(Content.Type.IMAGE_URL.getName()).build();
                    contentList1.add(imageContent1);

                }
                Content textContent2 = Content.builder().text(str).type(Content.Type.TEXT.getName()).build();
                contentList1.add(textContent2);
            }

            MessagePicture message1 = MessagePicture.builder().role(MessagePicture.Role.USER).content(contentList1).build();
            contextMessages.add(message1);
            return contextMessages;
        }

        // 上下文关联时间
        int relatedTimeHour = roomConfigParamAsMap.get(OpenAiChatCellConfigCodeEnum.CONTEXT_RELATED_TIME_HOUR).asInt();

        // 查询上下文消息
        List<RoomOpenAiChatMsgDO> historyMessages = SpringUtil.getBean(RoomOpenAiChatMsgService.class).list(new LambdaQueryWrapper<RoomOpenAiChatMsgDO>()
                // 查询需要的字段
                .select(RoomOpenAiChatMsgDO::getMessageType, RoomOpenAiChatMsgDO::getContent)
                // 当前房间
                .eq(RoomOpenAiChatMsgDO::getRoomId, questionMessage.getRoomId())
                // 查询消息为成功的
                .eq(RoomOpenAiChatMsgDO::getStatus, RoomOpenAiChatMsgStatusEnum.COMPLETE_SUCCESS)
                // 上下文的时间范围
                .gt(relatedTimeHour > 0, RoomOpenAiChatMsgDO::getCreateTime, DateUtil.offsetHour(new Date(), -relatedTimeHour))
                // 限制上下文条数
                .last("limit " + contextCount)
                // 按主键降序
                .orderByDesc(RoomOpenAiChatMsgDO::getId));
        // 这里降序用来取出最新的上下文消息，然后再反转
        Collections.reverse(historyMessages);

        ImageUrl imageUrl = new ImageUrl();
        Content imageContent = new Content();
        for (RoomOpenAiChatMsgDO historyMessage : historyMessages) {
            List<Content> contentList = new ArrayList<>();
            MessagePicture.Role role = (historyMessage.getMessageType() == MessageTypeEnum.ANSWER) ? MessagePicture.Role.ASSISTANT : MessagePicture.Role.USER;
            Content textContent = new Content();
            if(historyMessage.getMessageType() == MessageTypeEnum.QUESTION) {
                urls = CommonEnum.getUrl(historyMessage.getContent());
                if( urls.isEmpty()){
                    textContent = Content.builder().text(historyMessage.getContent()).type(Content.Type.TEXT.getName()).build();
                }else{
                    String str = questionMessage.getContent();
                    for(String url:urls){
                        str = str.replaceAll(url,"");

                        imageUrl = ImageUrl.builder().url(url).build();//urlToBase64Array(url)
                        imageContent = Content.builder().imageUrl(imageUrl).type(Content.Type.IMAGE_URL.getName()).build();
                        contentList.add(imageContent);
                    }
                    textContent = Content.builder().text(str).type(Content.Type.TEXT.getName()).build();
                }
            }else {
                textContent = Content.builder().text(historyMessage.getContent()).type(Content.Type.TEXT.getName()).build();
            }
            contentList.add(textContent);

            MessagePicture message = MessagePicture.builder().role(role).content(contentList).build();
            contextMessages.add(message);

        }

        // 当前用户消息
        Content textContent = new Content();;// Content.builder().text("What’s in this image?").type(Content.Type.TEXT.getName()).build();
        List<Content> contentList = new ArrayList<>();
        urls = CommonEnum.getUrl(questionMessage.getContent());
        if(urls.isEmpty()) {
            textContent = Content.builder().text(questionMessage.getContent()).type(Content.Type.TEXT.getName()).build();
        }else {
            String str = questionMessage.getContent();
            for(String url:urls){
                str = str.replaceAll(url,"");
                imageUrl = ImageUrl.builder().url(url).build();//urlToBase64Array(url)
                imageContent = Content.builder().imageUrl(imageUrl).type(Content.Type.IMAGE_URL.getName()).build();
                contentList.add(imageContent);
            }
            textContent = Content.builder().text(str).type(Content.Type.TEXT.getName()).build();

        }
        contentList.add(textContent);

        MessagePicture message = MessagePicture.builder().role(Message.Role.USER).content(contentList).build();
        contextMessages.add(message);

        return contextMessages;
    }
    //参考图转base64
    //public static String urlToBase64Array(String imageUrl) {
    //    try {
    //        if(imageUrl.contains(CommonEnum.baseUrl)){
    //            File imageFile = new File(FileUtil.getFileSavePathPrefix()+imageUrl.substring(imageUrl.indexOf(CommonEnum.baseUrl)+CommonEnum.baseUrl.length() ));
    //            byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
    //            String base64Image = "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
    //            return base64Image;
    //        }
    //        URL url = new URL(imageUrl);
    //        InputStream inputStream = url.openStream();
    //        byte[] imageBytes = inputStream.readAllBytes();
    //
    //        // 将图片数据转换为Base64格式
    //        String base64Image = "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
    //        return base64Image;
    //    }catch (Exception e){
    //        throw new ServiceException(e.getMessage());
    //    }
    //}
    /**
     * 构建聊天对话请求参数
     *
     * @param questionMessage      问题消息
     * @param roomConfigParamAsMap 房间配置参数
     * @param contentMessages      上下文消息
     * @return 聊天对话请求参数
     */
    public static ChatCompletionWithPicture build4VPChatCompletion(RoomOpenAiChatMsgDO questionMessage, Map<OpenAiChatCellConfigCodeEnum,
            DataWrapper> roomConfigParamAsMap, List<MessagePicture> contentMessages) {
        // 最终的 maxTokens
        int finalMaxTokens;
        // 本次对话剩余的 maxTokens 最大值 = 模型的最大上限 - 本次 prompt 消耗的 tokens - 1
        int currentRemainMaxTokens = OpenAiChatApiModelEnum.maxTokens(questionMessage.getModelName()) - questionMessage.getPromptTokens() - 1;
        // 获取 maxTokens
        DataWrapper maxTokensDataWrapper = roomConfigParamAsMap.get(OpenAiChatCellConfigCodeEnum.MAX_TOKENS);
        // 如果 maxTokens 为空或者大于当前剩余的 maxTokens
        if (maxTokensDataWrapper.isNull() || maxTokensDataWrapper.asInt() > currentRemainMaxTokens) {
            finalMaxTokens = currentRemainMaxTokens;
        } else {
            finalMaxTokens = maxTokensDataWrapper.asInt();
        }

        ChatCompletionWithPicture chatCompletion1 = ChatCompletionWithPicture
                .builder()
                .model(ChatCompletion.Model.GPT_4_VISION_PREVIEW.getName())//questionMessage.getModelName()
                 //[0, 2] 越低越精准
                .temperature(roomConfigParamAsMap.get(OpenAiChatCellConfigCodeEnum.TEMPERATURE).asBigDecimal().doubleValue())
                .topP(1.0)
                // 每次生成一条
                .n(1)
                .presencePenalty(roomConfigParamAsMap.get(OpenAiChatCellConfigCodeEnum.PRESENCE_PENALTY).asBigDecimal().doubleValue())
                .messages(contentMessages)
                .stream(true)
                .maxTokens(4000)
                .build();

        // 构建聊天参数
        return chatCompletion1;


    }


}
