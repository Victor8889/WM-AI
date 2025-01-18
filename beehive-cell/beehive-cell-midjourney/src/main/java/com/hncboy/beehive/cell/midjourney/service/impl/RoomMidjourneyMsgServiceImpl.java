package com.hncboy.beehive.cell.midjourney.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baidu.aip.contentcensor.AipContentCensor;
import com.baidu.aip.contentcensor.EImgType;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hncboy.beehive.base.domain.entity.HaExpenseRecordsDo;
import com.hncboy.beehive.base.domain.entity.HaProductsDo;
import com.hncboy.beehive.base.domain.entity.HaUserPermissionsDo;
import com.hncboy.beehive.base.domain.entity.RoomMidjourneyMsgDO;
import com.hncboy.beehive.base.domain.query.MjPageQuery;
import com.hncboy.beehive.base.enums.*;
import com.hncboy.beehive.base.exception.ServiceException;
import com.hncboy.beehive.base.handler.mp.BeehiveServiceImpl;
import com.hncboy.beehive.base.mapper.RoomMidjourneyMsgMapper;
import com.hncboy.beehive.base.resource.aip.BaiduAipHandler;
import com.hncboy.beehive.base.resource.aip.BaiduAipUtil;
import com.hncboy.beehive.base.resource.aip.JdOSSUtil;
import com.hncboy.beehive.base.util.FileUtil;
import com.hncboy.beehive.base.util.FrontUserUtil;
import com.hncboy.beehive.base.util.PageUtil;
import com.hncboy.beehive.cell.core.hander.RoomHandler;
import com.hncboy.beehive.cell.core.hander.strategy.DataWrapper;
import com.hncboy.beehive.cell.midjourney.constant.MidjourneyConstant;
import com.hncboy.beehive.cell.midjourney.domain.request.MjConvertRequest;
import com.hncboy.beehive.cell.midjourney.domain.request.MjDescribeRequest;
import com.hncboy.beehive.cell.midjourney.domain.request.MjImagineRequest;
import com.hncboy.beehive.cell.midjourney.domain.vo.RoomMidjourneyMsgVO;
import com.hncboy.beehive.cell.midjourney.handler.MidjourneyRoomMsgHandler;
import com.hncboy.beehive.cell.midjourney.handler.MidjourneyTaskQueueHandler;
import com.hncboy.beehive.cell.midjourney.handler.cell.MidjourneyCellConfigCodeEnum;
import com.hncboy.beehive.cell.midjourney.handler.cell.MidjourneyCellConfigStrategy;
import com.hncboy.beehive.cell.midjourney.handler.cell.MidjourneyProperties;
import com.hncboy.beehive.cell.midjourney.handler.converter.MidjourneyPageConverter;
import com.hncboy.beehive.cell.midjourney.handler.converter.RoomMidjourneyMsgConverter;
import com.hncboy.beehive.cell.midjourney.service.DiscordSendService;
import com.hncboy.beehive.cell.midjourney.service.DiscordService;
import com.hncboy.beehive.cell.midjourney.service.RoomMidjourneyMsgService;
import com.hncboy.beehive.cell.midjourney.util.MjRoomMessageUtil;
import com.hncboy.beehive.web.service.HaExpenseRecordsService;
import jakarta.annotation.Resource;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * @author ll
 * @date 2023/5/18
 * Midjourney 房间消息业务接口实现类
 */
@Slf4j
@Service
public class RoomMidjourneyMsgServiceImpl extends BeehiveServiceImpl<RoomMidjourneyMsgMapper, RoomMidjourneyMsgDO> implements RoomMidjourneyMsgService {

    @Resource
    private MidjourneyCellConfigStrategy midjourneyCellConfigStrategy;

    @Resource
    private DiscordService discordService;

    @Resource
    private MidjourneyTaskQueueHandler midjourneyTaskQueueHandler;

    @Resource
    private com.hncboy.beehive.web.service.HaUserPermissionsService haUserPermissionsService;


    List<MidjourneyMsgStatusEnum> runList = Arrays.asList(MidjourneyMsgStatusEnum.MJ_WAIT_RECEIVED,MidjourneyMsgStatusEnum.SYS_WAIT,MidjourneyMsgStatusEnum.MJ_IN_PROGRESS,
            MidjourneyMsgStatusEnum.SYS_MAX_QUEUE,MidjourneyMsgStatusEnum.SYS_QUEUING);
    @Override
    public List<RoomMidjourneyMsgVO> listMj(Integer mjType) {
        List<RoomMidjourneyMsgDO> roomMidjourneyMsgDOList = list(new LambdaQueryWrapper<RoomMidjourneyMsgDO>()
                .eq(RoomMidjourneyMsgDO::getUserId, FrontUserUtil.getUserId())
                .eq(RoomMidjourneyMsgDO::getMjType, mjType)
                .in(RoomMidjourneyMsgDO::getStatus,runList)
                // 根据主键降序
                .orderByDesc(RoomMidjourneyMsgDO::getId)
        );


        //List<RoomMidjourneyMsgDO> roomMidjourneyMsgDOList = List( RoomMidjourneyMsgDO::getId, new LambdaQueryWrapper<RoomMidjourneyMsgDO>()
        //        .eq(RoomMidjourneyMsgDO::getUserId, FrontUserUtil.getUserId())
        //        .in(RoomMidjourneyMsgDO::getStatus,runList)
        //        // 根据主键降序
        //        .orderByDesc(RoomMidjourneyMsgDO::getId)
        //);
        return RoomMidjourneyMsgConverter.INSTANCE.entityToVO(roomMidjourneyMsgDOList);
    }
    public IPage<RoomMidjourneyMsgVO> pageMj(MjPageQuery mjPageQuery) {
        Page<RoomMidjourneyMsgDO> mjPage = page(new Page<>(mjPageQuery.getPageNum(), mjPageQuery.getPageSize()), new LambdaQueryWrapper<RoomMidjourneyMsgDO>()
                // 自己的房间
                .eq(RoomMidjourneyMsgDO::getUserId, FrontUserUtil.getUserId())
                // 名称模糊查询
                //.like(StrUtil.isNotEmpty(roomPageQuery.getName()), RoomDO::getName, roomPageQuery.getName())
                // 未删除的
                .eq(RoomMidjourneyMsgDO::getMjType, mjPageQuery.getMjType())
                // 根据固定时间降序
                .notIn(RoomMidjourneyMsgDO::getStatus,runList)
                // 根据主键降序
                .orderByDesc(RoomMidjourneyMsgDO::getId));
        return PageUtil.toPage(mjPage, MidjourneyPageConverter.INSTANCE.entityToVO(mjPage.getRecords()));
    }

    @Override
    public RoomMidjourneyMsgVO detail(Long msgId) {
        RoomMidjourneyMsgDO roomMidjourneyMsgDO = getOne(new LambdaQueryWrapper<RoomMidjourneyMsgDO>()
                .eq(RoomMidjourneyMsgDO::getUserId, FrontUserUtil.getUserId())
                .eq(RoomMidjourneyMsgDO::getId, msgId));
        return RoomMidjourneyMsgConverter.INSTANCE.entityToVO(roomMidjourneyMsgDO);
    }
    @Override
    public Boolean delete(Long msgId) {
        return removeById(msgId);
    }

    //暂时不用这个方法了
    @Override
    public void imagine(MjImagineRequest imagineRequest) {
        //判断余额是否充足
        RoomHandler.checkRemainPoints(haUserPermissionsService.isRemainPoints(RecordsEnum.getPointsByName(RecordsEnum.IMG_MJ)));

        // 检查是否可以操作
        MidjourneyRoomMsgHandler.checkCanOperate(imagineRequest.getRoomId());

        // 这两个 id 按先后顺序生成，保证在表里的顺序也是有先后的
        // 生成问题的消息 id
        long questionMessageId = IdWorker.getId();
        // 生成回答消息的 id
        long answerMessageId = IdWorker.getId();

        MidjourneyProperties midjourneyProperties = MidjourneyProperties.init();

        // 问题消息创建插入
        RoomMidjourneyMsgDO questionMessage = new RoomMidjourneyMsgDO();
        questionMessage.setId(questionMessageId);
        questionMessage.setRoomId(imagineRequest.getRoomId());
        questionMessage.setUserId(FrontUserUtil.getUserId());
        questionMessage.setType(MessageTypeEnum.QUESTION);
        questionMessage.setPrompt(imagineRequest.getPrompt());
        // 组装最终的 prompt
        questionMessage.setFinalPrompt("[".concat(String.valueOf(answerMessageId)).concat("] ").concat(questionMessage.getPrompt()));
        questionMessage.setAction(MjMsgActionEnum.IMAGINE.getAction());
        questionMessage.setStatus(MidjourneyMsgStatusEnum.SYS_SUCCESS);
        questionMessage.setDiscordChannelId(midjourneyProperties.getChannelId());
        questionMessage.setIsDeleted(false);
        save(questionMessage);

        // 初始化回答消息
        RoomMidjourneyMsgDO answerMessage = new RoomMidjourneyMsgDO();
        answerMessage.setId(answerMessageId);
        answerMessage.setRoomId(questionMessage.getRoomId());
        answerMessage.setPrompt(questionMessage.getPrompt());
        answerMessage.setFinalPrompt(questionMessage.getFinalPrompt());
        answerMessage.setAction(MjMsgActionEnum.IMAGINE.getAction());
        answerMessage.setUUseBit(0);

        // 校验敏感词
        boolean isCheckPromptPass = checkPromptContent(questionMessage, answerMessage);
        if (!isCheckPromptPass) {
            return;
        }

        // 创建回答消息
        createAnswerMessage(answerMessage, midjourneyProperties, () -> discordService.imagine(answerMessage.getFinalPrompt(), midjourneyProperties));
    }
    @Override
    public RoomMidjourneyMsgVO image(MjImagineRequest imagineRequest) {
        //判断余额是否充足
        RoomHandler.checkRemainPoints(haUserPermissionsService.isRemainPoints(RecordsEnum.getPointsByName(RecordsEnum.IMG_MJ)));

        // 检查是否可以操作
        MidjourneyRoomMsgHandler.checkCanOperate(imagineRequest.getRoomId());

        // 这两个 id 按先后顺序生成，保证在表里的顺序也是有先后的
        // 生成问题的消息 id
        long questionMessageId = IdWorker.getId();
        //// 生成回答消息的 id
        //long answerMessageId = IdWorker.getId();

        MidjourneyProperties midjourneyProperties = MidjourneyProperties.init();

        // 消息创建插入
        RoomMidjourneyMsgDO questionMessage = new RoomMidjourneyMsgDO();


        if(StringUtil.isNotBlank(imagineRequest.getBaseImg())) {
            questionMessage.setBaseImg(imagineRequest.getBaseImg());
            imagineRequest.setPrompt(imagineRequest.getPrompt().replaceAll(imagineRequest.getBaseImg(),""));
        }

        questionMessage.setId(questionMessageId);
        questionMessage.setRoomId(imagineRequest.getRoomId());
        questionMessage.setUserId(FrontUserUtil.getUserId());
        questionMessage.setType(MessageTypeEnum.ANSWER);  //不区分问题还是回答，统一一个任务记录即可  MessageTypeEnum.QUESTION
        questionMessage.setPrompt(imagineRequest.getPrompt());
        // 组装最终的 prompt
        questionMessage.setFinalPrompt("[".concat(String.valueOf(questionMessageId)).concat("] ").concat(questionMessage.getPrompt()));
        questionMessage.setAction(MjMsgActionEnum.IMAGINE.getAction());
        questionMessage.setStatus(MidjourneyMsgStatusEnum.SYS_WAIT); //MidjourneyMsgStatusEnum.SYS_SUCCESS
        questionMessage.setDiscordChannelId(midjourneyProperties.getChannelId());
        questionMessage.setIsDeleted(false);
        questionMessage.setUUseBit(0);
        questionMessage.setParams(imagineRequest.getParams());
        questionMessage.setMjType(imagineRequest.getMjType());
        //questionMessage.setProgressing(0);
        // 校验敏感词
        boolean isCheckPromptPass = checkPromptContent(questionMessage, questionMessage);
        if (!isCheckPromptPass) {
            return null;
        }
        questionMessage.setIsDeducted(PayStatusEnum.INIT.getCode());
        //校验通过保存任务信息
        save(questionMessage);

        RoomMidjourneyMsgVO mjVo = new RoomMidjourneyMsgVO();
        mjVo.setId(questionMessageId);
        mjVo.setCreateTime(questionMessage.getCreateTime());
        mjVo.setProgressing(0);
        mjVo.setStatus(MidjourneyMsgStatusEnum.SYS_WAIT);

        if(imagineRequest.getMjType() == CommonEnum.commonOne)
            CommonEnum.isWaitMj = true;  //启动工作线程，执行mj任务
        else if(imagineRequest.getMjType() == CommonEnum.commonTwo)
            CommonEnum.isWaitRelxMj = true;
        if(questionMessage.getMjType() == CommonEnum.commonTwo)
        startRecord("" + questionMessage.getId(),FrontUserUtil.getUserId(),RecordsEnum.IMG_MJ_RELAX.getName(),questionMessage);
        else
            startRecord("" + questionMessage.getId(),FrontUserUtil.getUserId(),RecordsEnum.IMG_MJ.getName(),questionMessage);
        return mjVo;

        //// 初始化回答消息
        //RoomMidjourneyMsgDO answerMessage = new RoomMidjourneyMsgDO();
        //answerMessage.setId(answerMessageId);
        //answerMessage.setRoomId(questionMessage.getRoomId());
        //answerMessage.setPrompt(questionMessage.getPrompt());
        //answerMessage.setFinalPrompt(questionMessage.getFinalPrompt());
        //answerMessage.setAction(MjMsgActionEnum.IMAGINE);
        //answerMessage.setUUseBit(0);



        // 创建回答消息
        //createAnswerMessage(answerMessage, midjourneyProperties, () -> discordService.imagine(answerMessage.getFinalPrompt(), midjourneyProperties));
    }
    //放大
    @Override
    public void upscale(MjConvertRequest convertRequest) {

        //判断余额是否充足
        RoomHandler.checkRemainPoints(haUserPermissionsService.isRemainPoints(RecordsEnum.getPointsByName(RecordsEnum.IMG_MJ_U)));
        // 检查是否可以操作
        MidjourneyRoomMsgHandler.checkCanOperate(convertRequest.getRoomId());

        // 获取原消息
        RoomMidjourneyMsgDO parentRoomMidjourneyMsgDO = getOne(new LambdaQueryWrapper<RoomMidjourneyMsgDO>().eq(RoomMidjourneyMsgDO::getId, convertRequest.getMsgId())
                .eq(RoomMidjourneyMsgDO::getRoomId, convertRequest.getRoomId())
                .eq(RoomMidjourneyMsgDO::getUserId, FrontUserUtil.getUserId()));
        // 检查是否可以 upscale
        MidjourneyProperties midjourneyProperties = MidjourneyProperties.init();
        MidjourneyRoomMsgHandler.checkCanUpscale(parentRoomMidjourneyMsgDO, convertRequest.getIndex(), midjourneyProperties);

        // 这两个 id 按先后顺序生成，保证在表里的顺序也是有先后的
        //// 生成问题的消息 id
        //long questionMessageId = IdWorker.getId();
        // 生成回答消息的 id
        long answerMessageId = IdWorker.getId();

        // 问题消息创建插入
        RoomMidjourneyMsgDO questionMessage = new RoomMidjourneyMsgDO();
        questionMessage.setId(answerMessageId);
        questionMessage.setRoomId(parentRoomMidjourneyMsgDO.getRoomId());
        questionMessage.setUserId(parentRoomMidjourneyMsgDO.getUserId());
        questionMessage.setType(MessageTypeEnum.ANSWER);
        questionMessage.setPrompt("放大图片："+parentRoomMidjourneyMsgDO.getId()+"--"+convertRequest.getIndex());//parentRoomMidjourneyMsgDO.getPrompt()
        questionMessage.setFinalPrompt(parentRoomMidjourneyMsgDO.getFinalPrompt());
        questionMessage.setUvParentId(Long.parseLong(parentRoomMidjourneyMsgDO.getDuckId()));//配合duck修改
        questionMessage.setUvIndex(convertRequest.getIndex());
        questionMessage.setDiscordMessageId(parentRoomMidjourneyMsgDO.getDiscordMessageId());
        questionMessage.setAction(MjMsgActionEnum.UPSCALE.getAction());
        questionMessage.setStatus(MidjourneyMsgStatusEnum.SYS_WAIT);
        questionMessage.setIsDeleted(false);
        questionMessage.setCreateTime(new Date());
        questionMessage.setParams(parentRoomMidjourneyMsgDO.getParams());
        questionMessage.setUUseBit(0);
        questionMessage.setMjType(convertRequest.getMjType());

        save(questionMessage);

        if(convertRequest.getMjType() == CommonEnum.commonOne)
            CommonEnum.isWaitMj = true;  //启动工作线程，执行mj任务
        else if(convertRequest.getMjType() == CommonEnum.commonTwo)
            CommonEnum.isWaitRelxMj = true;  //启动工作线程，执行mj任务
        System.out.println("-------------------------"+questionMessage.getId()+"-=-=-=-=-=-"+FrontUserUtil.getUserId()+"-=-=-=-=-="+convertRequest.getIndex());
        if(convertRequest.getMjType() == CommonEnum.commonTwo)
            startRecord("" + questionMessage.getId(),questionMessage.getUserId(),RecordsEnum.IMG_MJ_U_RELAX.getName(),questionMessage);
        else
            startRecord("" + questionMessage.getId(),questionMessage.getUserId(),RecordsEnum.IMG_MJ_U.getName(),questionMessage);
        // 初始化回答消息
        //RoomMidjourneyMsgDO answerMessage = new RoomMidjourneyMsgDO();
        //answerMessage.setId(answerMessageId);
        //answerMessage.setRoomId(questionMessage.getRoomId());
        //answerMessage.setPrompt(questionMessage.getPrompt());
        //answerMessage.setFinalPrompt(questionMessage.getFinalPrompt());
        //answerMessage.setAction(MjMsgActionEnum.UPSCALE);
        //answerMessage.setUvParentId(parentRoomMidjourneyMsgDO.getId());
        //// 这里先赋值，因为回调监听没有可以赋值的地方
        //answerMessage.setDiscordStartTime(new Date());

        // 创建回答消息
        //createAnswerMessage(answerMessage, midjourneyProperties,
        //        () -> discordService.upscale(parentRoomMidjourneyMsgDO.getDiscordMessageId(),
        //                answerMessage.getUvIndex(),
        //                MidjourneyRoomMsgHandler.getDiscordMessageHash(parentRoomMidjourneyMsgDO.getDiscordImageUrl()),
        //                midjourneyProperties));
    }

    //仿制
    @Override
    public void variation(MjConvertRequest convertRequest) {
        //判断余额是否充足
        RoomHandler.checkRemainPoints(haUserPermissionsService.isRemainPoints(RecordsEnum.getPointsByName(RecordsEnum.IMG_MJ_V)));

        // 检查是否可以操作
        MidjourneyRoomMsgHandler.checkCanOperate(convertRequest.getRoomId());

        // 获取原消息
        RoomMidjourneyMsgDO parentRoomMidjourneyMsgDO = getOne(new LambdaQueryWrapper<RoomMidjourneyMsgDO>().eq(RoomMidjourneyMsgDO::getId, convertRequest.getMsgId())
                .eq(RoomMidjourneyMsgDO::getRoomId, convertRequest.getRoomId())
                .eq(RoomMidjourneyMsgDO::getUserId, FrontUserUtil.getUserId()));
        // 检查是否可以 variation
        MidjourneyProperties midjourneyProperties = MidjourneyProperties.init();
        MidjourneyRoomMsgHandler.checkCanVariation(parentRoomMidjourneyMsgDO, midjourneyProperties);

        // 这两个 id 按先后顺序生成，保证在表里的顺序也是有先后的
        // 生成问题的消息 id
        //long questionMessageId = IdWorker.getId();
        // 生成回答消息的 id
        long answerMessageId = IdWorker.getId();

        // 问题消息创建插入
        RoomMidjourneyMsgDO questionMessage = new RoomMidjourneyMsgDO();
        questionMessage.setId(answerMessageId);
        questionMessage.setRoomId(parentRoomMidjourneyMsgDO.getRoomId());
        questionMessage.setUserId(parentRoomMidjourneyMsgDO.getUserId());
        questionMessage.setType(MessageTypeEnum.QUESTION);
        questionMessage.setPrompt("仿制图片："+parentRoomMidjourneyMsgDO.getId()+"--"+convertRequest.getIndex());//parentRoomMidjourneyMsgDO.getPrompt()
        questionMessage.setFinalPrompt(parentRoomMidjourneyMsgDO.getFinalPrompt());
        questionMessage.setUvParentId(Long.parseLong(parentRoomMidjourneyMsgDO.getDuckId()));//配合duck修改
        questionMessage.setUvIndex(convertRequest.getIndex());
        questionMessage.setDiscordMessageId(parentRoomMidjourneyMsgDO.getDiscordMessageId());
        questionMessage.setAction(MjMsgActionEnum.VARIATION.getAction());
        questionMessage.setStatus(MidjourneyMsgStatusEnum.SYS_WAIT);
        questionMessage.setParams(parentRoomMidjourneyMsgDO.getParams());
        questionMessage.setIsDeleted(false);
        questionMessage.setCreateTime(new Date());
        questionMessage.setUUseBit(0);
        questionMessage.setMjType(convertRequest.getMjType());

        save(questionMessage);
        if(convertRequest.getMjType() == CommonEnum.commonOne)
            CommonEnum.isWaitMj = true;  //启动工作线程，执行mj任务
        else if(convertRequest.getMjType() == CommonEnum.commonTwo)
            CommonEnum.isWaitRelxMj = true; //启动工作线程，执行mj任务
        if(convertRequest.getMjType() == CommonEnum.commonTwo)
            startRecord("" + questionMessage.getId(),questionMessage.getUserId(),RecordsEnum.IMG_MJ_V_RELAX.getName(),questionMessage);
        else
            startRecord("" + questionMessage.getId(),questionMessage.getUserId(),RecordsEnum.IMG_MJ_V.getName(),questionMessage);

        //// 初始化回答消息
        //RoomMidjourneyMsgDO answerMessage = new RoomMidjourneyMsgDO();
        //answerMessage.setId(answerMessageId);
        //answerMessage.setRoomId(questionMessage.getRoomId());
        //answerMessage.setPrompt(questionMessage.getPrompt());
        //answerMessage.setFinalPrompt(questionMessage.getFinalPrompt());
        //answerMessage.setAction(MjMsgActionEnum.VARIATION);
        //answerMessage.setUvParentId(parentRoomMidjourneyMsgDO.getId());
        //answerMessage.setUvIndex(convertRequest.getIndex());
        //answerMessage.setDiscordStartTime(new Date());

        //// 创建回答消息
        //createAnswerMessage(answerMessage, midjourneyProperties,
        //        () -> discordService.variation(parentRoomMidjourneyMsgDO.getDiscordMessageId(),
        //                answerMessage.getUvIndex(),
        //                MidjourneyRoomMsgHandler.getDiscordMessageHash(parentRoomMidjourneyMsgDO.getDiscordImageUrl()),
        //                midjourneyProperties));
    }
    //强弱变化，变焦，扩展
    @Override
    public void cpzClick(MjConvertRequest convertRequest){
        //判断余额是否充足
        RoomHandler.checkRemainPoints(haUserPermissionsService.isRemainPoints(RecordsEnum.getPointsByName(RecordsEnum.IMG_MJ_V)));

        // 检查是否可以操作
        MidjourneyRoomMsgHandler.checkCanOperate(convertRequest.getRoomId());

        // 获取原消息
        RoomMidjourneyMsgDO parentRoomMidjourneyMsgDO = getOne(new LambdaQueryWrapper<RoomMidjourneyMsgDO>().eq(RoomMidjourneyMsgDO::getId, convertRequest.getMsgId())
                .eq(RoomMidjourneyMsgDO::getRoomId, convertRequest.getRoomId())
                .eq(RoomMidjourneyMsgDO::getUserId, FrontUserUtil.getUserId()));
        // 检查是否可以 variation
        MidjourneyProperties midjourneyProperties = MidjourneyProperties.init();
            MidjourneyRoomMsgHandler.checkCanVariation(parentRoomMidjourneyMsgDO, midjourneyProperties);


        // 这两个 id 按先后顺序生成，保证在表里的顺序也是有先后的
        // 生成问题的消息 id
        //long questionMessageId = IdWorker.getId();
        // 生成回答消息的 id
        long answerMessageId = IdWorker.getId();

        // 问题消息创建插入
        RoomMidjourneyMsgDO questionMessage = new RoomMidjourneyMsgDO();
        questionMessage.setId(answerMessageId);
        questionMessage.setRoomId(parentRoomMidjourneyMsgDO.getRoomId());
        questionMessage.setUserId(parentRoomMidjourneyMsgDO.getUserId());
        questionMessage.setType(MessageTypeEnum.QUESTION);
        questionMessage.setPrompt(convertRequest.getActionName()+":"+parentRoomMidjourneyMsgDO.getId());//parentRoomMidjourneyMsgDO.getPrompt()
        questionMessage.setFinalPrompt(parentRoomMidjourneyMsgDO.getFinalPrompt());
        questionMessage.setUvParentId(Long.parseLong(parentRoomMidjourneyMsgDO.getDuckId()));//配合duck修改
        questionMessage.setUvIndex(convertRequest.getIndex());
        questionMessage.setDiscordMessageId(parentRoomMidjourneyMsgDO.getDiscordMessageId());
        questionMessage.setAction(getCustomId(convertRequest.getAction(),parentRoomMidjourneyMsgDO.getButtons()));
        questionMessage.setStatus(MidjourneyMsgStatusEnum.SYS_WAIT);
        questionMessage.setParams(parentRoomMidjourneyMsgDO.getParams());
        questionMessage.setIsDeleted(false);
        questionMessage.setCreateTime(new Date());
        questionMessage.setUUseBit(0);
        questionMessage.setMjType(convertRequest.getMjType());
        questionMessage.setFinalPrompt(convertRequest.getPrompt());
        try {
            questionMessage.setOriginalImageName(convertRequest.getMaskBase64());
        }catch (Exception e){

        }

        save(questionMessage);
        if(convertRequest.getMjType() == CommonEnum.commonOne)
            CommonEnum.isWaitMj = true;  //启动工作线程，执行mj任务
        else if(convertRequest.getMjType() == CommonEnum.commonTwo)
            CommonEnum.isWaitRelxMj = true; //启动工作线程，执行mj任务
        startRecord("" + questionMessage.getId(),questionMessage.getUserId(),RecordsEnum.IMG_MJ_V.getName(),questionMessage);

    }

    @Override
    public void blendMj(MjConvertRequest convertRequest){
        //判断余额是否充足
        RoomHandler.checkRemainPoints(haUserPermissionsService.isRemainPoints(RecordsEnum.getPointsByName(RecordsEnum.IMG_MJ_V)));

        // 检查是否可以操作
        MidjourneyRoomMsgHandler.checkCanOperate(convertRequest.getRoomId());

        // 检查是否可以 variation
            MidjourneyRoomMsgHandler.checkExistProcessingTask();
        long questionMessageId = IdWorker.getId();
        //// 生成回答消息的 id
        //long answerMessageId = IdWorker.getId();

        MidjourneyProperties midjourneyProperties = MidjourneyProperties.init();

        // 消息创建插入
        RoomMidjourneyMsgDO questionMessage = new RoomMidjourneyMsgDO();


        questionMessage.setId(questionMessageId);
        questionMessage.setRoomId(convertRequest.getRoomId());
        questionMessage.setUserId(FrontUserUtil.getUserId());
        questionMessage.setType(MessageTypeEnum.ANSWER);  //不区分问题还是回答，统一一个任务记录即可  MessageTypeEnum.QUESTION
        questionMessage.setPrompt(convertRequest.getActionName());
        // 组装最终的 prompt
        questionMessage.setAction(convertRequest.getAction());
        questionMessage.setStatus(MidjourneyMsgStatusEnum.SYS_WAIT); //MidjourneyMsgStatusEnum.SYS_SUCCESS
        questionMessage.setDiscordChannelId(midjourneyProperties.getChannelId());
        questionMessage.setIsDeleted(false);
        questionMessage.setUUseBit(0);
        questionMessage.setMjType(convertRequest.getMjType());
        questionMessage.setOriginalImageName(convertRequest.getMaskBase64());
        questionMessage.setFinalPrompt(convertRequest.getPrompt());
        //questionMessage.setProgressing(0);

        questionMessage.setIsDeducted(PayStatusEnum.INIT.getCode());
        //校验通过保存任务信息
        save(questionMessage);

        RoomMidjourneyMsgVO mjVo = new RoomMidjourneyMsgVO();
        mjVo.setId(questionMessageId);
        mjVo.setCreateTime(questionMessage.getCreateTime());
        mjVo.setProgressing(0);
        mjVo.setStatus(MidjourneyMsgStatusEnum.SYS_WAIT);

        if(convertRequest.getMjType() == CommonEnum.commonOne)
            CommonEnum.isWaitMj = true;  //启动工作线程，执行mj任务
        else if(convertRequest.getMjType() == CommonEnum.commonTwo)
            CommonEnum.isWaitRelxMj = true;
        if(MjMsgActionEnum.BLEND.getAction().equals(convertRequest.getAction()))
            startRecord("" + questionMessage.getId(),questionMessage.getUserId(),RecordsEnum.IMG_MJ_BLEND_RELAX.getName(),questionMessage);
        else
            startRecord("" + questionMessage.getId(),questionMessage.getUserId(),RecordsEnum.IMG_MJ_FACE.getName(),questionMessage);

    }




    private String getCustomId(String action,String buttons){
        JSONArray json = new JSONArray(buttons);
        Map<String, JSONObject> map = new HashMap<>();
        for (int i = 0; i < json.length(); i++) {
            JSONObject jsonObject = json.getJSONObject(i);
            String customId = jsonObject.getString("customId");
            if(customId.contains(action))
                return customId;
        }

        return null;
    }
    //局部重绘
    @Override
    public void modalImg(MjConvertRequest convertRequest){
        //判断余额是否充足
        RoomHandler.checkRemainPoints(haUserPermissionsService.isRemainPoints(RecordsEnum.getPointsByName(RecordsEnum.IMG_MJ_V)));

        // 检查是否可以操作
        MidjourneyRoomMsgHandler.checkCanOperate(convertRequest.getRoomId());

        // 获取原消息
        RoomMidjourneyMsgDO parentRoomMidjourneyMsgDO = getOne(new LambdaQueryWrapper<RoomMidjourneyMsgDO>().eq(RoomMidjourneyMsgDO::getId, convertRequest.getMsgId())
                .eq(RoomMidjourneyMsgDO::getRoomId, convertRequest.getRoomId())
                .eq(RoomMidjourneyMsgDO::getUserId, FrontUserUtil.getUserId()));
        // 检查是否可以 variation
        MidjourneyProperties midjourneyProperties = MidjourneyProperties.init();
        MidjourneyRoomMsgHandler.checkCanVariation(parentRoomMidjourneyMsgDO, midjourneyProperties);

        // 这两个 id 按先后顺序生成，保证在表里的顺序也是有先后的
        // 生成问题的消息 id
        //long questionMessageId = IdWorker.getId();
        // 生成回答消息的 id
        long answerMessageId = IdWorker.getId();

        // 问题消息创建插入
        RoomMidjourneyMsgDO questionMessage = new RoomMidjourneyMsgDO();
        questionMessage.setId(answerMessageId);
        questionMessage.setRoomId(parentRoomMidjourneyMsgDO.getRoomId());
        questionMessage.setUserId(parentRoomMidjourneyMsgDO.getUserId());
        questionMessage.setType(MessageTypeEnum.QUESTION);
        questionMessage.setPrompt(convertRequest.getPrompt() + "--局部重绘："+parentRoomMidjourneyMsgDO.getId());//parentRoomMidjourneyMsgDO.getPrompt()
        questionMessage.setFinalPrompt(parentRoomMidjourneyMsgDO.getFinalPrompt());
        questionMessage.setUvParentId(Long.parseLong(parentRoomMidjourneyMsgDO.getDuckId()));//配合duck修改
        questionMessage.setOriginalImageName(convertRequest.getMaskBase64());
        questionMessage.setDiscordMessageId(parentRoomMidjourneyMsgDO.getDiscordMessageId());
        questionMessage.setAction(MjMsgActionEnum.MODAL.getAction());
        questionMessage.setStatus(MidjourneyMsgStatusEnum.SYS_WAIT);
        questionMessage.setParams(parentRoomMidjourneyMsgDO.getParams());
        questionMessage.setIsDeleted(false);
        questionMessage.setCreateTime(new Date());
        questionMessage.setUUseBit(0);
        questionMessage.setMjType(convertRequest.getMjType());

        save(questionMessage);
        if(convertRequest.getMjType() == CommonEnum.commonOne)
            CommonEnum.isWaitMj = true;  //启动工作线程，执行mj任务
        else if(convertRequest.getMjType() == CommonEnum.commonTwo)
            CommonEnum.isWaitRelxMj = true; //启动工作线程，执行mj任务
        startRecord("" + questionMessage.getId(),questionMessage.getUserId(),RecordsEnum.IMG_MJ_V.getName(),questionMessage);
    }
    @Override
    public void describe(MjDescribeRequest describeRequest) {
        //判断余额是否充足
        RoomHandler.checkRemainPoints(haUserPermissionsService.isRemainPoints(RecordsEnum.getPointsByName(RecordsEnum.IMG_MJ_TOWEN)));
        // 检查房间是否存在
        RoomHandler.checkRoomExistAndCellCanUse(describeRequest.getRoomId(), CellCodeEnum.MIDJOURNEY);

        // 这两个 id 按先后顺序生成，保证在表里的顺序也是有先后的
        // 生成问题的消息 id
        long questionMessageId = IdWorker.getId();
        // 生成回答消息的 id
        long answerMessageId = IdWorker.getId();

        MultipartFile multipartFile = describeRequest.getFile();
        // 新原始文件名：前缀 + 消息 id + 后缀
        String newOriginalFileName = MidjourneyConstant.DESCRIBE_ORIGINAL_FILE_PREFIX + answerMessageId + StrPool.DOT + FileUtil.getFileExtension(multipartFile.getOriginalFilename());
        // 保存文件
        FileUtil.downloadFromMultipartFile(multipartFile, newOriginalFileName);

        MidjourneyProperties midjourneyProperties = MidjourneyProperties.init();

        // 判断文件大小
        if (multipartFile.getSize() > midjourneyProperties.getMaxFileSize()) {
            int maxMbFileSize = midjourneyProperties.getMaxFileSize() / 1024 / 1024;
            log.warn("Midjourney 业务异常，用户 id：{}，房间 id：{}，describe 文件大小为{}MB，超过{}MB限制", FrontUserUtil.getUserId(), describeRequest.getRoomId(), multipartFile.getSize(), maxMbFileSize);
            throw new ServiceException(StrUtil.format("文件大小超过{}MB限制", maxMbFileSize));
        }

        // 判断文件后缀是否符合
        if (!StrUtil.equalsAnyIgnoreCase(multipartFile.getContentType(), MidjourneyConstant.IMAGE_JPEG, MidjourneyConstant.IMAGE_PNG)) {
            log.warn("Midjourney 业务异常，用户 id：{}，房间 id：{}，describe 文件大小格式为 {} 错误", FrontUserUtil.getUserId(), describeRequest.getRoomId(), multipartFile.getContentType());
            throw new ServiceException("文件格式不符合要求，只能是 jpg 或 png 格式");
        }

        // TODO 图片审核

        // 上传图片
        Pair<Boolean, String> uploadResponsePair = discordService.uploadImage(newOriginalFileName, multipartFile, midjourneyProperties);
        if (!uploadResponsePair.getKey()) {
            throw new ServiceException(uploadResponsePair.getValue());
        }

        // 上面的操作应该没有并发限制，所以上面的上传图片操作就不考虑队列限制

        String discordUploadFileName = uploadResponsePair.getValue();
        // 检查是否有正在处理的任务
        MidjourneyRoomMsgHandler.checkExistProcessingTask();

        // 下载压缩图片
        String compressedImageFileName = MjRoomMessageUtil.downloadCompressedImage(newOriginalFileName, answerMessageId);

        // 问题消息创建插入
        RoomMidjourneyMsgDO questionMessage = new RoomMidjourneyMsgDO();
        questionMessage.setId(questionMessageId);
        questionMessage.setRoomId(describeRequest.getRoomId());
        questionMessage.setUserId(FrontUserUtil.getUserId());
        questionMessage.setType(MessageTypeEnum.QUESTION);
        questionMessage.setOriginalImageName(newOriginalFileName);
        questionMessage.setCompressedImageName(compressedImageFileName);
        questionMessage.setDiscordImageUrl(discordUploadFileName);
        questionMessage.setAction(MjMsgActionEnum.DESCRIBE.getAction());
        questionMessage.setStatus(MidjourneyMsgStatusEnum.SYS_SUCCESS);
        questionMessage.setDiscordChannelId(midjourneyProperties.getChannelId());
        questionMessage.setIsDeleted(false);
        save(questionMessage);

        // 初始化回答消息
        RoomMidjourneyMsgDO answerMessage = new RoomMidjourneyMsgDO();
        answerMessage.setId(answerMessageId);
        answerMessage.setRoomId(questionMessage.getRoomId());
        answerMessage.setOriginalImageName(newOriginalFileName);
        answerMessage.setCompressedImageName(compressedImageFileName);
        answerMessage.setDiscordImageUrl(discordUploadFileName);
        answerMessage.setAction(MjMsgActionEnum.DESCRIBE.getAction());
        answerMessage.setDiscordStartTime(new Date());
        answerMessage.setUUseBit(0);

        // 创建回答消息
        createAnswerMessage(answerMessage, midjourneyProperties, () -> discordService.describe(answerMessage.getDiscordImageUrl(), midjourneyProperties));
    }
    @Override
    public String upload(MultipartFile multipartFile,String type) {

        // 这两个 id 按先后顺序生成，保证在表里的顺序也是有先后的
        // 生成问题的消息 id
        long questionMessageId = IdWorker.getId();

        //MultipartFile multipartFile = describeRequest.getFile();
        // 新原始文件名：前缀 + 消息 id + 后缀
        String newOriginalFileName = MidjourneyConstant.UPLOAD_ORIGINAL_FILE_PREFIX + questionMessageId + StrPool.DOT + FileUtil.getFileExtension(multipartFile.getOriginalFilename());


        MidjourneyProperties midjourneyProperties = MidjourneyProperties.init();

        // 判断文件大小
        if (multipartFile.getSize() > midjourneyProperties.getMaxFileSize()) {
            int maxMbFileSize = midjourneyProperties.getMaxFileSize() / 1024 / 1024;
            log.warn("Midjourney 业务异常，用户 id：{}，房间 id：{}，describe 文件大小为{}MB，超过{}MB限制", FrontUserUtil.getUserId(),111, multipartFile.getSize(), maxMbFileSize);
            throw new ServiceException(StrUtil.format("文件大小超过{}MB限制", maxMbFileSize));
        }
        if(StringUtil.isNotBlank(type) && type.equals("file")){

        }else
        // 判断文件后缀是否符合
        if (StringUtil.isNotBlank(type) && type.equals("img") && !StrUtil.equalsAnyIgnoreCase(multipartFile.getContentType(), MidjourneyConstant.IMAGE_JPEG, MidjourneyConstant.IMAGE_PNG)) {
            log.warn("Midjourney 业务异常，用户 id：{}，房间 id：{}，describe 文件大小格式为 {} 错误", FrontUserUtil.getUserId(), 111, multipartFile.getContentType());
            throw new ServiceException("文件格式不符合要求，只能是 jpg 或 png 格式");
        }

        if(StringUtil.isNotBlank(type) && type.equals("file")){

            // 保存文件到OSS
            //FileUtil.downloadFromMultipartFile(multipartFile, "wmup" + File.separator + "file" + File.separator + newOriginalFileName);
            //String showUrl = FileUtil.getFilePathVisitPrefix().concat("wmup/file/" + newOriginalFileName);
            FileUtil.putFromMultipartFile(multipartFile,newOriginalFileName,multipartFile.getContentType());

            return CommonEnum.upfile +"/" + newOriginalFileName;
        }else {
            // 保存文件
            //FileUtil.downloadFromMultipartFile(multipartFile, "wmup" + File.separator + newOriginalFileName);
            //// 下载压缩图片
            //String compressedImageFileName = MjRoomMessageUtil.downloadCompressedImage("wmup" + File.separator + newOriginalFileName, questionMessageId);
            //String showUrl = FileUtil.getFilePathVisitPrefix().concat("wmup/" + newOriginalFileName);
            //// TODO 图片审核
            //if (BaiduAipUtil.getBaiduAipProperties().getEnabled()) {
            //    long currentTime = DateUtil.current();
            //    log.info("审核标识：{}，图片审核，开始审核时间：{}, 审核内容：{}", questionMessageId, currentTime, showUrl);
            //    Map<String, Object> resultMap = SpringUtil.getBean(AipContentCensor.class).imageCensorUserDefined(FileUtil.getFileSavePathPrefix().concat(compressedImageFileName), EImgType.FILE, null).toMap();
            //    log.info("审核标识：{}，图片审核，审核总时长：{} 毫秒，审核结果：{}", questionMessageId, DateUtil.spendMs(currentTime), resultMap);
            //    if (!resultMap.get("conclusion").equals("合规"))
            //        throw new ServiceException(StrUtil.format("上传的图片不合规，请重新上传"));
            //}

            //return FileUtil.getFilePathVisitPrefix().concat(compressedImageFileName.replace("\\","/"))+ "," + showUrl;


            FileUtil.putFromMultipartFile(multipartFile,newOriginalFileName,multipartFile.getContentType());
            if (BaiduAipUtil.getBaiduAipProperties().getEnabled()) {
                long currentTime = DateUtil.current();
                log.info("审核标识：{}，图片审核，开始审核时间：{}, 审核内容：{}", questionMessageId, currentTime, JdOSSUtil.getJdOSSAipProperties().getBucketUrl()+ CommonEnum.upfile + "/" + newOriginalFileName);
                Map<String, Object> resultMap = SpringUtil.getBean(AipContentCensor.class).imageCensorUserDefined(JdOSSUtil.getJdOSSAipProperties().getBucketUrl()+ CommonEnum.upfile + "/" + newOriginalFileName, EImgType.URL, null).toMap();
                log.info("审核标识：{}，图片审核，审核总时长：{} 毫秒，审核结果：{}", questionMessageId, DateUtil.spendMs(currentTime), resultMap);
                if (!resultMap.get("conclusion").equals("合规"))
                    throw new ServiceException(StrUtil.format("上传的图片不合规，请重新上传"));
            }
            return CommonEnum.upfile +"/" + newOriginalFileName+ "," + CommonEnum.upfile +"/" + newOriginalFileName;
        }

        //审核成功...

        //审核失败...


    }

    /**
     * 校验 prompt 内容是否合规
     *
     * @param questionMessage 问题消息
     * @param answerMessage   回答消息
     * @return 是否合规
     */
    private boolean checkPromptContent(RoomMidjourneyMsgDO questionMessage, RoomMidjourneyMsgDO answerMessage) {
        // 填充公共字段
        //answerMessage.setUserId(FrontUserUtil.getUserId());
        //answerMessage.setType(MessageTypeEnum.ANSWER);
        //answerMessage.setStatus(MidjourneyMsgStatusEnum.SYS_FAILURE);
        //answerMessage.setDiscordChannelId(null);
        //answerMessage.setIsDeleted(false);

        // 获取所有配置项
        Map<MidjourneyCellConfigCodeEnum, DataWrapper> cellConfigCodeMap = midjourneyCellConfigStrategy.getCellConfigMap();

        // 检查本地敏感词校验是否启用
        //boolean enabled = cellConfigCodeMap.get(MidjourneyCellConfigCodeEnum.ENABLED_LOCAL_SENSITIVE_WORD).asBoolean();
        //if (enabled) {
        //    List<String> sensitiveWords = SensitiveWordHandler.checkWord(questionMessage.getPrompt());
        //    if (CollectionUtil.isNotEmpty(sensitiveWords)) {
        //        //answerMessage.setResponseContent(StrUtil.format("发送失败，包含敏感词{}", sensitiveWords));
        //        //answerMessage.setFailureReason("本地敏感词库：".concat(answerMessage.getResponseContent()));
        //        //save(answerMessage);
        //        throw new ServiceException("提示词包含敏感信息：" + sensitiveWords + " ，请修改后重试");
        //        //return false;
        //    }
        //}

        // 检查百度敏感词校验是否启用
        boolean enabled = cellConfigCodeMap.get(MidjourneyCellConfigCodeEnum.ENABLED_BAIDU_AIP).asBoolean();
        if (enabled) {
            Pair<Boolean, String> checkTextPassResultPair = BaiduAipHandler.isCheckTextPass(String.valueOf(answerMessage.getId()), questionMessage.getPrompt());
            if (!checkTextPassResultPair.getKey()) {
                // 填充公共字段
                //answerMessage.setResponseContent("存在不合规内容，请修改内容");
                //answerMessage.setFailureReason(checkTextPassResultPair.getValue());
                //save(answerMessage);
                throw new ServiceException("提示词包含敏感信息：" + checkTextPassResultPair.getValue() + " ，请修改后重试");
                //return false;
            }
        }

        return true;
    }

    /**
     * 创建回答消息
     *
     * @param answerMessage        回答消息
     * @param midjourneyProperties Midjourney 配置
     * @param discordSendService   discord 发送服务接口
     */
    private void createAnswerMessage(RoomMidjourneyMsgDO answerMessage, MidjourneyProperties midjourneyProperties, DiscordSendService discordSendService) {
        // 填充公共字段
        answerMessage.setUserId(FrontUserUtil.getUserId());
        answerMessage.setType(MessageTypeEnum.ANSWER);
        answerMessage.setDiscordChannelId(midjourneyProperties.getChannelId());
        answerMessage.setIsDeleted(false);

        // 创建任务并返回回答的状态
        MidjourneyMsgStatusEnum answerStatus = midjourneyTaskQueueHandler.pushNewTask(answerMessage.getId(), midjourneyProperties);
        // 达到队列上限
        if (answerStatus == MidjourneyMsgStatusEnum.SYS_MAX_QUEUE) {
            answerMessage.setResponseContent(StrUtil.format("当前排队任务为 {} 条，已经达到上限，请稍后再试", midjourneyProperties.getMaxWaitQueueSize()));
        }
        answerMessage.setStatus(answerStatus);
        save(answerMessage);

        // 等待接收状态，此时可以调用 discord 接口
        if (answerStatus == MidjourneyMsgStatusEnum.MJ_WAIT_RECEIVED) {
            Pair<Boolean, String> resultPair = discordSendService.sendRequest();
            // 调用失败的情况，应该是少数情况，这里不重试
            if (!resultPair.getKey()) {
                answerMessage.setStatus(MidjourneyMsgStatusEnum.SYS_SEND_MJ_REQUEST_FAILURE);
                answerMessage.setResponseContent("系统异常，直接调用 discord 接口失败，请稍后再试");
                answerMessage.setFailureReason(resultPair.getValue());
                updateById(answerMessage);

                // 结束执行中任务
                midjourneyTaskQueueHandler.finishExecuteTask(answerMessage.getId());
            }
        }
    }


    @Override
    public List<RoomMidjourneyMsgDO> getComMjList() {
        // 构建查询条件，根据userid查询数据
        QueryWrapper<RoomMidjourneyMsgDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("user_id,id,action,user_id");

        queryWrapper.eq("is_deducted", PayStatusEnum.SUCCESS.getCode());//扣款成功的
        queryWrapper.gt("create_time", LocalDateTime.now().minus(1, ChronoUnit.HOURS));
        queryWrapper.eq("status", MidjourneyMsgStatusEnum.SYS_FAILURE.getCode());
        //
        // 根据条件查询一条数据
        return this.list(queryWrapper);
    }
//检查漏网之鱼
    @Override
    public List<RoomMidjourneyMsgDO> getNoPayMjList() {
        // 构建查询条件，根据userid查询数据
        QueryWrapper<RoomMidjourneyMsgDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("user_id,id,action,user_id");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.minus(20, ChronoUnit.MINUTES);
        LocalDateTime endTime = now.minus(5, ChronoUnit.MINUTES);


        queryWrapper.eq("is_deducted", PayStatusEnum.INIT.getCode());//未付款的
        queryWrapper.between("create_time", startTime, endTime);
        queryWrapper.eq("status", MidjourneyMsgStatusEnum.SYS_SUCCESS.getCode());
        //
        // 根据条件查询一条数据
        return this.list(queryWrapper);
    }

    @Override
    public List<RoomMidjourneyMsgDO> getMjList(int status,int mjType) {
        // 构建查询条件，根据userid查询数据
        QueryWrapper<RoomMidjourneyMsgDO> queryWrapper = new QueryWrapper<>();
        //queryWrapper.eq("status", MidjourneyMsgStatusEnum.SYS_WAIT.getCode());
        queryWrapper
                .eq("status", status)
                .eq("mj_type", mjType);
        //
        // 根据条件查询一条数据
        return this.list(queryWrapper);
    }
    @Override
    public List<RoomMidjourneyMsgDO> getRuntMjList(Integer mjType) {
        // 构建查询条件，根据userid查询数据
        QueryWrapper<RoomMidjourneyMsgDO> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq("status", MidjourneyMsgStatusEnum.MJ_IN_PROGRESS.getCode())
                .eq("mj_type", mjType);

        // 根据条件查询一条数据
        return this.list(queryWrapper);
    }
    @Override
    public List<RoomMidjourneyMsgDO> getTimeOutMjList() {
        // 构建查询条件，根据userid查询数据
        QueryWrapper<RoomMidjourneyMsgDO> queryWrapper = new QueryWrapper<>();
        //queryWrapper.eq("status", MidjourneyMsgStatusEnum.SYS_WAIT.getCode());
        queryWrapper.in("status", MidjourneyMsgStatusEnum.SYS_WAIT.getCode(), MidjourneyMsgStatusEnum.MJ_IN_PROGRESS.getCode());

        LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minus(10, ChronoUnit.MINUTES);
        queryWrapper.apply("TIMESTAMPDIFF(MINUTE, create_time, {0}) >= 10", thirtyMinutesAgo);
        //
        // 根据条件查询一条数据
        return this.list(queryWrapper);
    }

    @Override
    public boolean update(RoomMidjourneyMsgDO roomMidjourneyMsgDO) {
        return this.updateById(roomMidjourneyMsgDO);
    }

    //消费插入缓存

    private void startRecord(String taskId,int userId,String model, RoomMidjourneyMsgDO da){
        HaProductsDo hpd;
          if(model.equals(RecordsEnum.IMG_MJ_FACE.getName()) )
            hpd= RecordsEnum.getByName(RecordsEnum.IMG_MJ_FACE);
        else if(da.getMjType() == CommonEnum.commonOne)
            hpd= RecordsEnum.getByName(RecordsEnum.IMG_MJ);
        else
            hpd= RecordsEnum.getByName(RecordsEnum.IMG_MJ_RELAX);
        runMjRecords(taskId + "," + userId + "," + model,da,hpd);
    }
    private boolean runMjRecords(String tup, RoomMidjourneyMsgDO da, HaProductsDo hpd){
        String[] st = tup.split(",");//String taskId,int userId,string model

        HaUserPermissionsDo hsp = haUserPermissionsService.getOne(Integer.parseInt(st[1]));
        int record = hpd.getRecords();
        if(hsp.getIsVip() == CommonEnum.isVip && (new Date()).before(hsp.getValidyDate()))
            record = hpd.getVipRecords();
        if(addRecords(-record,hpd.getModel(),st[2],Long.parseLong(st[0]),Integer.parseInt(st[1]))){
            if(haUserPermissionsService.updatePoints(Integer.parseInt(st[1]),-record)) {
                da.setIsDeducted(PayStatusEnum.SUCCESS.getCode());//付费成功
                da.setId(Long.parseLong(st[0]));
                //da.setUserId(Integer.parseInt(st[1]));
                this.update(da);
                System.out.println( midjourneyTaskQueueHandler.removeRecord(tup));
            }
        }
        return true;
    }

    @Resource
    private HaExpenseRecordsService haExpenseRecordsService;

    private boolean addRecords(int points,String model,String name,long modelId,int userId){
        HaExpenseRecordsDo herd = new HaExpenseRecordsDo();
        herd.setUserId(userId);
        herd.setModel(model);
        herd.setMark(name);
        herd.setHoldBi(points);
        herd.setModelId(modelId);

        return haExpenseRecordsService.save(herd);
    }
}
