//package com.hncboy.beehive.cell.midjourney.handler.scheduler;
//
//import cn.hutool.core.lang.Pair;
//import cn.hutool.core.util.StrUtil;
//import com.hncboy.beehive.base.domain.entity.HaProductsDo;
//import com.hncboy.beehive.base.domain.entity.RoomMidjourneyMsgDO;
//import com.hncboy.beehive.base.enums.CommonEnum;
//import com.hncboy.beehive.base.enums.MessageTypeEnum;
//import com.hncboy.beehive.base.enums.MidjourneyMsgStatusEnum;
//import com.hncboy.beehive.base.enums.RecordsEnum;
//import com.hncboy.beehive.base.util.FrontUserUtil;
//import com.hncboy.beehive.cell.midjourney.handler.MidjourneyTaskQueueHandler;
//import com.hncboy.beehive.cell.midjourney.handler.cell.MidjourneyProperties;
//import com.hncboy.beehive.cell.midjourney.service.DiscordSendService;
//import com.hncboy.beehive.cell.midjourney.service.DiscordService;
//import com.hncboy.beehive.cell.midjourney.service.RoomMidjourneyMsgService;
//import jakarta.annotation.Resource;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
///**
// * @author ll
// * @date 2023/7/1
// * 执行MJ任务
// */
//@Slf4j
//@Component
//public class RunMjScheduler implements ApplicationRunner {
//
//    @Resource
//    private RoomMidjourneyMsgService midjourneyMsgService;
//
//    @Resource
//    private DiscordService discordService;
//
//    @Resource
//    private MidjourneyTaskQueueHandler midjourneyTaskQueueHandler;
//
//
//
//    @Override
//    public void run(ApplicationArguments args) {
//        //log.info("  run MjScheduler MJ thread start.");
//        //    while(true) {
//        //        try {
//        //            //if(CommonEnum.isRunMj)
//        //            //    getWaitMj();
//        //            // 安排每5秒钟执行一次deductedMJ()方法
//        //        } catch (Exception e) {
//        //            log.error("更新MJ失败。" + e.getMessage());
//        //        }finally {
//        //            try {
//        //                Thread.sleep(1*1000);
//        //            } catch (InterruptedException e) {
//        //                e.printStackTrace();
//        //            }
//        //        }
//        //    }
//        System.out.println("wamyc");
//    }
//
//    @Transactional
//    public void getWaitMj(){
//        List<RoomMidjourneyMsgDO> mjList = midjourneyMsgService.getWaitMjList();
//        if(mjList.size() == 0) {
//            CommonEnum.isRunMj = false; //获取不到等待的mj任务，暂停查询数据库
//            return;
//        }
//        HaProductsDo hpd = RecordsEnum.getByName(RecordsEnum.IMG_MJ);
//        MidjourneyProperties midjourneyProperties = MidjourneyProperties.init();
//        for(RoomMidjourneyMsgDO da : mjList){
//
//            createAnswerMessage(da, midjourneyProperties, () -> discordService.imagine(da.getFinalPrompt(), midjourneyProperties));
//
//        }
//    }
//
//    /**
//     * 创建回答消息
//     *
//     * @param answerMessage        回答消息
//     * @param midjourneyProperties Midjourney 配置
//     * @param discordSendService   discord 发送服务接口
//     */
//    private void createAnswerMessage(RoomMidjourneyMsgDO answerMessage, MidjourneyProperties midjourneyProperties, DiscordSendService discordSendService) {
//        // 填充公共字段
//        answerMessage.setUserId(FrontUserUtil.getUserId());
//        answerMessage.setType(MessageTypeEnum.ANSWER);
//        answerMessage.setDiscordChannelId(midjourneyProperties.getChannelId());
//        answerMessage.setIsDeleted(false);
//
//        // 创建任务并返回回答的状态
//        MidjourneyMsgStatusEnum answerStatus = midjourneyTaskQueueHandler.pushNewTask(answerMessage.getId(), midjourneyProperties);
//        // 达到队列上限
//        if (answerStatus == MidjourneyMsgStatusEnum.SYS_MAX_QUEUE) {
//            answerMessage.setResponseContent(StrUtil.format("当前排队任务为 {} 条，已经达到上限，请稍后再试", midjourneyProperties.getMaxWaitQueueSize()));
//        }
//        answerMessage.setStatus(answerStatus);
//        midjourneyMsgService.save(answerMessage);
//
//        // 等待接收状态，此时可以调用 discord 接口
//        if (answerStatus == MidjourneyMsgStatusEnum.MJ_WAIT_RECEIVED) {
//            Pair<Boolean, String> resultPair = discordSendService.sendRequest();
//            // 调用失败的情况，应该是少数情况，这里不重试
//            if (!resultPair.getKey()) {
//                answerMessage.setStatus(MidjourneyMsgStatusEnum.SYS_SEND_MJ_REQUEST_FAILURE);
//                answerMessage.setResponseContent("系统异常，直接调用 discord 接口失败，请稍后再试");
//                answerMessage.setFailureReason(resultPair.getValue());
//                midjourneyMsgService.updateById(answerMessage);
//
//                // 结束执行中任务
//                midjourneyTaskQueueHandler.finishExecuteTask(answerMessage.getId());
//            }
//        }
//    }
//
//
//}
