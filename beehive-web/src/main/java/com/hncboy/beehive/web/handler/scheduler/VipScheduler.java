package com.hncboy.beehive.web.handler.scheduler;

import com.hncboy.beehive.base.domain.entity.HaExpenseRecordsDo;
import com.hncboy.beehive.base.domain.entity.HaRechargeRecordsDo;
import com.hncboy.beehive.base.domain.entity.HaUserPermissionsDo;
import com.hncboy.beehive.base.enums.CommonEnum;
import com.hncboy.beehive.base.enums.PayStatusEnum;
import com.hncboy.beehive.web.service.HaUserPermissionsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author ll
 * @date 2023/7/1
 * vip更新及免费积分更新
 */
@Slf4j
@Component
public class VipScheduler implements Runnable {
    @Resource
    private HaUserPermissionsService haUserPermissionsService;

    @Resource
    private com.hncboy.beehive.web.service.HaExpenseRecordsService haExpenseRecordsService;

    @Resource
    private com.hncboy.beehive.web.service.HaRechargeRecordsService haRechargeRecordsService;



    @Override
    public void run() {//ApplicationArguments args
        if(!CommonEnum.isBakRun)
            return;
        log.info("5、 VIP thread start.");
            try {
                ScheduledExecutorService executor = Executors.newScheduledThreadPool(3);

                // 安排每30秒钟执行一次deductedQr()方法
                executor.scheduleAtFixedRate(this::updateVip, 0, 30, TimeUnit.SECONDS);

                // 安排每3分钟执行一次updateProduct()方法
                executor.scheduleAtFixedRate(this::updateIsGiving, 0, 1, TimeUnit.MINUTES);

                // 安排每3分钟执行一次updateProduct()方法
                executor.scheduleAtFixedRate(this::addFreePoints, 0, 1, TimeUnit.MINUTES);

            }catch(Exception e){
                log.error("更新product失败。" + e.getMessage());
            }finally {
                try {
                    Thread.sleep(20 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
    }


//更新vip到期处理
    @Transactional(rollbackFor = Exception.class)
    public void updateVip(){
        List<HaUserPermissionsDo> updList = haUserPermissionsService.getAllVip();
        if(null != updList){
            for(HaUserPermissionsDo hpd :updList){
                if((new Date()).after(hpd.getValidyDate())) {
                    haUserPermissionsService.updateNoVip(hpd.getUserId(), CommonEnum.noVip);

                    log.info("updateVip vip掉线：" + hpd.getUserId());
                }
            }
        }
    }
    //每个月固定赠送积分
    @Transactional(rollbackFor = Exception.class)
    public void addFreePoints(){
        LocalTime currentTime = LocalTime.now();
        if(CommonEnum.isRunAddFree && currentTime.isAfter(LocalTime.of(00, 00)) && currentTime.isBefore(LocalTime.of(00, 05))) {

            int currentDayOfMonth = LocalDate.now().getDayOfMonth();
            List<HaUserPermissionsDo> updList = new ArrayList<>();
            if (isLastDay() && currentDayOfMonth != 31) {
                for (int i = currentDayOfMonth; i < 31; i++) {
                    updList = haUserPermissionsService.getFreeList(i);
                    if (null != updList) {
                        for (HaUserPermissionsDo hpd : updList) {
                            addFreeCords(hpd.getUserId());
                            haUserPermissionsService.updateFreePoints(hpd.getUserId(), CommonEnum.freePoints);
                        }

                    }
                }
            } else {
                updList = haUserPermissionsService.getFreeList(currentDayOfMonth);
                if (null != updList) {
                    for (HaUserPermissionsDo hpd : updList) {
                        addFreeCords(hpd.getUserId());
                        haUserPermissionsService.updateFreePoints(hpd.getUserId(), CommonEnum.freePoints);
                    }

                }
            }
            log.info("addFreePoints 赠送积分，" + updList.size());
            CommonEnum.isRunAddFree = false;
        }else if(currentTime.isAfter(LocalTime.of(00, 06)) && currentTime.isBefore(LocalTime.of(23, 59))){
            CommonEnum.isRunAddFree = true;
        }

    }
    private boolean addFreeCords(int userId){
        HaRechargeRecordsDo haRechargeRecordsDo = new HaRechargeRecordsDo();
        haRechargeRecordsDo.setStatus(PayStatusEnum.NOTHING);
        haRechargeRecordsDo.setMark("VIP用户赠送万码AI对话3.5积分"+ CommonEnum.freePoints);
        haRechargeRecordsDo.setIsAddPoints(1);
        haRechargeRecordsDo.setAmount(0);
        haRechargeRecordsDo.setPoints(CommonEnum.freePoints);
        haRechargeRecordsDo.setPayResult("VIP用户赠送积分");
        haRechargeRecordsDo.setUserId(userId);
        return  haRechargeRecordsService.save(haRechargeRecordsDo);
    }
    //更新是否添加免费积分标志位
    private void updateIsGiving(){
        if(isLastDay()){////如果是当月第一天,更新所有vip用户为未赠送积分
            LocalTime currentTime = LocalTime.now();

            if (CommonEnum.isRunUpdateFree && currentTime.isAfter(LocalTime.of(23, 55)) && currentTime.isBefore(LocalTime.of(23, 59))) {
                haUserPermissionsService.updateNoGiving();
                CommonEnum.isRunUpdateFree = false;
                log.info("updateIsGiving 更新所有vip为未赠送积分状态");
            }else if(currentTime.isAfter(LocalTime.of(00, 00)) && currentTime.isBefore(LocalTime.of(23, 54))){
                CommonEnum.isRunUpdateFree = true;
            }

        }
    }

    //判断今天是不是这个月的最后一天
    public boolean isLastDay(){
        LocalDate today = LocalDate.now();
        LocalDate lastDayOfMonth = today.withDayOfMonth(today.lengthOfMonth());
        return today.equals(lastDayOfMonth);

    }
    //判断今天是不是这个月的第一天
    public boolean isFirstDay(){
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        return today.equals(firstDayOfMonth);
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
