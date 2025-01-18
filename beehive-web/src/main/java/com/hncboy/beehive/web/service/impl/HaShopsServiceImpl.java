package com.hncboy.beehive.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hncboy.beehive.base.domain.entity.HaShopsDo;
import com.hncboy.beehive.base.mapper.HaShopsMapper;
import com.hncboy.beehive.web.service.HaShopsService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author ll
 * @date 2023-7-25
 */
@Service
public class HaShopsServiceImpl extends ServiceImpl<HaShopsMapper, HaShopsDo> implements HaShopsService {
    @Override
    public boolean saveDo(HaShopsDo haShopsDo) {

        return this.save(haShopsDo);
    }

    @Override
    public boolean update(HaShopsDo haShopsDo) {
        return false;
    }

    @Override
    public List<HaShopsDo> getList() {
        QueryWrapper<HaShopsDo> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq("enable", 1);
        queryWrapper.orderByAsc("amount");

        // 根据条件查询一条数据
        return this.list(queryWrapper);
    }

}
