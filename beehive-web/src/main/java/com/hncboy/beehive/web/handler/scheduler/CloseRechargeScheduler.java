package com.hncboy.beehive.web.handler.scheduler;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipayLogger;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.hncboy.beehive.base.domain.entity.HaRechargeRecordsDo;
import com.hncboy.beehive.base.enums.CommonEnum;
import com.hncboy.beehive.base.enums.PayStatusEnum;
import com.hncboy.beehive.base.enums.PayTypeEnum;
import com.hncboy.beehive.base.util.ZfbUtil;
import com.hncboy.beehive.web.service.HaRechargeRecordsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author ll
 * @date 2023/7/1
 * 阿里支付状态确认
 */
@Slf4j
@Component
public class CloseRechargeScheduler {


    @Resource
    private HaRechargeRecordsService haRechargeRecordsService;


    @Scheduled(cron = "0 0/5 * * * *")
    public void handler() throws AlipayApiException {
        if(!CommonEnum.isBakRun)
            return;
        AlipayClient alipayClient = ZfbUtil.zfbClinet();
        AlipayLogger.setNeedEnableLogger(false) ;
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        AlipayTradeCloseRequest closeRequest = new AlipayTradeCloseRequest();
        for(PayTypeEnum pte : PayTypeEnum.values()) {

            if (pte.getCode() == 1 || pte.getCode() == 2) {
                List<HaRechargeRecordsDo> lhd = haRechargeRecordsService.getNotPayList(pte);
                if (null != lhd) {
                    for (HaRechargeRecordsDo hr : lhd) {
                        //获取一次状态，如果还是未支付就关闭订单，如果支付成功就更新状态，如果异常就更新状态
                        //从ali获取状态，更新数据库

                        JSONObject bizContent = new JSONObject();
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
                            haRechargeRecordsDo.setIsAddPoints(2);
                            haRechargeRecordsDo.setId(hr.getId());
                            if ("TRADE_SUCCESS".equals(trade_status) || "TRADE_FINISHED".equals(trade_status)) {

                                haRechargeRecordsDo.setIsAddPoints(1);
                                haRechargeRecordsDo.setStatus(PayStatusEnum.SUCCESS);
                                haRechargeRecordsService.updateById(haRechargeRecordsDo);
                                log.info("close RECHARGE order thread。充值id：" + haRechargeRecordsDo.getId() + "，trade_status" + trade_status + "，1");
                            } else if ("TRADE_CLOSED".equals(trade_status)) {

                                haRechargeRecordsDo.setBuyerLogonId(buyer_logon_id + "、TRADE_CLOSED");
                                haRechargeRecordsDo.setPayResult("交易未支付，超时未系统关闭，无需处理");
                                haRechargeRecordsDo.setStatus(PayStatusEnum.CLOSED);
                                log.info("close RECHARGE order thread。充值id：" + haRechargeRecordsDo.getId() + "，trade_status" + trade_status + "，2");
                                haRechargeRecordsService.updateById(haRechargeRecordsDo);
                            } else if ("WAIT_BUYER_PAY".equals(trade_status)) {

                                closeRequest.setBizContent(bizContent.toString());
                                //先调用接口关闭订单，然后修改状态
                                bizContent = new JSONObject();
                                bizContent.put("trade_no", hr.getOrderId());
                                request.setBizContent(bizContent.toString());
                                AlipayTradeCloseResponse  closeResponse = alipayClient.certificateExecute(closeRequest);
                                if(closeResponse.isSuccess()) {
                                    haRechargeRecordsDo.setBuyerLogonId(buyer_logon_id + "、WAIT_BUYER_PAY to CLOSE");

                                    haRechargeRecordsDo.setPayResult("超时未支付，系统关闭，无需处理");
                                    haRechargeRecordsDo.setStatus(PayStatusEnum.CLOSED);
                                    haRechargeRecordsService.updateById(haRechargeRecordsDo);
                                    log.info("close RECHARGE order thread。关闭订单，id：" + haRechargeRecordsDo.getId() + "，trade_status" + trade_status + "，3");
                                }
                            }
                        }else{
                            //调用失败
                            HaRechargeRecordsDo haRechargeRecordsDo = new HaRechargeRecordsDo();
                            haRechargeRecordsDo.setIsAddPoints(2);
                            haRechargeRecordsDo.setId(hr.getId());
                            haRechargeRecordsDo.setPayResult("异常，无支付内容，无需处理");
                            haRechargeRecordsDo.setStatus(PayStatusEnum.OTHER);
                            haRechargeRecordsService.updateById(haRechargeRecordsDo);
                            log.info("close RECHARGE order thread。不存在订单，id：" + haRechargeRecordsDo.getId() + "，trade_status" +  "，4");
                        }
                    }
                }
            }
         }
    }

}
