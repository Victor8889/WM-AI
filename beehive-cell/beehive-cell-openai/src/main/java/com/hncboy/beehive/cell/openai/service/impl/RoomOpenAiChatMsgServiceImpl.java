package com.hncboy.beehive.cell.openai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.hncboy.beehive.base.domain.entity.RoomDO;
import com.hncboy.beehive.base.domain.entity.RoomOpenAiChatMsgDO;
import com.hncboy.beehive.base.domain.query.RoomMsgCursorQuery;
import com.hncboy.beehive.base.enums.*;
import com.hncboy.beehive.base.handler.mp.BeehiveServiceImpl;
import com.hncboy.beehive.base.mapper.RoomOpenAiChatMsgMapper;
import com.hncboy.beehive.base.util.FrontUserUtil;
import com.hncboy.beehive.base.util.ObjectMapperUtil;
import com.hncboy.beehive.cell.core.hander.RoomHandler;
import com.hncboy.beehive.cell.core.hander.strategy.CellConfigFactory;
import com.hncboy.beehive.cell.openai.domain.request.RoomOpenAiChatSendRequest;
import com.hncboy.beehive.cell.openai.domain.vo.RoomOpenAiChatMsgVO;
import com.hncboy.beehive.cell.openai.module.chat.emitter.RoomOpenAiChatResponseEmitter;
import com.hncboy.beehive.cell.openai.module.chat.emitter.RoomOpenAiChatResponseEmitterDispatcher;
import com.hncboy.beehive.cell.openai.service.RoomOpenAiChatMsgService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author ll·
 * @date 2023/5/31
 * OpenAi 对话房间消息业务实现类
 */
@Slf4j
@Service
public class RoomOpenAiChatMsgServiceImpl extends BeehiveServiceImpl<RoomOpenAiChatMsgMapper, RoomOpenAiChatMsgDO> implements RoomOpenAiChatMsgService {

    @Resource
    private CellConfigFactory cellConfigFactory;

    @Resource
    private com.hncboy.beehive.web.service.HaUserPermissionsService haUserPermissionsService;

    @Override
    public List<RoomOpenAiChatMsgVO> list(RoomMsgCursorQuery cursorQuery) {
        List<RoomOpenAiChatMsgDO> cursorList = cursorList(cursorQuery, RoomOpenAiChatMsgDO::getId, new LambdaQueryWrapper<RoomOpenAiChatMsgDO>()
                .eq(RoomOpenAiChatMsgDO::getUserId, FrontUserUtil.getUserId())
                .eq(RoomOpenAiChatMsgDO::getRoomId, cursorQuery.getRoomId())); // 添加 status != -1 条件;
        //return RoomOpenAiChatMsgConverter.INSTANCE.entityToVO(cursorList);
        return entityToListVO(cursorList);
    }
    private List<RoomOpenAiChatMsgVO> entityToListVO(List<RoomOpenAiChatMsgDO> recordsDos) {
        if ( recordsDos == null ) {
            return null;
        }

        List<RoomOpenAiChatMsgVO> list = new ArrayList<RoomOpenAiChatMsgVO>( recordsDos.size() );
        for ( RoomOpenAiChatMsgDO records : recordsDos ) {
            list.add( entityToListVO( records ) );
        }

        return list;
    }
    private RoomOpenAiChatMsgVO entityToListVO(RoomOpenAiChatMsgDO recordsDos) {
        if ( recordsDos == null ) {
            return null;
        }
        RoomOpenAiChatMsgVO recodrsistVO = new RoomOpenAiChatMsgVO();
        BeanUtils.copyProperties(recordsDos, recodrsistVO);
        if( "answer".equalsIgnoreCase(recordsDos.getMessageType().name()) )
            recodrsistVO.setModelName(CommonEnum.NAME_MAP_PRODUCT.get(recordsDos.getModelName()).getShowName() );//RecordsEnum.fromApiModel(recordsDos.getModelName()).getName().replace("CHAT","万码AI对话")
        else
            recodrsistVO.setModelName("");
        recodrsistVO.setInversion( "answer".equalsIgnoreCase(recordsDos.getMessageType().name()) );
        return recodrsistVO;
    }
    private SFunction<RoomOpenAiChatMsgDO, RoomOpenAiChatMsgStatusEnum> getStatusColumn() {
        return RoomOpenAiChatMsgDO::getStatus;
    }
    @Override
    public ResponseBodyEmitter send(RoomOpenAiChatSendRequest sendRequest) {
        // 超时时间设置 2 分钟
        ResponseBodyEmitter emitter = new ResponseBodyEmitter(20 * 60 * 1000L);
        emitter.onCompletion(() -> log.debug("OpenAi Chat 请求参数：{}，Front-end closed the emitter connection.", ObjectMapperUtil.toJson(sendRequest)));
        emitter.onTimeout(() -> log.error("OpenAi Chat 请求参数：{}，Back-end closed the emitter connection.", ObjectMapperUtil.toJson(sendRequest)));

        boolean isRemainPoints = false;
        int remainPoints = haUserPermissionsService.getRemainPoints();
        if(sendRequest.getRoomId() == ChatGptRoomIdEnum.MJ_ROOM_ID.getCode() ||
                sendRequest.getRoomId() == ChatGptRoomIdEnum.DALLE_ROOM_ID.getCode()){//绘画:888MJ,777openimg,666sd

            String promptStart = "帮我把下面的内容翻译成英文，要求：只返回翻译后的结果，不要用引号引起来：";
            sendRequest.setText(promptStart + sendRequest.getText());
            // 转换为对应的响应处理器
            //RoomOpenAiChatResponseEmitter responseEmitter = RoomOpenAiChatResponseEmitterDispatcher.doDispatch(CellCodeEnum.OPENAI_CHAT_API_3_5);
            //responseEmitter.requestToResponseEmitter(sendRequest, emitter, cellConfigFactory.getCellConfigStrategy(CellCodeEnum.OPENAI_CHAT_API_3_5));
            //
            //判断余额是否充足
            //RoomHandler.checkRemainPoints(haUserPermissionsService.isRemainPoints(RecordsEnum.getPointsByName(RecordsEnum.IMG_MJ_TOWEN.getName())));
            if(sendRequest.getRoomId() == ChatGptRoomIdEnum.MJ_ROOM_ID.getCode() && remainPoints >= RecordsEnum.getPointsByName(RecordsEnum.IMG_MJ))
                isRemainPoints = true;
            else if(sendRequest.getRoomId() == ChatGptRoomIdEnum.DALLE_ROOM_ID.getCode() && remainPoints >= RecordsEnum.getPointsByName(RecordsEnum.IMG_DALLE))
                isRemainPoints = true;
            else if(sendRequest.getRoomId() == ChatGptRoomIdEnum.SD_ROOM_ID.getCode() && remainPoints >= RecordsEnum.getPointsByName(RecordsEnum.IMG_SD) )
                isRemainPoints = true;
            //判断余额是否充足
            RoomHandler.checkRemainPoints(isRemainPoints);
        }else if(ChatGptRoomIdEnum.SWD_ROOM_ID.getCode() == sendRequest.getRoomId()){//思维导图
            if(remainPoints >= RecordsEnum.SIWEIDAOTU.getRecords())
                isRemainPoints = true;
            //判断余额是否充足
            // isRemainPoints = false;
            RoomHandler.checkRemainPoints(isRemainPoints);
            //String promptStart = "帮我回答下面的问题（或分析下面的内容），要求：只返markdown格式的结果（不要返回其他多余的或描述或解释性或总结或感慨性的文字）,使用#号来表示标题的级别,我要将你返回的结果生成思维导图：";
            String promptStart = "请根据后面的内容创建markdown格式的结果,要求只返markdown格式的结果并且结果要丰富（不要返回其他多余的或描述或解释性或总结或感慨性的文字,不要返回```markdown字符，不要把内容放到引号里面）:";
            sendRequest.setText(promptStart + sendRequest.getText());
            // 转换为对应的响应处理器
            //RoomOpenAiChatResponseEmitter responseEmitter = RoomOpenAiChatResponseEmitterDispatcher.doDispatch(CellCodeEnum.OPENAI_CHAT_API_3_5);
            //responseEmitter.requestToResponseEmitter(sendRequest, emitter, cellConfigFactory.getCellConfigStrategy(CellCodeEnum.OPENAI_CHAT_API_3_5));

        }
            // 获取房间信息
            Map<CellCodeEnum, RoomOpenAiChatResponseEmitter> responseEmitterMap = RoomOpenAiChatResponseEmitterDispatcher.RESPONSE_EMITTER_MAP;
            RoomDO roomDO = RoomHandler.checkRoomExistAndCellCanUse(sendRequest.getRoomId(), new ArrayList<>(responseEmitterMap.keySet()));

            // 转换为对应的响应处理器
            RoomOpenAiChatResponseEmitter responseEmitter = RoomOpenAiChatResponseEmitterDispatcher.doDispatch(roomDO.getCellCode());
            responseEmitter.requestToResponseEmitter(sendRequest, emitter, cellConfigFactory.getCellConfigStrategy(roomDO.getCellCode()));



        return emitter;
    }
    @Override
    public Boolean delete(String id){
        RoomOpenAiChatMsgDO newChat = new RoomOpenAiChatMsgDO();
        newChat.setId(Long.parseLong(id));
        ////newChat.setUserId(FrontUserUtil.getUserId());
        //newChat.setStatus(RoomOpenAiChatMsgStatusEnum.DELETE_CHAT);//逻辑删除
        //UpdateWrapper<RoomOpenAiChatMsgDO> updateWrapper = new UpdateWrapper<>();
        //updateWrapper.eq("id", id);
        //updateWrapper.eq("user_id", FrontUserUtil.getUserId());
        //return this.update(newChat,updateWrapper);
        //逻辑删除改为真删
        return this.removeById(newChat);
    }

    public boolean update(RoomOpenAiChatMsgDO roomOpenAiChatMsgDO){
        return this.updateById(roomOpenAiChatMsgDO);
    }

    public List<RoomOpenAiChatMsgDO> getComChatList(){
        // 构建查询条件，根据userid查询数据
        QueryWrapper<RoomOpenAiChatMsgDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("user_id,id,room_id,model_name");

        queryWrapper.eq("is_deducted", PayStatusEnum.INIT.getCode());
        queryWrapper.gt("create_time", LocalDateTime.now().minus(20, ChronoUnit.MINUTES));
        queryWrapper.eq("message_type", MessageTypeEnum.ANSWER);
        queryWrapper.eq("status", RoomOpenAiChatMsgStatusEnum.COMPLETE_SUCCESS.getCode());
        queryWrapper.ne("room_id", ChatGptRoomIdEnum.DALLE_ROOM_ID)
                .ne("room_id",ChatGptRoomIdEnum.MJ_ROOM_ID)
                .ne("room_id",ChatGptRoomIdEnum.SD_ROOM_ID);

        // 根据条件查询一条数据
        return this.list(queryWrapper);
    }
}
