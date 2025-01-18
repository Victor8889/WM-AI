package com.hncboy.beehive.web.handler.scheduler;

import com.hncboy.beehive.base.domain.entity.HaExpenseRecordsDo;
import com.hncboy.beehive.base.domain.entity.HaProductsDo;
import com.hncboy.beehive.base.domain.entity.HaQrInfoDo;
import com.hncboy.beehive.base.domain.vo.ModelSelectVO;
import com.hncboy.beehive.base.enums.CommonEnum;
import com.hncboy.beehive.base.enums.RechargeEnum;
import com.hncboy.beehive.base.enums.RecordsEnum;
import com.hncboy.beehive.web.service.HaNoticeService;
import com.hncboy.beehive.web.service.HaShopsService;
import com.hncboy.beehive.web.service.HaUserPermissionsService;
import com.hncboy.beehive.web.service.RoomQrService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author ll
 * @date 2023/7/1
 * 商品和价格定时获取
 */
@Slf4j
@Component
public class ProductScheduler implements Runnable {

    @Resource
    private com.hncboy.beehive.web.service.HaProductService haProductService;
    @Resource
    private HaUserPermissionsService haUserPermissionsService;

    @Resource
    private com.hncboy.beehive.web.service.HaInviteService haInviteService;

    @Resource
    private com.hncboy.beehive.web.service.HaExpenseRecordsService haExpenseRecordsService;

    @Resource
    private RoomQrService roomQrService;

    @Resource
    private HaShopsService haShopsService;

    @Resource
    private com.hncboy.beehive.web.service.HaVipPriceService haVipPriceService;

    @Resource
    private  HaNoticeService haNoticeService;


    @Override
    public void run() {//ApplicationArguments args
        //if(!CommonEnum.isMainRun)
        //    return;
        log.info("2、 ProductScheduler product QR thread start.");
            try {
                ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

                // 安排每3分钟执行一次updateProduct()方法
                executor.scheduleAtFixedRate(this::updateProduct, 0, 3, TimeUnit.MINUTES);

                // 安排每10秒钟执行一次deductedQr()方法
                executor.scheduleAtFixedRate(this::deductedQr, 0, 10, TimeUnit.SECONDS);
                // 安排每10秒钟执行一次deductedQr()方法
                executor.scheduleAtFixedRate(this::updateNotice, 0, 5, TimeUnit.MINUTES);

            }catch(Exception e){
                log.error("更新product失败。" + e.getMessage());
            }
    }

    /**
     * 更新公告
     */
    private void updateNotice(){
        CommonEnum.noticeList = haNoticeService.getList();
        log.info("end更新 notice" );
    }
    private void updateProduct(){
        try {
            log.info("更新 RecordsEnum RechargeEnum vipHaVipPriceDo" );
            RecordsEnum.lhd = haProductService.getChargeLst();
            CommonEnum.modelSelect = getSelectProduct();
            RechargeEnum.lhsd = haShopsService.getList();
            CommonEnum.vipl = haVipPriceService.getList();
            RecordsEnum.showLhd = getValidProduct();//去掉无效的
            CommonEnum.NAME_MAP_PRODUCT = RecordsEnum.lhd.stream()
                    .filter(hpd -> Objects.nonNull(hpd.getApiModel())) // 过滤掉 name 为 null 的情况
                    .collect(Collectors.toMap(HaProductsDo::getApiModel, hpd -> hpd));

            log.info("end更新 RecordsEnum RechargeEnum vipHaVipPriceDo" );
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private List<HaProductsDo> getValidProduct(){
        try{
            List<HaProductsDo> hpdl = new ArrayList<>();
            for(HaProductsDo hpd: RecordsEnum.lhd){
                if(hpd.getIsInvalid() == CommonEnum.commonOne)
                    hpdl.add(hpd);

            }
            return hpdl;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    private List<ModelSelectVO> getSelectProduct(){
        try{
            List<ModelSelectVO> hpdl = new ArrayList<>();
            for(HaProductsDo hpd: RecordsEnum.lhd){
                if(hpd.getIsInvalid() == CommonEnum.commonOne && hpd.getIsChat() != null && hpd.getIsChat() == CommonEnum.commonOne){
                    ModelSelectVO ms = new ModelSelectVO();
                    ms.setLabel(hpd.getShowKey());
                    ms.setValue(hpd.getApiModel());
                    hpdl.add(ms);
                }

            }
            return hpdl;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    @Transactional(rollbackFor = Exception.class)
    public void deductedQr(){
        try {
            List<HaQrInfoDo> qrList = roomQrService.getComQrList();
            HaProductsDo hpd = RecordsEnum.getByName(RecordsEnum.IMG_QR);
            for (HaQrInfoDo qr : qrList) {
                if (addRecords(-hpd.getRecords(), hpd.getModel(), hpd.getName(), qr.getId(), qr.getUserId())) {
                    if (haUserPermissionsService.updatePoints(qr.getUserId(), -hpd.getRecords())) {
                        qr.setIsDeducted(1);
                        roomQrService.update(qr);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
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
