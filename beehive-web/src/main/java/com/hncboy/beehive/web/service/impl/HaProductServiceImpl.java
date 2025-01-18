package com.hncboy.beehive.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hncboy.beehive.base.domain.entity.HaProductsDo;
import com.hncboy.beehive.base.mapper.HaProductMapper;
import com.hncboy.beehive.web.service.HaProductService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author ll
 * @date 2023-7-25
 */
@Service
public class HaProductServiceImpl extends ServiceImpl<HaProductMapper, HaProductsDo> implements HaProductService {

    @Override
    public boolean saveDo(HaProductsDo haProductDo) {
        return false;
    }

    @Override
    public List<HaProductsDo> getLst() {
        try {
            QueryWrapper<HaProductsDo> queryWrapper = new QueryWrapper<>();
            queryWrapper
                    .eq("is_invalid",1)
                    .orderByAsc("id");// orderByAsc(HaProductsDo::getId);
            return this.list(queryWrapper);
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    @Override
    public List<HaProductsDo> getChargeLst() {
        try {
            QueryWrapper<HaProductsDo> queryWrapper = new QueryWrapper<>();
            queryWrapper
                    .orderByAsc("id");// orderByAsc(HaProductsDo::getId);
            return this.list(queryWrapper);
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
