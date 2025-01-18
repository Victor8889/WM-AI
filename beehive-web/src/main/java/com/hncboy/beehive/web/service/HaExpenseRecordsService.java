package com.hncboy.beehive.web.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hncboy.beehive.base.domain.entity.HaExpenseRecordsDo;
import com.hncboy.beehive.base.domain.query.PageQuery;
import com.hncboy.beehive.web.domain.vo.HaExpenseRecordsVo;

/**
 * @author ll
 * @date 2023-7-25
 */
public interface HaExpenseRecordsService  extends IService<HaExpenseRecordsDo> {
    /**
     *
     *
     * @return id
     */
    boolean save(HaExpenseRecordsDo haExpenseRecordsDo);
    /**
     * 参数更新
     *
     * @return id
     */
    boolean update(HaExpenseRecordsDo haExpenseRecordsDo);

    HaExpenseRecordsDo getOne();
    HaExpenseRecordsDo getOneByModelId(long id,String mark);

    IPage<HaExpenseRecordsVo> pageRoom(PageQuery recodeQuery);
}
