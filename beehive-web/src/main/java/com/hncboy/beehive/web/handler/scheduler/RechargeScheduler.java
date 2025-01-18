package com.hncboy.beehive.web.handler.scheduler;

import cn.hutool.core.thread.ThreadUtil;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipayLogger;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.hncboy.beehive.base.domain.entity.HaInviteDo;
import com.hncboy.beehive.base.domain.entity.HaRechargeRecordsDo;
import com.hncboy.beehive.base.domain.entity.HaUserPermissionsDo;
import com.hncboy.beehive.base.enums.CommonEnum;
import com.hncboy.beehive.base.enums.PayStatusEnum;
import com.hncboy.beehive.base.enums.PayTypeEnum;
import com.hncboy.beehive.base.util.ZfbUtil;
import com.hncboy.beehive.web.service.HaRechargeRecordsService;
import com.hncboy.beehive.web.service.HaUserPermissionsService;
import jakarta.annotation.Resource;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author ll
 * @date 2023/7/1
 * OpenAi ApiKey 定时任务
 * 邀请积分
 * vip
 */
@Slf4j
@Component
public class RechargeScheduler {

    @Resource
    private HaUserPermissionsService haUserPermissionsService;

    @Resource
    private HaRechargeRecordsService haRechargeRecordsService;

    @Resource
    private com.hncboy.beehive.web.service.HaInviteService haInviteService;

    @Scheduled(cron = "*/10 * * * * *")
    public void handler() throws AlipayApiException {
        if(!CommonEnum.isBakRun)
            return;

        mobilePayStatus();
        addPoints();

    }
    @Transactional(rollbackFor = Exception.class)
    public void addPoints(){
        List<HaRechargeRecordsDo> lhd = haRechargeRecordsService.getPermissionList();
        for (HaRechargeRecordsDo hrrd : lhd) {
            try {
                HaUserPermissionsDo hpd = new HaUserPermissionsDo();
                HaUserPermissionsDo haUserPermissionsDo = haUserPermissionsService.getOne(hrrd.getUserId());
                hpd.setId(haUserPermissionsDo.getId());
                if(null != hrrd.getPayMark() && CommonEnum.vip.equals(hrrd.getPayMark())){
                    Date vipDate = new Date();
                    if(haUserPermissionsDo.getValidyDate().before(new Date())){//已过期或者未充值过
                        LocalDateTime currentDate = LocalDateTime.now();
                        LocalDateTime  newDate = currentDate.plusDays(hrrd.getPoints());
                        vipDate = Date.from(newDate.atZone(ZoneId.systemDefault()).toInstant());
                        hpd.setVipTime(new Date());
                        if(haUserPermissionsDo.getIsGiving() == CommonEnum.noGiving){
                            hpd.setRemainHoldCount(CommonEnum.freePoints);
                            hpd.setIsGiving(CommonEnum.isGiving);
                        }
                    }else{
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(haUserPermissionsDo.getValidyDate());
                        calendar.add(Calendar.DAY_OF_MONTH, hrrd.getPoints());
                        vipDate = calendar.getTime();
                    }

                    hpd.setValidyDate(vipDate);
                    hpd.setIsVip(CommonEnum.isVip);
                }else
                    hpd.setRemainHoldBi(haUserPermissionsDo.getRemainHoldBi() + hrrd.getPoints());
                if(StringUtil.isNotBlank(hrrd.getPayMark()) && hrrd.getPayMark().equals(CommonEnum.vip) && haUserPermissionsDo.getIsGiving() == CommonEnum.noGiving)
                    addFreeCords(haUserPermissionsDo.getUserId());
                haUserPermissionsService.update(hpd);

                //更新邀请人的积分
                if (haUserPermissionsDo.getOtherUserId() != null) {
                    int morePoins = getPoints(hrrd.getPoints());
                    int otherUserId = haUserPermissionsDo.getOtherUserId();
                    haUserPermissionsDo = haUserPermissionsService.getOne(otherUserId);
                    hpd.setRemainHoldBi(haUserPermissionsDo.getRemainHoldBi() + morePoins);
                    hpd.setId(haUserPermissionsDo.getId());
                    if(haUserPermissionsService.update(hpd)){
                        //增加邀请记录
                        HaInviteDo hd = new HaInviteDo();
                        hd.setPoints(morePoins);
                        hd.setUserId(otherUserId);
                        hd.setInviteUserId(hrrd.getUserId());
                        hd.setMark("邀请者id：" + otherUserId + "充值，自己获得" + morePoins + "积分");
                        haInviteService.saveDo(hd);

                        log.info("RECHARGE points thread。邀请者id：" + otherUserId + "充值，自己获得" + morePoins + "积分");
                    }
                }

            } catch (Exception e) {
                log.info("RECHARGE points thread error。id："+hrrd.getId()+",points:"+hrrd.getPoints()+"error:"+e.getMessage());
                e.printStackTrace();
                ThreadUtil.sleep(50);
            }finally {
                HaRechargeRecordsDo hr = new HaRechargeRecordsDo();
                hr.setIsAddPoints(1);
                hr.setId(hrrd.getId());
                haRechargeRecordsService.updateById(hr);
                hr = null;
            }
            log.info("RECHARGE points thread。id："+hrrd.getId()+",points:"+hrrd.getPoints());
            ThreadUtil.sleep(50);
        }
    }
    private boolean addFreeCords(int userId){
        HaRechargeRecordsDo haRechargeRecordsDo = new HaRechargeRecordsDo();
        haRechargeRecordsDo.setStatus(PayStatusEnum.NOTHING);
        haRechargeRecordsDo.setMark("VIP用户赠送AI对话3.5积分"+ CommonEnum.freePoints);
        haRechargeRecordsDo.setIsAddPoints(1);
        haRechargeRecordsDo.setAmount(0);
        haRechargeRecordsDo.setPoints(CommonEnum.freePoints);
        haRechargeRecordsDo.setPayResult("VIP用户赠送积分");
        haRechargeRecordsDo.setUserId(userId);
        return  haRechargeRecordsService.save(haRechargeRecordsDo);
    }

    private int getPoints(int points){
        int  newp = (int)(points * 0.1);
        int remainder = points % 10;
        if(remainder != 0)
            newp ++;
        return newp;
    }
    @Transactional(rollbackFor = Exception.class)
    public void mobilePayStatus() throws AlipayApiException {
        AlipayClient alipayClient = ZfbUtil.zfbClinet();
        AlipayLogger.setNeedEnableLogger(false) ;
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        for (PayTypeEnum pte : PayTypeEnum.values()) {

            if (pte.getCode() == 2 || pte.getCode() == 1) {
                List<HaRechargeRecordsDo> lhd = haRechargeRecordsService.getMobileNotPayList(pte);
                if (null != lhd) {
                    for (HaRechargeRecordsDo hr : lhd) {
                        //获取一次状态，如果还是未支付就关闭订单，如果支付成功就更新状态，如果异常就更新状态
                        //从ali获取状态，更新数据库

                        com.alibaba.fastjson.JSONObject bizContent = new com.alibaba.fastjson.JSONObject();
                        //商户订单号，商家自定义，保持唯一性
                        bizContent.put("out_trade_no", hr.getOrderId());

                        request.setBizContent(bizContent.toString());

                        AlipayTradeQueryResponse response = alipayClient.certificateExecute(request);
                        if (response.isSuccess()) {
                            String trade_no = response.getTradeNo();
                            String buyer_logon_id = response.getBuyerLogonId() + "、" + response.getBuyerUserName() + "、" + response.getBuyerPayAmount() + "、sysAuto";
                            String trade_status = response.getTradeStatus();
                            HaRechargeRecordsDo haRechargeRecordsDo = new HaRechargeRecordsDo();
                            haRechargeRecordsDo.setBuyerLogonId(buyer_logon_id);
                            haRechargeRecordsDo.setTradeNo(trade_no);
                            haRechargeRecordsDo.setIsAddPoints(0);
                            haRechargeRecordsDo.setId(hr.getId());
                            if ("TRADE_SUCCESS".equals(trade_status) || "TRADE_FINISHED".equals(trade_status)) {

                                haRechargeRecordsDo.setStatus(PayStatusEnum.SUCCESS);
                                haRechargeRecordsService.updateById(haRechargeRecordsDo);

                                log.info("RECHARGE status thread。id："+haRechargeRecordsDo.getId()+",points:"+haRechargeRecordsDo.getPoints()+",status:"+haRechargeRecordsDo.getStatus());

                            }
                        }
                    }
                }
            }
        }
    }
}
