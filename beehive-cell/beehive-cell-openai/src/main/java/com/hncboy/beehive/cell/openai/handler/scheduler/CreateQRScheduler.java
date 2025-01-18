package com.hncboy.beehive.cell.openai.handler.scheduler;

import cn.hutool.core.thread.ThreadUtil;
import com.hncboy.beehive.base.domain.entity.HaQrInfoDo;
import com.hncboy.beehive.base.enums.CommonEnum;
import com.hncboy.beehive.base.enums.PayStatusEnum;
import com.hncboy.beehive.web.service.RoomQrService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author ll
 * @date 2023/7/1
 * 二维码任务
 */
@Slf4j
@Component
public class CreateQRScheduler {

    @Resource
    private RoomQrService roomQrService;

    @Scheduled(cron = "0/20 * * * * ?")
    public void handler() {

        if(!CommonEnum.isBakRun)
            return;
        log.info(" CreateQRScheduler qr thread start.");
        List<HaQrInfoDo> qrLisr = roomQrService.getNotComQrList(PayStatusEnum.INIT.getCode());

        for (HaQrInfoDo qr : qrLisr) {
            try {
                if(isTimeOut(qr.getCreateTime())){
                    qr.setDescription("执行超时");
                    qr.setStatus(PayStatusEnum.FAILURE.getCode());
                    qr.setIsCompleted(PayStatusEnum.FAILURE.getCode());
                    roomQrService.updateById(qr);
                }

            } catch (Exception e) {

            }

            // 不频繁请求
            ThreadUtil.sleep(2000);
        }
    }

    public boolean isTimeOut(Date timestamp) {
        Date now = new Date();

        // 创建一个Calendar实例，并设置为当前时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);

        // 将分钟数减去1
        calendar.add(Calendar.MINUTE, -1);

        // 获取1分钟前的时间
        Date oneMinuteAgo = calendar.getTime();

        // 判断一个时间是否距离现在超过1分钟
        return timestamp.before(oneMinuteAgo);
    }
}
