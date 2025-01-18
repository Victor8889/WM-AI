package com.hncboy.beehive.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hncboy.beehive.base.domain.entity.HaVipPriceDo;

import java.util.List;

/**
 * @author ll
 * @date 2023-7-25
 */
public interface HaVipPriceService extends IService<HaVipPriceDo> {
    /**
     *
     *
     * @return id
     */
    boolean saveDo(HaVipPriceDo haVipPriceDo);

    List<HaVipPriceDo> getList();
}
