package com.hncboy.beehive.cell.openai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hncboy.beehive.base.domain.entity.OpenAiApiKeyDO;

import java.util.List;

/**
 * @author ll
 * @date 2023/6/30
 *  OpenAi ApiKey 业务接口
 */
public interface OpenAiApiKeyService extends IService<OpenAiApiKeyDO> {
    public List<OpenAiApiKeyDO> dalle3ApiList();
    List<OpenAiApiKeyDO> getApiListBy(String useSence);
    public List<OpenAiApiKeyDO> getApiList();
}
