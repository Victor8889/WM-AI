package com.hncboy.beehive.web.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hncboy.beehive.base.domain.entity.HaRechargeRecordsDo;
import com.hncboy.beehive.base.domain.query.PageQuery;
import com.hncboy.beehive.base.enums.PayTypeEnum;
import com.hncboy.beehive.web.domain.vo.HaRechargeRecordsVo;

import java.util.List;

/**
 * @author ll
 * @date 2023-7-25
 */
public interface HaRechargeRecordsService  extends IService<HaRechargeRecordsDo> {
    /**
     *
     *
     * @return id
     */
    int save(int amount, int points, PayTypeEnum type, String orderId,String mark,String payMark);
    /**
     * 参数更新
     *
     * @return id
     */
    boolean updateById(HaRechargeRecordsDo haRechargeRecordsDo);
    boolean updateByOrderId(HaRechargeRecordsDo haRechargeRecordsDo);

    HaRechargeRecordsDo getOne();

    HaRechargeRecordsDo getOne(int id);
    IPage<HaRechargeRecordsVo> pageRecharge(PageQuery recodeQuery);

    List<HaRechargeRecordsDo> getPermissionList();

    List<HaRechargeRecordsDo> getNotPayList(PayTypeEnum type);
    List<HaRechargeRecordsDo> getMobileNotPayList(PayTypeEnum type);
}
