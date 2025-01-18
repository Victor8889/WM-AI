package com.hncboy.beehive.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hncboy.beehive.base.domain.entity.HaVipPriceDo;
import com.hncboy.beehive.base.mapper.HaVipPriceMapper;
import com.hncboy.beehive.web.service.HaVipPriceService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author ll
 * @date 2023-7-25
 */
@Service
public class HaVipPriceServiceImpl extends ServiceImpl<HaVipPriceMapper, HaVipPriceDo> implements HaVipPriceService {

    @Override
    public boolean saveDo(HaVipPriceDo haVipPriceDo) {
        return false;
    }

    @Override
    public List<HaVipPriceDo> getList() {
        try {
            LambdaQueryWrapper<HaVipPriceDo> queryWrapper = new LambdaQueryWrapper<>();

            queryWrapper.orderByAsc(HaVipPriceDo::getId);
            return this.list(queryWrapper);
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
