package com.hncboy.beehive.cell.openai.handler.scheduler;

import com.hncboy.beehive.base.enums.CommonEnum;
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
    private Dalle3ApiKeyScheduler dalle3ApiKeyScheduler;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        new Thread(dalle3ApiKeyScheduler).start();
    }
}
