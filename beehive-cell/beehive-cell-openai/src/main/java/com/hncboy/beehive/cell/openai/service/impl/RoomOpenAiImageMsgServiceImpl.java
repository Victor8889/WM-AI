package com.hncboy.beehive.cell.openai.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Pair;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.hncboy.beehive.base.domain.entity.HaExpenseRecordsDo;
import com.hncboy.beehive.base.domain.entity.HaProductsDo;
import com.hncboy.beehive.base.domain.entity.HaUserPermissionsDo;
import com.hncboy.beehive.base.domain.entity.RoomOpenAiImageMsgDO;
import com.hncboy.beehive.base.domain.query.RoomMsgCursorQuery;
import com.hncboy.beehive.base.enums.*;
import com.hncboy.beehive.base.handler.mp.BeehiveServiceImpl;
import com.hncboy.beehive.base.mapper.RoomOpenAiImageMsgMapper;
import com.hncboy.beehive.base.util.FileUtil;
import com.hncboy.beehive.base.util.FrontUserUtil;
import com.hncboy.beehive.base.util.ObjectMapperUtil;
import com.hncboy.beehive.base.util.OkHttpClientUtil;
import com.hncboy.beehive.cell.core.hander.RoomHandler;
import com.hncboy.beehive.cell.core.hander.strategy.CellConfigFactory;
import com.hncboy.beehive.cell.core.hander.strategy.CellConfigStrategy;
import com.hncboy.beehive.cell.core.hander.strategy.DataWrapper;
import com.hncboy.beehive.cell.openai.domain.request.RoomOpenAiImageSendRequest;
import com.hncboy.beehive.cell.openai.domain.vo.DallEParams;
import com.hncboy.beehive.cell.openai.domain.vo.RoomOpenAiImageMsgVO;
import com.hncboy.beehive.cell.openai.enums.OpenAiImageCellConfigCodeEnum;
import com.hncboy.beehive.cell.openai.handler.converter.RoomOpenAiImageMsgConverter;
import com.hncboy.beehive.cell.openai.module.key.OpenAiApiKeyHandler;
import com.hncboy.beehive.cell.openai.service.RoomOpenAiImageMsgService;
import com.hncboy.beehive.web.service.HaExpenseRecordsService;
import com.unfbx.chatgpt.OpenAiClient;
import com.unfbx.chatgpt.entity.images.Image;
import com.unfbx.chatgpt.entity.images.ImageResponse;
import com.unfbx.chatgpt.entity.images.Item;
import com.unfbx.chatgpt.entity.images.SizeEnum;
import jakarta.annotation.Resource;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * @author ll
 * @date 2023/6/3
 * OpenAi 图像房间消息业务实现类
 */
@Slf4j
@Service
public class RoomOpenAiImageMsgServiceImpl extends BeehiveServiceImpl<RoomOpenAiImageMsgMapper, RoomOpenAiImageMsgDO> implements RoomOpenAiImageMsgService {

    @Resource
    private CellConfigFactory cellConfigFactory;

    @Resource
    private com.hncboy.beehive.web.service.HaUserPermissionsService haUserPermissionsService;
    @Resource
    private HaExpenseRecordsService haExpenseRecordsService;

    @Override
    public List<RoomOpenAiImageMsgVO> list(RoomMsgCursorQuery cursorQuery) {
        List<RoomOpenAiImageMsgDO> cursorList = cursorList(cursorQuery, RoomOpenAiImageMsgDO::getId, new LambdaQueryWrapper<RoomOpenAiImageMsgDO>()
                .eq(RoomOpenAiImageMsgDO::getUserId, FrontUserUtil.getUserId())
                .eq(RoomOpenAiImageMsgDO::getRoomId, cursorQuery.getRoomId()));
        return RoomOpenAiImageMsgConverter.INSTANCE.entityToVO(cursorList);
    }

    @Override
    public RoomOpenAiImageMsgVO send(RoomOpenAiImageSendRequest sendRequest) {


        //获取参数
        String str = sendRequest.getRunParams();
        //String size = dp.getSize();

        Image image = null;
        String model = Image.Model.DALL_E_3.getName();
        if(!StringUtil.isNotBlank(str)) {//str为空
            //image = Image.builder()
            //        .model(Image.Model.DALL_E_2.getName())
            //        .prompt(sendRequest.getPrompt())
            //        .size(SizeEnum.size_512.getName())
            //        .build();

            //判断余额是否充足
            RoomHandler.checkRemainPoints(haUserPermissionsService.isRemainPoints(RecordsEnum.getPointsByName(RecordsEnum.IMG_DALLE_3)));
            image = Image.builder()
                    .responseFormat(com.unfbx.chatgpt.entity.images.ResponseFormat.URL.getName())
                    .model(Image.Model.DALL_E_3.getName())
                    .prompt(sendRequest.getPrompt())
                    .n(1)
                    .quality(Image.Quality.HD.getName())
                    .size(SizeEnum.size_1024_1792.getName())
                    .style(Image.Style.NATURAL.getName())
                    .build();
        }else {
            DallEParams dp = parseFromString(str);
            model = dp.getModel();
            // 构建图像生成参数
            if (dp.getModel().equals(Image.Model.DALL_E_2.getName())) {

                //判断余额是否充足
                RoomHandler.checkRemainPoints(haUserPermissionsService.isRemainPoints(RecordsEnum.getPointsByName(RecordsEnum.IMG_DALLE)));
                image = Image.builder()
                        .model(Image.Model.DALL_E_2.getName())
                        .prompt(sendRequest.getPrompt())
                        .size(dp.getSize())
                        .build();
            }else if (dp.getModel().equals(Image.Model.DALL_E_3.getName())) {

                //判断余额是否充足
                RoomHandler.checkRemainPoints(haUserPermissionsService.isRemainPoints(RecordsEnum.getPointsByName(RecordsEnum.IMG_DALLE_3)));
                image = Image.builder()
                        .responseFormat(com.unfbx.chatgpt.entity.images.ResponseFormat.URL.getName())
                        .model(Image.Model.DALL_E_3.getName())
                        .prompt(sendRequest.getPrompt())
                        .n(1)
                        .size(dp.getSize())
                        .style(dp.getStyle())
                        .build();
                if (StringUtil.isNotBlank(dp.getQuality()))
                    image = Image.builder()
                            .responseFormat(com.unfbx.chatgpt.entity.images.ResponseFormat.URL.getName())
                            .model(Image.Model.DALL_E_3.getName())
                            .prompt(sendRequest.getPrompt())
                            .n(1)
                            .quality(Image.Quality.HD.getName())
                            .size(dp.getSize())
                            .style(dp.getStyle())
                            .build();
            }
        }



        // 获取房间配置参数
        CellConfigStrategy cellConfigStrategy = cellConfigFactory.getCellConfigStrategy(sendRequest.getRoomId(), CellCodeEnum.OPENAI_IMAGE);
        //(!StringUtil.isNotBlank(str) || model.equals(Image.Model.DALL_E_3.getName())) ? CellCodeEnum.OPENAI_DALLE3 :

        Map<OpenAiImageCellConfigCodeEnum, DataWrapper> roomConfigParamAsMap = cellConfigStrategy.getRoomConfigParamAsMap(sendRequest.getRoomId());

        String apiKey = "";
        String url = "";
        if(model.equals(Image.Model.DALL_E_3.getName())) {
            apiKey = CommonEnum.dalle3Api.get(0).getApiKey();
            url = CommonEnum.dalle3Api.get(0).getBaseUrl();
        }else{

            // 获取 ApiKey 相关信息
            Pair<String, String> chatApiKeyInfoPair = OpenAiApiKeyHandler.getImageApiKeyInfo(
                    roomConfigParamAsMap.get(OpenAiImageCellConfigCodeEnum.API_KEY).asString(),
                    roomConfigParamAsMap.get(OpenAiImageCellConfigCodeEnum.OPENAI_BASE_URL).asString(),
                    roomConfigParamAsMap.get(OpenAiImageCellConfigCodeEnum.KEY_STRATEGY).asString());
            url = chatApiKeyInfoPair.getValue();
            apiKey = chatApiKeyInfoPair.getKey();
        }

        // 创建问题消息
        RoomOpenAiImageMsgDO questionMessage = new RoomOpenAiImageMsgDO();
        questionMessage.setUserId(FrontUserUtil.getUserId());
        questionMessage.setRoomId(sendRequest.getRoomId());
        questionMessage.setMessageType(MessageTypeEnum.QUESTION);
        questionMessage.setApiKey(apiKey);
        questionMessage.setSize(image.getSize());
        questionMessage.setPrompt(sendRequest.getPrompt());
        questionMessage.setOriginalData(ObjectMapperUtil.toJson(image));
        questionMessage.setStatus(MessageStatusEnum.INIT);
        questionMessage.setRoomConfigParamJson(ObjectMapperUtil.toJson(roomConfigParamAsMap));
        questionMessage.setModel(model);
        save(questionMessage);

        // 构建 OpenAiClient
        OpenAiClient openAiClient = OpenAiClient.builder()
                .apiKey(Collections.singletonList(apiKey))
                .okHttpClient(OkHttpClientUtil.getProxyInstance())
                .apiHost(url)
                .build();

        // 构建回答消息
        RoomOpenAiImageMsgDO answerMessage = new RoomOpenAiImageMsgDO();
        answerMessage.setId(IdWorker.getId());
        answerMessage.setUserId(questionMessage.getUserId());
        answerMessage.setRoomId(questionMessage.getRoomId());
        answerMessage.setParentQuestionMessageId(questionMessage.getId());
        answerMessage.setMessageType(MessageTypeEnum.ANSWER);
        answerMessage.setApiKey(questionMessage.getApiKey());
        answerMessage.setSize(questionMessage.getSize());
        answerMessage.setPrompt(questionMessage.getPrompt());
        answerMessage.setRoomConfigParamJson(questionMessage.getRoomConfigParamJson());
        answerMessage.setModel(model);

        boolean isSucc = false;
        try {
            ImageResponse imageResponse = openAiClient.genImages(image);
            answerMessage.setOriginalData(ObjectMapperUtil.toJson(imageResponse));

            if (Objects.isNull(imageResponse) || CollUtil.isEmpty(imageResponse.getData())) {
                answerMessage.setResponseErrorData("生成图片数据为空");
                answerMessage.setStatus(MessageStatusEnum.FAILURE);
            } else {
                Item item = imageResponse.getData().get(0);
                answerMessage.setOpenaiImageUrl(item.getUrl());
                // 构建图片名称
                String imageName = "image-".concat(String.valueOf(answerMessage.getId())).concat(".png");
                // 下载图片
                Pair<Boolean, String> pb = FileUtil.putDalleFile(imageName,answerMessage.getOpenaiImageUrl());
                if(pb.getKey()){
                    answerMessage.setImageName(CommonEnum.dallefile +"/" + imageName);
                }else{
                    FileUtil.downloadFromUrl(answerMessage.getOpenaiImageUrl(), imageName);
                    answerMessage.setImageName(imageName);
                }
                isSucc = true;
                answerMessage.setStatus(MessageStatusEnum.SUCCESS);
            }
        } catch (Exception e) {
            log.error("OpenAi 图像生成异常", e);
            answerMessage.setResponseErrorData("图像生成异常：" + e.getMessage());
            answerMessage.setStatus(MessageStatusEnum.FAILURE);
        }
        if(isSucc){
            HaProductsDo hpd = CommonEnum.NAME_MAP_PRODUCT.get(answerMessage.getModel());
            //RecordsEnum.getByName(RecordsEnum.fromApiModel(answerMessage.getModel()));

            HaUserPermissionsDo hsp = haUserPermissionsService.getOne(answerMessage.getUserId());
            int record = hpd.getRecords();
            if(hsp.getIsVip() == CommonEnum.isVip && (new Date()).before(hsp.getValidyDate()))
                record = hpd.getVipRecords();

            if(addRecords(-record,hpd.getModel(),hpd.getName(),answerMessage.getId(),answerMessage.getUserId())){
                if(haUserPermissionsService.updatePoints(answerMessage.getUserId(),-record)) {//hpd.getRecords()
                    answerMessage.setIsDeducted(1);
                    //openAiImageMsgService.update(da);
                }
            }
        }

        // 保存回答消息
        save(answerMessage);
        questionMessage.setStatus(answerMessage.getStatus());
        // 更新问题消息
        updateById(questionMessage);

        return RoomOpenAiImageMsgConverter.INSTANCE.entityToVO(answerMessage);
    }
    private boolean addRecords(int points,String model,String name,long modelId,int userId){
        HaExpenseRecordsDo herd = new HaExpenseRecordsDo();
        herd.setUserId(userId);
        herd.setModel(model);
        herd.setMark(name);
        herd.setHoldBi(points);
        herd.setModelId(modelId);

        return haExpenseRecordsService.save(herd);
    }
    public DallEParams parseFromString(String paramString) {
        Map<String, String> paramMap = new HashMap<>();
        String[] keyValuePairs = paramString.split(",");

        for (String pair : keyValuePairs) {
            String[] keyValue = pair.split(":");
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                paramMap.put(key, value);
            }
        }

        String model = paramMap.get("model");
        String size = paramMap.get("size");
        String style = paramMap.get("style");
        String quality = paramMap.get("quality");

        return new DallEParams(model, size, style, quality);
    }
    @Override
    public boolean update(RoomOpenAiImageMsgDO roomOpenAiImageMsgDO) {
        return this.updateById(roomOpenAiImageMsgDO);
    }

    @Override
    public List<RoomOpenAiImageMsgDO> getComQrList() {
        // 构建查询条件，根据userid查询数据
        QueryWrapper<RoomOpenAiImageMsgDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("user_id,id,model");

        queryWrapper.gt("create_time", LocalDateTime.now().minus(1, ChronoUnit.HOURS));
        queryWrapper.eq("status", MessageStatusEnum.SUCCESS.getCode());
        queryWrapper.eq("message_type", MessageTypeEnum.ANSWER);
        queryWrapper.eq("is_deducted", PayStatusEnum.INIT.getCode());
        // 根据条件查询一条数据
        return this.list(queryWrapper);
    }
}
