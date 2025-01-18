package com.hncboy.beehive.web.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hncboy.beehive.base.constant.HaUserConstant;
import com.hncboy.beehive.base.domain.entity.HaUserParamDo;
import com.hncboy.beehive.base.exception.ServiceException;
import com.hncboy.beehive.base.handler.SensitiveWordHandler;
import com.hncboy.beehive.base.mapper.HaUserParamMapper;
import com.hncboy.beehive.base.util.FrontUserUtil;
import com.hncboy.beehive.web.domain.request.HaUserParamRequest;
import com.hncboy.beehive.web.service.HaUserParamService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author ll
 * @date 2023-7-25
 */
@Service
public class HaUserParamServiceImpl extends ServiceImpl<HaUserParamMapper, HaUserParamDo> implements HaUserParamService {
    @Override
    public boolean save(int userid) {

        HaUserParamDo haUserParamDo = HaUserConstant.setParamDefaultValues(userid);

        return this.save(haUserParamDo);
    }

    @Override
    public boolean update(HaUserParamRequest haUserParamRequest) {
        List<String> systemMessageSensitiveWords = SensitiveWordHandler.checkWord(haUserParamRequest.getSystemMessage());
        if (CollectionUtil.isNotEmpty(systemMessageSensitiveWords))
            throw new ServiceException("系统消息包含敏感词，请修改后重试");
        HaUserParamDo haUserParamDo = new HaUserParamDo();
        // 将前端传递的参数拷贝到实体对象中
        BeanUtils.copyProperties(haUserParamRequest, haUserParamDo);
        // 设置用户ID，
        //haUserParamDo.setUserid(FrontUserUtil.getUserId());

        // 构建更新条件，根据实体对象的ID更新数据
        UpdateWrapper<HaUserParamDo> updateWrapper = new UpdateWrapper<>();
        if(null != haUserParamRequest.getId() && haUserParamRequest.getId() > 0)
            updateWrapper.eq("id", haUserParamRequest.getId());
        else
            updateWrapper.eq("userid", FrontUserUtil.getUserId());

        // 执行更新操作
        return this.update(haUserParamDo, updateWrapper);
    }

    @Override
    public HaUserParamRequest getOne() {
        // 构建查询条件，根据userid查询数据
        QueryWrapper<HaUserParamDo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userid", FrontUserUtil.getUserId());

        // 根据条件查询一条数据
        HaUserParamDo haUserParamDo = this.getOne(queryWrapper);

        if(haUserParamDo == null)
            return null;
        // 将实体对象转换为请求对象并返回
        HaUserParamRequest haUserParamRequest = new HaUserParamRequest();
        BeanUtils.copyProperties(haUserParamDo, haUserParamRequest);
        return haUserParamRequest;
    }
    @Override
    public HaUserParamDo getOneById(String id){
        QueryWrapper<HaUserParamDo> queryWrapper = new QueryWrapper<>();
        queryWrapper
                .select("is_vip as isVip,validy_date as validyDate,id")
                .eq("userid", id);

        // 根据条件查询一条数据
        HaUserParamDo haUserParamDo = this.getOne(queryWrapper);

        return haUserParamDo;
    }
}
