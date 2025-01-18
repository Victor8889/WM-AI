package com.hncboy.beehive.web.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hncboy.beehive.base.domain.entity.HaInviteDo;
import com.hncboy.beehive.base.domain.query.PageQuery;
import com.hncboy.beehive.web.domain.vo.HaInviteVo;

/**
 * @author ll
 * @date 2023-7-25
 */
public interface HaInviteService extends IService<HaInviteDo> {
    /**
     *
     *
     * @return id
     */
    boolean saveDo(HaInviteDo haInviteDo);
    /**
     * 参数更新
     *
     * @return id
     */
    boolean update(HaInviteDo haInviteDo);

    HaInviteDo getOne();
    IPage<HaInviteVo> pageInvite(PageQuery recodeQuery);
}
