package com.hncboy.beehive.cell.openai.module.chat.emitter;

import cn.hutool.core.lang.Pair;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.hncboy.beehive.base.domain.entity.HaProductsDo;
import com.hncboy.beehive.base.domain.entity.HaUserPermissionsDo;
import com.hncboy.beehive.base.domain.entity.RoomOpenAiChatMsgDO;
import com.hncboy.beehive.base.enums.*;
import com.hncboy.beehive.base.exception.ServiceException;
import com.hncboy.beehive.base.util.*;
import com.hncboy.beehive.cell.core.hander.strategy.CellConfigStrategy;
import com.hncboy.beehive.cell.core.hander.strategy.DataWrapper;
import com.hncboy.beehive.cell.openai.domain.request.RoomOpenAiChatSendRequest;
import com.hncboy.beehive.cell.openai.enums.OpenAiChatApiModelEnum;
import com.hncboy.beehive.cell.openai.enums.OpenAiChatCellConfigCodeEnum;
import com.hncboy.beehive.cell.openai.module.chat.apikey.*;
import com.hncboy.beehive.cell.openai.module.chat.listener.ParsedEventSourceListener;
import com.hncboy.beehive.cell.openai.module.chat.listener.ResponseBodyEmitterStreamListener;
import com.hncboy.beehive.cell.openai.module.chat.parser.ChatCompletionResponseParser;
import com.hncboy.beehive.cell.openai.module.chat.storage.ApiKeyDatabaseDataStorage;
import com.hncboy.beehive.cell.openai.module.key.OpenAiApiKeyHandler;
import com.hncboy.beehive.cell.openai.service.RoomOpenAiChatMsgService;
import com.hncboy.beehive.web.domain.request.HaUserParamRequest;
import com.hncboy.beehive.web.service.HaUserParamService;
import com.unfbx.chatgpt.OpenAiStreamClient;
import com.unfbx.chatgpt.entity.chat.ChatCompletion;
import com.unfbx.chatgpt.entity.chat.ChatCompletionWithPicture;
import com.unfbx.chatgpt.entity.chat.Message;
import com.unfbx.chatgpt.entity.chat.MessagePicture;
import com.unfbx.chatgpt.function.KeyRandomStrategy;
import com.unfbx.chatgpt.interceptor.DynamicKeyOpenAiAuthInterceptor;
import com.unfbx.chatgpt.utils.TikTokensUtil;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author ll
 * @date 2023-3-24
 * OpenAi 对话房间消息响应处理
 */
@Lazy
@Component
public class RoomOpenAiChatApiResponseEmitter implements RoomOpenAiChatResponseEmitter {

    @Resource
    private com.hncboy.beehive.web.service.HaExpenseRecordsService haExpenseRecordsService;

    @Resource
    private HaUserParamService haUserParamService;

    @Resource
    private ChatCompletionResponseParser parser;

    @Resource
    private ApiKeyDatabaseDataStorage dataStorage;

    @Resource
    private RoomOpenAiChatMsgService roomOpenAiChatMsgService;
    @Resource
    private com.hncboy.beehive.web.service.HaUserPermissionsService haUserPermissionsService;

    @Override
    public void requestToResponseEmitter(RoomOpenAiChatSendRequest sendRequest, ResponseBodyEmitter emitter, CellConfigStrategy cellConfigStrategy) {
        // 获取房间配置参数
        Map<OpenAiChatCellConfigCodeEnum, DataWrapper> roomConfigParamAsMap = cellConfigStrategy.getRoomConfigParamAsMap(sendRequest.getRoomId());

        HaUserParamRequest hspr= haUserParamService.getOne();
        String apiKey = roomConfigParamAsMap.get(OpenAiChatCellConfigCodeEnum.API_KEY).asString();
        if(StringUtils.isNotBlank(hspr.getApikey()))
            apiKey =  hspr.getApikey();
                    // 获取 ApiKey 相关信息

        boolean isRemainPoints = false;
        HaUserPermissionsDo hpd = haUserPermissionsService.getRemainPointsCount();
        if(hpd.getIsEnable() == CommonEnum.commonTwo){
            System.out.println(hpd.getUserId()+",账户异常，被禁用");
            ResponseBodyEmitterUtil.sendWithComplete(emitter, "账户异常，请联系管理员");
            return;
        }
        int remainPoints = hpd.getRemainHoldBi();

        //修改获取model单价的方法，改为直接获取数据库的商品价格
        if(remainPoints >= RecordsEnum.getPointsByApiModel(hspr.getChatModel()))// getPointsByName(RecordsEnum.fromApiModel(hspr.getChatModel())))
            //if(remainPoints >= RecordsEnum.getPointsByName(hspr.getChatModel()))
            isRemainPoints = true;


        //如果是3.5则重新判断免费积分是否还有
        if(!isRemainPoints && hpd.getIsVip() == CommonEnum.commonOne && (OpenAiChatApiModelEnum.GPT_3_5_TURBO_1106.getName().equals(hspr.getChatModel()) || OpenAiChatApiModelEnum.GPT_3_5_TURBO.getName().equals(hspr.getChatModel()))){
            if(hpd.getRemainHoldCount() > RecordsEnum.getPointsByApiModel(hspr.getChatModel())  )//.getPointsByName(RecordsEnum.GPT_3_5_TURBO)
                isRemainPoints = true;
        }
        if(sendRequest.getRoomId() == ChatGptRoomIdEnum.MJ_ROOM_ID.getCode()
                || sendRequest.getRoomId() == ChatGptRoomIdEnum.SWD_ROOM_ID.getCode() || sendRequest.getRoomId() == ChatGptRoomIdEnum.DALLE_ROOM_ID.getCode()
                || sendRequest.getRoomId() == ChatGptRoomIdEnum.SD_ROOM_ID.getCode() ) {//不关联上下文---纯翻译

            //不关联上下文
            roomConfigParamAsMap.put(OpenAiChatCellConfigCodeEnum.CONTEXT_COUNT, new DataWrapper(0));
                //翻译等重置为gpt3.5
                hspr.setChatModel(OpenAiChatApiModelEnum.GPT_3_5_TURBO_1106.getName());
                roomConfigParamAsMap.put(OpenAiChatCellConfigCodeEnum.MODEL, new DataWrapper(OpenAiChatApiModelEnum.GPT_3_5_TURBO_1106.getName()));
        }
        else
            roomConfigParamAsMap.put(OpenAiChatCellConfigCodeEnum.CONTEXT_COUNT,new DataWrapper(hspr.getContextCount()));

            if(hspr.getIsContext() == CommonEnum.commonOne ||
                OpenAiChatApiModelEnum.STABLE_DIFFUSION.getName().equals(hspr.getChatModel()) )
            roomConfigParamAsMap.put(OpenAiChatCellConfigCodeEnum.CONTEXT_COUNT, new DataWrapper(0));

        //获取是否强制关联上下文
        HaProductsDo hpdo =CommonEnum.getProductByApiModel(hspr.getChatModel());
        if(hpdo.getIsContext() != null ){
            if(hpdo.getIsContext() <= 0)
                roomConfigParamAsMap.put(OpenAiChatCellConfigCodeEnum.CONTEXT_COUNT, new DataWrapper(0));
            else
                roomConfigParamAsMap.put(OpenAiChatCellConfigCodeEnum.CONTEXT_COUNT,new DataWrapper(hpdo.getIsContext()));
        }


        Pair<String, String> chatApiKeyInfoPair = OpenAiApiKeyHandler.getProductChatApiKeyInfo(
                apiKey,//roomConfigParamAsMap.get(OpenAiChatCellConfigCodeEnum.API_KEY).asString(),
                roomConfigParamAsMap.get(OpenAiChatCellConfigCodeEnum.OPENAI_BASE_URL).asString(),
                roomConfigParamAsMap.get(OpenAiChatCellConfigCodeEnum.KEY_STRATEGY).asString(),
                hspr.getChatModel()//roomConfigParamAsMap.get(OpenAiChatCellConfigCodeEnum.MODEL).asString()//个人参数获取模型
                //roomConfigParamAsMap.get(OpenAiChatCellConfigCodeEnum.MODEL).asString()
        );
        // 覆盖原始值
        roomConfigParamAsMap.put(OpenAiChatCellConfigCodeEnum.API_KEY, new DataWrapper(chatApiKeyInfoPair.getKey()));
        roomConfigParamAsMap.put(OpenAiChatCellConfigCodeEnum.OPENAI_BASE_URL, new DataWrapper(chatApiKeyInfoPair.getValue()));
        if(null == hspr) {
            ResponseBodyEmitterUtil.sendWithComplete(emitter, "账户异常，请联系管理员");
            //RoomHandler.checkRemainPoints(isRemainPoints);
            return;
        }
        roomConfigParamAsMap.put(OpenAiChatCellConfigCodeEnum.TEMPERATURE, new DataWrapper(hspr.getTemperature().toString()));
        if(StringUtils.isNotBlank(hspr.getChatModel()))
            roomConfigParamAsMap.put(OpenAiChatCellConfigCodeEnum.MODEL, new DataWrapper(hspr.getChatModel()));

        roomConfigParamAsMap.put(OpenAiChatCellConfigCodeEnum.PRESENCE_PENALTY, new DataWrapper(hspr.getPresencePenalty().toString()));
        if(StringUtils.isNotBlank(hspr.getSystemMessage()))
            roomConfigParamAsMap.put(OpenAiChatCellConfigCodeEnum.SYSTEM_MESSAGE, new DataWrapper(hspr.getSystemMessage()));
        //翻译判断对应模块的积分是否充足
        if(sendRequest.getRoomId() == ChatGptRoomIdEnum.MJ_ROOM_ID.getCode() &&  remainPoints >= RecordsEnum.getPointsByName(RecordsEnum.IMG_MJ))
            isRemainPoints = true;
        else if( sendRequest.getRoomId() == ChatGptRoomIdEnum.DALLE_ROOM_ID.getCode() &&  remainPoints >= RecordsEnum.getPointsByName(RecordsEnum.IMG_DALLE))
        isRemainPoints = true;
        else if(sendRequest.getRoomId() == ChatGptRoomIdEnum.SD_ROOM_ID.getCode()  &&  remainPoints >= RecordsEnum.getPointsByName(RecordsEnum.SIWEIDAOTU))
            isRemainPoints = true;
        //判断余额是否充足
        //RoomHandler.checkRemainPoints(!isRemainPoints);//页面弹出
        if(!isRemainPoints) {
            ResponseBodyEmitterUtil.sendWithComplete(emitter, "积分不足，请先充值；充值VIP更划算，每月仅需4元");
            //RoomHandler.checkRemainPoints(isRemainPoints);
            return;
        }
        // 初始化问题消息
        RoomOpenAiChatMsgDO questionMessage = initQuestionMessage(sendRequest, roomConfigParamAsMap);

        // 构建上下文消息
        LinkedList<Message> contentMessages = ChatCompletionBuildUtil.buildContextMessage(questionMessage, roomConfigParamAsMap);
        if(questionMessage.getModelName().contains(CommonEnum.isGpt) && (null != hpdo.getDescript() && !hpdo.getDescript().contains(CommonEnum.noGpt))) {
            // 获取上下文的 token 数量设置 promptTokens
            String modelName = CommonEnum.NAME_MAP_PRODUCT.get(questionMessage.getModelName()).getApiModel();
            try {
                questionMessage.setPromptTokens(TikTokensUtil.tokens(modelName, contentMessages));//TikTokensUtil.tokens(modelName, contentMessages)
            }catch(Exception e){
                String new_modelName = OpenAiChatApiModelEnum.NAME_MAP.get(OpenAiChatApiModelEnum.GPT_4.getName()).getCalcTokenModelName();//OpenAiChatApiModelEnum.GPT_4.getName();
                if(questionMessage.getModelName().contains("-3-"))
                    new_modelName = OpenAiChatApiModelEnum.NAME_MAP.get(OpenAiChatApiModelEnum.GPT_3_5_TURBO.getName()).getCalcTokenModelName();
                questionMessage.setPromptTokens(TikTokensUtil.tokens(new_modelName, contentMessages));
            }
            //OpenAiChatApiModelEnum.NAME_MAP.get(questionMessage.getModelName()).getCalcTokenModelName()
        }else {
            String modelName = OpenAiChatApiModelEnum.NAME_MAP.get(OpenAiChatApiModelEnum.GPT_4.getName()).getCalcTokenModelName();//OpenAiChatApiModelEnum.GPT_4.getName();
            if(questionMessage.getModelName().contains("-3-"))
                modelName = OpenAiChatApiModelEnum.NAME_MAP.get(OpenAiChatApiModelEnum.GPT_3_5_TURBO.getName()).getCalcTokenModelName();////OpenAiChatApiModelEnum.GPT_3_5_TURBO.getName();
            questionMessage.setPromptTokens(TikTokensUtil.tokens(modelName, contentMessages));//
        }//后面需要处理非gpt的sokens问题


        // 构建聊天对话请求参数
        ChatCompletion chatCompletion = ChatCompletionBuildUtil.buildChatCompletion(questionMessage, roomConfigParamAsMap, contentMessages);
        if(OpenAiChatApiModelEnum.GPT_4_128K.getName().equals(hspr.getChatModel()))
            chatCompletion.setMaxTokens(4090);

        questionMessage.setOriginalData(ObjectMapperUtil.toJson(chatCompletion));

        // 构建错误内容处理节点
        List<ChatErrorNode> chatErrorNodes = new ArrayList<>();
        chatErrorNodes.add(SpringUtil.getBean(ChatTokenLimitErrorNode.class));
        //chatErrorNodes.add(SpringUtil.getBean(ChatLocalSensitiveWordErrorNode.class));//单独处理----不需要本地只做百度校验
        //chatErrorNodes.add(SpringUtil.getBean(ChatBaiduAipErrorNode.class));
        for (ChatErrorNode chatErrorNode : chatErrorNodes) {
            Pair<Boolean, String> errorHandleResultPair = chatErrorNode.doHandle(questionMessage, roomConfigParamAsMap.get(OpenAiChatCellConfigCodeEnum.ENABLED_BAIDU_AIP).asBoolean());
            if (!errorHandleResultPair.getKey()) {

                // 保存问题消息
                roomOpenAiChatMsgService.save(questionMessage);
                // 保存错误的回答消息
                saveErrorAnswerQuestion(questionMessage, errorHandleResultPair.getValue());
                // 发送错误消息
                ResponseBodyEmitterUtil.sendWithComplete(emitter, errorHandleResultPair.getValue());
                return;
            }
        }
        ChatErrorNode chatErrorNode = SpringUtil.getBean(ChatBaiduAipErrorNode.class);

        String question = questionMessage.getContent();
        try {
        Pair<Boolean, String> errorHandleResultPair = chatErrorNode.doHandle(questionMessage, roomConfigParamAsMap.get(OpenAiChatCellConfigCodeEnum.ENABLED_BAIDU_AIP).asBoolean());
        question = questionMessage.getContent();
            if (!errorHandleResultPair.getKey()) {
                //获取敏感词
                System.out.println(errorHandleResultPair.getValue());
                String words = chatErrorNode.getWords(errorHandleResultPair.getValue());
                if (!words.isBlank()) {
                    String[] wordsArray = words.split(",");
                    for (int i = 0; i < wordsArray.length; i++) {
                        String word = wordsArray[i];
                        String newWord = chatErrorNode.getFirstLetter(word);
                        question = question.replaceAll(word, newWord);
                    }
                }
            }
        }catch (Exception e){
             chatErrorNode = SpringUtil.getBean(ChatLocalSensitiveWordErrorNode.class);
            question = questionMessage.getContent();
            Pair<Boolean, String>  errorHandleResultPair = chatErrorNode.doHandle(questionMessage, true);
            if (!errorHandleResultPair.getKey()) {
                //获取敏感词
               String words = errorHandleResultPair.getValue();

                //System.out.println("`````````````````````````"+words);
                List<String> mutableList = Arrays.asList(words.split(","));;
                if (!mutableList.isEmpty()) {
                    for (int i = 0; i < mutableList.size(); i++) {
                        String word = mutableList.get(i);
                        String newWord = chatErrorNode.getFirstLetter(word);
                        question = question.replaceAll(word, newWord);
                    }
                }
            }
        }

        questionMessage.setContent(question);
        // 保存问题消息
        try {
            roomOpenAiChatMsgService.save(questionMessage);
        }catch (Exception e){
            if(e.getMessage().contains("Data too long"))
                throw new ServiceException("对话长度超过最大值，减少输入或者上下文数量");
                //ResponseBodyEmitterUtil.sendWithComplete(emitter, "");

            return;
        }
        System.out.println(new Date() + "--start call server:" + roomConfigParamAsMap.get(OpenAiChatCellConfigCodeEnum.OPENAI_BASE_URL).asString());
        //addRecords(questionMessage.getPromptTokens(),questionMessage.getId());//添加消费记录
        ParsedEventSourceListener parsedEventSourceListener = new ParsedEventSourceListener.Builder()
//                .addListener(new ConsoleStreamListener())
                .addListener(new ResponseBodyEmitterStreamListener(emitter))
                .setParser(parser)
                .setDataStorage(dataStorage)
                .setQuestionMessageDO(questionMessage)
                .build();
        if(OpenAiChatApiModelEnum.GPT_4_VISION.getName().equals(hspr.getChatModel())) {
            OpenAiStreamClient streamClient = OpenAiStreamClient.builder()
                    //支持多key传入，请求时候随机选择
                    .apiKey(Collections.singletonList(roomConfigParamAsMap.get(OpenAiChatCellConfigCodeEnum.API_KEY).asString()))
                    //自定义key的获取策略：默认KeyRandomStrategy
                    .keyStrategy(new KeyRandomStrategy())
                    .authInterceptor(new DynamicKeyOpenAiAuthInterceptor())
                    .okHttpClient(OkHttpClientUtil.getProxyInstance())
                    //自己做了代理就传代理地址，没有可不不传,(关注公众号回复：openai ，获取免费的测试代理地址)
                    .apiHost(roomConfigParamAsMap.get(OpenAiChatCellConfigCodeEnum.OPENAI_BASE_URL).asString())
                    .build();

            //构建messageList
            List<MessagePicture> ml = ChatCompletionBuildUtil.build4VPContextMessage(questionMessage, roomConfigParamAsMap);
            ChatCompletionWithPicture chatCompletion1 = ChatCompletionBuildUtil.build4VPChatCompletion(questionMessage, roomConfigParamAsMap, ml);

            streamClient.streamChatCompletion(chatCompletion1, parsedEventSourceListener);

            //pictureChatV4(questionMessage,client);
        }else{
            if(OpenAiChatApiModelEnum.GPT_4.getName().equals(hspr.getChatModel()) && questionMessage.getPromptTokens() > 200){
                Pair<String, String> chatApiKeyInfoPair1 = OpenAiApiKeyHandler.getChatApiKeyInfo(
                        "",//roomConfigParamAsMap.get(OpenAiChatCellConfigCodeEnum.API_KEY).asString(),
                        roomConfigParamAsMap.get(OpenAiChatCellConfigCodeEnum.OPENAI_BASE_URL).asString(),
                        roomConfigParamAsMap.get(OpenAiChatCellConfigCodeEnum.KEY_STRATEGY).asString(),
                        hspr.getChatModel()//CommonEnum.isGpt4_0//hspr.getChatModel()//roomConfigParamAsMap.get(OpenAiChatCellConfigCodeEnum.MODEL).asString()//个人参数获取模型
                        //roomConfigParamAsMap.get(OpenAiChatCellConfigCodeEnum.MODEL).asString()
                );
                if(null != chatApiKeyInfoPair1) {
                    roomConfigParamAsMap.put(OpenAiChatCellConfigCodeEnum.API_KEY, new DataWrapper(chatApiKeyInfoPair1.getKey()));
                    roomConfigParamAsMap.put(OpenAiChatCellConfigCodeEnum.OPENAI_BASE_URL, new DataWrapper(chatApiKeyInfoPair1.getValue()));
                }
            }
            OkHttpClient okHttpClient = OkHttpClientUtil.getProxyInstance();
            if(CommonEnum.long_time.equals(hpdo.getDescript()))
                okHttpClient = new OkHttpClient.Builder()
                        .connectTimeout(60 * 20, TimeUnit.SECONDS)
                        .readTimeout(60 * 20, TimeUnit.SECONDS)
                        .writeTimeout(60 * 20, TimeUnit.SECONDS).build();
                // 构建 OpenAiStreamClient
        OpenAiStreamClient openAiStreamClient = OpenAiStreamClient.builder()
                .okHttpClient(okHttpClient)
                .apiKey(Collections.singletonList(roomConfigParamAsMap.get(OpenAiChatCellConfigCodeEnum.API_KEY).asString()))
                .apiHost(roomConfigParamAsMap.get(OpenAiChatCellConfigCodeEnum.OPENAI_BASE_URL).asString())
                .build();

            openAiStreamClient.streamChatCompletion(chatCompletion, parsedEventSourceListener);
            System.out.println(new Date() + "--end call server:" );
        }
    }


    //参考图转base64
    public String urlToBase64Array(String imageUrl) {
        try {
            if(imageUrl.contains(CommonEnum.baseUrl)){
                File imageFile = new File(FileUtil.getFileSavePathPrefix()+imageUrl.substring(imageUrl.indexOf(CommonEnum.baseUrl)+CommonEnum.baseUrl.length() ));
                byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
                String base64Image = "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
                return base64Image;
            }
            URL url = new URL(imageUrl);
            InputStream inputStream = url.openStream();
            byte[] imageBytes = inputStream.readAllBytes();

            // 将图片数据转换为Base64格式
            String base64Image = "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
            return base64Image;
        }catch (Exception e){
        throw new ServiceException(e.getMessage());
        }
    }

    /**
     * 初始化问题消息
     *
     * @param sendRequest          发送的消息
     * @param roomConfigParamAsMap 房间配置参数
     * @return 问题消息
     */
    private RoomOpenAiChatMsgDO initQuestionMessage(RoomOpenAiChatSendRequest sendRequest, Map<OpenAiChatCellConfigCodeEnum, DataWrapper> roomConfigParamAsMap) {
        RoomOpenAiChatMsgDO questionMessage = new RoomOpenAiChatMsgDO();
        questionMessage.setId(IdWorker.getId());
        questionMessage.setUserId(FrontUserUtil.getUserId());
        questionMessage.setRoomId(sendRequest.getRoomId());
        questionMessage.setIp(WebUtil.getIp());
        questionMessage.setMessageType(MessageTypeEnum.QUESTION);
        questionMessage.setModelName(roomConfigParamAsMap.get(OpenAiChatCellConfigCodeEnum.MODEL).asString());
        questionMessage.setApiKey(roomConfigParamAsMap.get(OpenAiChatCellConfigCodeEnum.API_KEY).asString());
        questionMessage.setRoomConfigParamJson(ObjectMapperUtil.toJson(roomConfigParamAsMap));
        questionMessage.setContent(sendRequest.getText());
        questionMessage.setStatus(RoomOpenAiChatMsgStatusEnum.INIT);
        return questionMessage;
    }

    /**
     * 保存错误消息
     *
     * @param questionMessage 问题消息
     * @param content         错误内容
     */
    private void saveErrorAnswerQuestion(RoomOpenAiChatMsgDO questionMessage, String content) {
        RoomOpenAiChatMsgDO answerMessage = new RoomOpenAiChatMsgDO();
        answerMessage.setId(IdWorker.getId());
        answerMessage.setUserId(questionMessage.getUserId());
        answerMessage.setRoomId(questionMessage.getRoomId());
        answerMessage.setParentQuestionMessageId(questionMessage.getId());
        answerMessage.setMessageType(MessageTypeEnum.ANSWER);
        answerMessage.setModelName(questionMessage.getModelName());
        answerMessage.setIp(questionMessage.getIp());
        answerMessage.setApiKey(questionMessage.getApiKey());
        answerMessage.setContent(content);
        answerMessage.setOriginalData(null);
        answerMessage.setPromptTokens(0);
        answerMessage.setCompletionTokens(0);
        answerMessage.setTotalTokens(0);
        answerMessage.setStatus(questionMessage.getStatus());
        roomOpenAiChatMsgService.save(answerMessage);
    }
}
