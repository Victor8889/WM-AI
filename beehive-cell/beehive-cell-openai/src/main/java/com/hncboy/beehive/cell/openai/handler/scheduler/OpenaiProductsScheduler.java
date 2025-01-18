package com.hncboy.beehive.cell.openai.handler.scheduler;

import com.hncboy.beehive.base.domain.entity.*;
import com.hncboy.beehive.base.enums.ChatGptRoomIdEnum;
import com.hncboy.beehive.base.enums.CommonEnum;
import com.hncboy.beehive.base.enums.RecordsEnum;
import com.hncboy.beehive.cell.openai.enums.OpenAiChatApiModelEnum;
import com.hncboy.beehive.cell.openai.service.RoomOpenAiChatMsgService;
import com.hncboy.beehive.cell.openai.service.RoomOpenAiImageMsgService;
import com.hncboy.beehive.web.service.HaExpenseRecordsService;
import com.hncboy.beehive.web.service.HaUserPermissionsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @author ll
 * @date 2023/7/1
 * OpenAi 消费记录
 *  chat、swd、dall消费记录计算
 *  sd
 *  gemini
 *  起辅助作用，主要的扣费都在任务结束的时候扣了
 */
@Slf4j
@Component
public class OpenaiProductsScheduler{

    @Resource
    private HaUserPermissionsService haUserPermissionsService;

    @Resource
    private RoomOpenAiImageMsgService openAiImageMsgService;

    @Resource
    private RoomOpenAiChatMsgService roomOpenAiChatMsgService;

    @Resource
    private HaExpenseRecordsService haExpenseRecordsService;

    @Scheduled(cron = "0/50 * * * * ?")
    public void handler() {
        if(!CommonEnum.isBakRun)
            return;
        log.info(" Openai chat、swd、dall thread start.");
            try {
                deductedChatAndSwd();
                deductedDalle();

            } catch (Exception e) {
                e.printStackTrace();
            }

            // 不频繁请求
    }


    @Transactional(rollbackFor = Exception.class)
    public void deductedDalle(){
        List<RoomOpenAiImageMsgDO> dalleList = openAiImageMsgService.getComQrList();
        HaProductsDo hpd = null;
        for(RoomOpenAiImageMsgDO da : dalleList){
            hpd = CommonEnum.NAME_MAP_PRODUCT.get(da.getModel()) ;//RecordsEnum.getByName(RecordsEnum.fromApiModel(da.getModel()));

            HaUserPermissionsDo hsp = haUserPermissionsService.getOne(da.getUserId());
            int record = hpd.getRecords();
            if(hsp.getIsVip() == CommonEnum.isVip && (new Date()).before(hsp.getValidyDate()))
                record = hpd.getVipRecords();

            if(addRecords(-record,hpd.getModel(),hpd.getName(),da.getId(),da.getUserId())){
                if(haUserPermissionsService.updatePoints(da.getUserId(),-record)) {//hpd.getRecords()
                    da.setIsDeducted(1);
                    openAiImageMsgService.update(da);
                }
            }
        }
    }
    @Transactional(rollbackFor = Exception.class)
    public void deductedChatAndSwd(){
        List<RoomOpenAiChatMsgDO> chatList = roomOpenAiChatMsgService.getComChatList();
        HaProductsDo hpd = null;
        for(RoomOpenAiChatMsgDO da : chatList){

                hpd = CommonEnum.NAME_MAP_PRODUCT.get(da.getModelName());
                //RecordsEnum.getByName(RecordsEnum.fromApiModel(da.getModelName()));


            if(da.getRoomId() == ChatGptRoomIdEnum.SWD_ROOM_ID.getCode())
                hpd = RecordsEnum.getByName(RecordsEnum.SIWEIDAOTU);

            HaUserPermissionsDo hsp = haUserPermissionsService.getOne(da.getUserId());
            int record = hpd.getRecords();
            if(hsp.getIsVip() == CommonEnum.isVip && (new Date()).before(hsp.getValidyDate())) {
                record = hpd.getVipRecords();
                if(OpenAiChatApiModelEnum.GPT_3_5_TURBO_1106.getName().equals(da.getModelName()) || OpenAiChatApiModelEnum.GPT_3_5_TURBO.getName().equals(da.getModelName())){
                    if(hsp.getRemainHoldCount() >= record){
                        if(addFreeRecords(-record,hpd.getModel(),hpd.getName(),da.getId(),da.getUserId())){
                            if(haUserPermissionsService.updateFreePointCount(da.getUserId(),-record)) {
                                da.setIsDeducted(1);
                                roomOpenAiChatMsgService.update(da);
                                return;
                            }
                        }
                    }
                }

            }
            if(addRecords(-record,hpd.getModel(),hpd.getName(),da.getId(),da.getUserId())){
                if(haUserPermissionsService.updatePoints(da.getUserId(),-record)) {
                    da.setIsDeducted(1);
                    roomOpenAiChatMsgService.update(da);
                }
            }
        }
    }

    private boolean addFreeRecords(int points,String model,String name,long modelId,int userId){
        HaExpenseRecordsDo herd = new HaExpenseRecordsDo();
        herd.setUserId(userId);
        herd.setModel(model);
        herd.setMark(name+"免费积分");
        herd.setHoldBi(points);
        herd.setModelId(modelId);

        return haExpenseRecordsService.save(herd);
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


}
