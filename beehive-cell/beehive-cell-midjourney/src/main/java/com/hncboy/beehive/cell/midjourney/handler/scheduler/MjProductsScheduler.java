//package com.hncboy.beehive.cell.midjourney.handler.scheduler;
//
//import com.hncboy.beehive.base.domain.entity.HaExpenseRecordsDo;
//import com.hncboy.beehive.base.domain.entity.HaProductsDo;
//import com.hncboy.beehive.base.domain.entity.HaUserPermissionsDo;
//import com.hncboy.beehive.base.domain.entity.RoomMidjourneyMsgDO;
//import com.hncboy.beehive.base.enums.CommonEnum;
//import com.hncboy.beehive.base.enums.PayStatusEnum;
//import com.hncboy.beehive.base.enums.RecordsEnum;
//import com.hncboy.beehive.cell.midjourney.handler.MidjourneyTaskQueueHandler;
//import com.hncboy.beehive.cell.midjourney.service.RoomMidjourneyMsgService;
//import com.hncboy.beehive.web.service.HaExpenseRecordsService;
//import com.hncboy.beehive.web.service.HaUserPermissionsService;
//import jakarta.annotation.Resource;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Date;
//import java.util.Iterator;
//import java.util.Map;
//
///**
// * @author ll
// * @date 2023/7/1
// * mj消费记录
// */
//@Slf4j
//@Component
//public class MjProductsScheduler implements Runnable {
//    @Resource
//    private HaUserPermissionsService haUserPermissionsService;
//
//    @Resource
//    private RoomMidjourneyMsgService midjourneyMsgService;
//
//    @Resource
//    private HaExpenseRecordsService haExpenseRecordsService;
//
//    @Resource
//    private MidjourneyTaskQueueHandler midjourneyTaskQueueHandler;
//
//
//
//    @Override
//    public void run() {//ApplicationArguments args
//        if(!CommonEnum.isMainRun)
//            return;
//        log.info("3、 MjProductsScheduler MJ thread start.");
//            while(true) {
//                try {
//                    mjRedisRecode();
//                    // 安排每5秒钟执行一次deductedMJ()方法
//                } catch (Exception e) {
//                    log.error("MjProductsScheduler更新MJ失败。" + e.getMessage() );
//                }finally {
//                    try {
//                        Thread.sleep(2*1000);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//    }
//    @Transactional(rollbackFor = Exception.class)
//    public void mjRedisRecode(){
//
//        HaProductsDo hpd = RecordsEnum.getByName(RecordsEnum.IMG_MJ);
//        RoomMidjourneyMsgDO da = new RoomMidjourneyMsgDO();
//    //从redis获取list
//        Iterator<Map.Entry<String, String>> iterator = CommonEnum.cacheMap.entrySet().iterator();
//        while (iterator.hasNext()) {
//            Map.Entry<String, String> entry = iterator.next();
//            String key = entry.getKey();
//            String value = entry.getValue();
//            if(runMjRecords(value,da,hpd))
//                iterator.remove();
//            // 处理每个键值对
//        }
//    }
//    private boolean runMjRecords(String tup,RoomMidjourneyMsgDO da,HaProductsDo hpd){
//        String[] st = tup.split(",");//String taskId,int userId,string model
//
//        HaUserPermissionsDo hsp = haUserPermissionsService.getOne(Integer.parseInt(st[1]));
//        int record = hpd.getRecords();
//        if(hsp.getIsVip() == CommonEnum.isVip && (new Date()).before(hsp.getValidyDate()))
//            record = hpd.getVipRecords();
//        if(addRecords(-record,hpd.getModel(),st[2],Long.parseLong(st[0]),Integer.parseInt(st[1]))){
//            if(haUserPermissionsService.updatePoints(Integer.parseInt(st[1]),-record)) {
//                da.setIsDeducted(PayStatusEnum.SUCCESS.getCode());//付费成功
//                da.setId(Long.parseLong(st[0]));
//                //da.setUserId(Integer.parseInt(st[1]));
//                midjourneyMsgService.update(da);
//                System.out.println( midjourneyTaskQueueHandler.removeRecord(tup));
//            }
//        }
//        return true;
//    }
//
//
//
//    private boolean addRecords(int points,String model,String name,long modelId,int userId){
//        HaExpenseRecordsDo herd = new HaExpenseRecordsDo();
//        herd.setUserId(userId);
//        herd.setModel(model);
//        herd.setMark(name);
//        herd.setHoldBi(points);
//        herd.setModelId(modelId);
//
//        return haExpenseRecordsService.save(herd);
//    }
//
//
//
//
//
//
//
//}
