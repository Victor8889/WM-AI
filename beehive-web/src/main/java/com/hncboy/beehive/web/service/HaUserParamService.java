package com.hncboy.beehive.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hncboy.beehive.base.domain.entity.HaUserParamDo;
import com.hncboy.beehive.web.domain.request.HaUserParamRequest;

/**
 * @author ll
 * @date 2023-7-25
 */
public interface HaUserParamService  extends IService<HaUserParamDo> {
    /**
     *
     *
     * @return id
     */
    boolean save(int userid);
    /**
     * 参数更新
     *
     * @return id
     */
    boolean update(HaUserParamRequest haUserParamRequest);

    HaUserParamRequest getOne();
    HaUserParamDo getOneById(String id);
}
