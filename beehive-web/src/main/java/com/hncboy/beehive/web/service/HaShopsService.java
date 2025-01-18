package com.hncboy.beehive.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hncboy.beehive.base.domain.entity.HaShopsDo;

import java.util.List;

/**
 * @author ll
 * @date 2023-7-25
 */
public interface HaShopsService extends IService<HaShopsDo> {
    /**
     *
     *
     * @return id
     */
    boolean saveDo(HaShopsDo haInviteDo);
    /**
     * 参数更新
     *
     * @return id
     */
    boolean update(HaShopsDo haInviteDo);

     List<HaShopsDo> getList();
}
