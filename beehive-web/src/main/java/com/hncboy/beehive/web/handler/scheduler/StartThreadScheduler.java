package com.hncboy.beehive.web.handler.scheduler;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @author ll
 * @date 2024-1-25
 */

@Slf4j
@Component
public class StartThreadScheduler implements ApplicationRunner{
    @Resource
    private ProductScheduler productScheduler;
    @Resource
    private VipScheduler vipScheduler;
    @Override
    public void run(ApplicationArguments args) throws Exception {

        new Thread(productScheduler).start();
        new Thread(vipScheduler).start();
    }
}
