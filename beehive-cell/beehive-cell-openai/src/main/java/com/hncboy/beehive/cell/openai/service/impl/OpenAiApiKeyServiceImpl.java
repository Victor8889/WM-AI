package com.hncboy.beehive.cell.openai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hncboy.beehive.base.domain.entity.OpenAiApiKeyDO;
import com.hncboy.beehive.base.enums.OpenAiApiKeyStatusEnum;
import com.hncboy.beehive.base.mapper.OpenAiApiKeyMapper;
import com.hncboy.beehive.cell.openai.service.OpenAiApiKeyService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author ll
 * @date 2023/6/30
 * OpenAi ApiKey 业务实现类
 */
@Service
public class OpenAiApiKeyServiceImpl extends ServiceImpl<OpenAiApiKeyMapper, OpenAiApiKeyDO> implements OpenAiApiKeyService {

    public List<OpenAiApiKeyDO> dalle3ApiList(){
        QueryWrapper<OpenAiApiKeyDO> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq("status", OpenAiApiKeyStatusEnum.ENABLE.getCode());
        queryWrapper.eq("use_scenes", "[\"DALLE3\"]");

        // 根据条件查询一条数据
        return this.list(queryWrapper);
    }
    public List<OpenAiApiKeyDO> getApiListBy(String useSence){
        QueryWrapper<OpenAiApiKeyDO> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq("status", OpenAiApiKeyStatusEnum.ENABLE.getCode());
        queryWrapper.eq("use_scenes", useSence);

        // 根据条件查询一条数据
        return this.list(queryWrapper);
    }

    public List<OpenAiApiKeyDO> getApiList(){
        QueryWrapper<OpenAiApiKeyDO> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq("status", OpenAiApiKeyStatusEnum.ENABLE.getCode());

        // 根据条件查询一条数据
        return this.list(queryWrapper);
    }
}
