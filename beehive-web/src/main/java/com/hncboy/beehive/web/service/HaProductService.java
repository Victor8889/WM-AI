package com.hncboy.beehive.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hncboy.beehive.base.domain.entity.HaProductsDo;

import java.util.List;

/**
 * @author ll
 * @date 2023-7-25
 */
public interface HaProductService extends IService<HaProductsDo> {
    /**
     *
     *
     * @return id
     */
    boolean saveDo(HaProductsDo haProductDo);

    List<HaProductsDo> getLst();
    public List<HaProductsDo> getChargeLst();
}
