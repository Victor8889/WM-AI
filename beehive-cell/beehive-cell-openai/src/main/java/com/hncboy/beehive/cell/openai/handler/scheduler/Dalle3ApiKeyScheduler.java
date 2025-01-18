package com.hncboy.beehive.cell.openai.handler.scheduler;

import com.hncboy.beehive.base.domain.entity.OpenAiApiKeyDO;
import com.hncboy.beehive.base.enums.CommonEnum;
import com.hncboy.beehive.cell.openai.service.OpenAiApiKeyService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ll
 * @date 2023/7/1
 * OpenAi ApiKey 定时任务
 * 1分钟定时刷新Dalle3 api key
 */

@Slf4j
@Component
public class Dalle3ApiKeyScheduler implements Runnable  {


    @Resource
    private OpenAiApiKeyService openAiApiKeyService;

    @Override
    public void run() {//ApplicationArguments args
        //if(!CommonEnum.isMainRun)
        //    return;
        while(true) {
            try {
                log.info("Api ApiKey 定时更新任务开始");

                CommonEnum.openApiKey = openAiApiKeyService.getApiList();

                CommonEnum.dalle3Api = getByUseScene("DALLE3");//openAiApiKeyService.dalle3ApiList();
                CommonEnum.mjApi = getByUseScene("MJ-FAST");//openAiApiKeyService.getApiListBy("[\"MJ\"]");
                CommonEnum.mjRelaxApi = getByUseScene("MJ-RELAX");//openAiApiKeyService.getApiListBy("[\"MJ-RELAX\"]");
                Thread.sleep(4 * 60 * 1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private List<OpenAiApiKeyDO> getByUseScene(String useScene){

        if(CommonEnum.openApiKey != null){
            List<OpenAiApiKeyDO> apiList = new ArrayList<>();
            for(OpenAiApiKeyDO apis : CommonEnum.openApiKey){
                if(null != apis.getUseScenes() && apis.getUseScenes().contains(useScene))
                    apiList.add(apis);
            }
            return apiList;
        }
        return null;
    }

}
