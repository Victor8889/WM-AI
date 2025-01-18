package com.hncboy.beehive.cell.midjourney.handler.scheduler;

import com.hncboy.beehive.base.domain.entity.HaExpenseRecordsDo;
import com.hncboy.beehive.base.domain.entity.HaProductsDo;
import com.hncboy.beehive.base.domain.entity.RoomMidjourneyMsgDO;
import com.hncboy.beehive.base.enums.CommonEnum;
import com.hncboy.beehive.base.enums.PayStatusEnum;
import com.hncboy.beehive.base.enums.RecordsEnum;
import com.hncboy.beehive.cell.midjourney.service.RoomMidjourneyMsgService;
import com.hncboy.beehive.web.service.HaExpenseRecordsService;
import com.hncboy.beehive.web.service.HaUserPermissionsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author ll
 * @date 2023/7/1
 * mj消费退还记录
 * mj搜寻漏网之鱼
 */
@Slf4j
@Component
public class MjBackRecordScheduler implements Runnable {

    @Resource
    private HaUserPermissionsService haUserPermissionsService;

    @Resource
    private RoomMidjourneyMsgService midjourneyMsgService;

    @Resource
    private HaExpenseRecordsService haExpenseRecordsService;




    @Override
    public void run() {//ApplicationArguments args
        if(!CommonEnum.isBakRun)
            return;
        log.info("3、 MjProductsScheduler MJ thread start.");

                try {
                    ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

                    // 安排每20秒钟执行一次deductedQr()方法
                    executor.scheduleAtFixedRate(this::mjChargeBack, 0, 20, TimeUnit.SECONDS);
                    // 安排每1分钟执行一次updateProduct()方法
                    executor.scheduleAtFixedRate(this::deductedMj, 0, 1, TimeUnit.MINUTES);

                } catch (Exception e) {
                    log.error("更新MJ失败。" + e.getMessage());
                }finally {
                    try {
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
    }
    //搜寻未付款的漏网之鱼
    @Transactional(rollbackFor = Exception.class)
    public void deductedMj(){
        List<RoomMidjourneyMsgDO> mjList = midjourneyMsgService.getNoPayMjList();//此方法已经修改，
        //修改获取product方法
        HaProductsDo hpd = RecordsEnum.getProductByName(RecordsEnum.IMG_MJ.getName());// RecordsEnum.getByName(RecordsEnum.IMG_MJ);
        for(RoomMidjourneyMsgDO da : mjList){
            //if(da.getAction().equals(MjMsgActionEnum.DESCRIBE))
            //    hpd = RecordsEnum.getByName(RecordsEnum.IMG_MJ_TOWEN);
            //else if(da.getAction().equals(MjMsgActionEnum.UPSCALE))
            //    hpd = RecordsEnum.getByName(RecordsEnum.IMG_MJ_U);
            //else if(da.getAction().equals(MjMsgActionEnum.VARIATION))
            //    hpd = RecordsEnum.getByName(RecordsEnum.IMG_MJ_V);
            if(da.getMjType() == CommonEnum.commonOne)
                hpd= RecordsEnum.getByName(RecordsEnum.IMG_MJ);
            else
                hpd= RecordsEnum.getByName(RecordsEnum.IMG_MJ_RELAX);

            if(addRecords(-hpd.getRecords(),hpd.getModel(),hpd.getName(),da.getId(),da.getUserId())){
                if(haUserPermissionsService.updatePoints(da.getUserId(),-hpd.getRecords())) {
                    da.setIsDeducted(PayStatusEnum.SUCCESS.getCode());
                    midjourneyMsgService.update(da);
                }
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void mjChargeBack(){
        List<RoomMidjourneyMsgDO> mjList = midjourneyMsgService.getComMjList();
        //HaProductsDo hpd = RecordsEnum.getByName(RecordsEnum.IMG_MJ);
        if(null != mjList){
            //String mark = RecordsEnum.IMG_MJ.getName();
            for(RoomMidjourneyMsgDO da : mjList){
                //if(da.getAction().equals(MjMsgActionEnum.UPSCALE.getAction())) {
                //    mark = RecordsEnum.IMG_MJ_U.getName();
                //    hpd = RecordsEnum.getByName(RecordsEnum.IMG_MJ_U);
                //}else if(da.getAction().equals(MjMsgActionEnum.VARIATION)) {
                //    mark = RecordsEnum.IMG_MJ_V.getName();
                //    hpd = RecordsEnum.getByName(RecordsEnum.IMG_MJ_V);
                //}
                HaExpenseRecordsDo her = haExpenseRecordsService.getOneByModelId(da.getId(),null);

                if(null != her && addRecords(-her.getHoldBi(),her.getModel(),her.getMark(),da.getId(),da.getUserId())){
                    if(haUserPermissionsService.updatePoints(da.getUserId(),-her.getHoldBi())) {
                        da.setIsDeducted(PayStatusEnum.REBACK.getCode());
                        midjourneyMsgService.update(da);
                    }
                }
            }
        }

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
