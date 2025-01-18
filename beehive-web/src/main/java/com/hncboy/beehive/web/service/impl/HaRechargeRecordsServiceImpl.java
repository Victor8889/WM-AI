package com.hncboy.beehive.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hncboy.beehive.base.domain.entity.HaRechargeRecordsDo;
import com.hncboy.beehive.base.domain.query.PageQuery;
import com.hncboy.beehive.base.enums.PayStatusEnum;
import com.hncboy.beehive.base.enums.PayTypeEnum;
import com.hncboy.beehive.base.mapper.HaRechargeRecordsMapper;
import com.hncboy.beehive.base.util.FrontUserUtil;
import com.hncboy.beehive.base.util.PageUtil;
import com.hncboy.beehive.web.domain.vo.HaRechargeRecordsVo;
import com.hncboy.beehive.web.service.HaInviteService;
import com.hncboy.beehive.web.service.HaRechargeRecordsService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ll
 * @date 2023-8-29
 */
@Service
public class HaRechargeRecordsServiceImpl extends ServiceImpl<HaRechargeRecordsMapper, HaRechargeRecordsDo> implements HaRechargeRecordsService {
    @Resource
    private com.hncboy.beehive.web.service.HaUserPermissionsService haUserPermissionsService;

    @Resource
    private HaInviteService haInviteService;

    @Override
    public int save(int amount,int points,PayTypeEnum type,String orderId,String mark,String payMark) {

       HaRechargeRecordsDo haRechargeRecordsDo = new HaRechargeRecordsDo();
        haRechargeRecordsDo.setPoints(points);
        haRechargeRecordsDo.setStatus(PayStatusEnum.INIT);
        haRechargeRecordsDo.setAmount(amount);
        haRechargeRecordsDo.setMark(mark);//amount + "元" + points + "积分"
        haRechargeRecordsDo.setUserId(FrontUserUtil.getUserId());
        haRechargeRecordsDo.setType(type);
        haRechargeRecordsDo.setOrderId(orderId);
        haRechargeRecordsDo.setPayMark(payMark);
        this.save(haRechargeRecordsDo);

        return haRechargeRecordsDo.getId();
    }

    @Override
    public boolean updateById(HaRechargeRecordsDo haRechargeRecordsDo) {
        // 构建更新条件，根据实体对象的ID更新数据
        UpdateWrapper<HaRechargeRecordsDo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", haRechargeRecordsDo.getId());

        // 执行更新操作
        return this.update(haRechargeRecordsDo, updateWrapper);
    }

    @Override
    public boolean updateByOrderId(HaRechargeRecordsDo haRechargeRecordsDo) {
        UpdateWrapper<HaRechargeRecordsDo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("user_id", FrontUserUtil.getUserId());
        updateWrapper.eq("order_id", haRechargeRecordsDo.getOrderId());

        // 执行更新操作
        return this.update(haRechargeRecordsDo, updateWrapper);
    }

    private boolean wxRecharge(){

        return true;
    }

    private boolean zfbRecharge(){

        return true;
    }

    public boolean update(HaRechargeRecordsDo haRechargeRecordsDo) {
        // 构建更新条件，根据实体对象的ID更新数据
        UpdateWrapper<HaRechargeRecordsDo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", haRechargeRecordsDo.getId());

        // 执行更新操作
        return this.update(haRechargeRecordsDo, updateWrapper);
    }

    public boolean update(int id, int status) {
        HaRechargeRecordsDo haRechargeRecordsDo = new HaRechargeRecordsDo();
        haRechargeRecordsDo.setStatus(PayStatusEnum.SUCCESS);

        // 构建更新条件，根据实体对象的ID更新数据
        UpdateWrapper<HaRechargeRecordsDo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id);

        // 执行更新操作
        return this.update(haRechargeRecordsDo, updateWrapper);
    }


    @Override
    public HaRechargeRecordsDo getOne() {
        return null;
    }

    @Override
    public HaRechargeRecordsDo getOne(int id) {
        // 构建更新条件，根据实体对象的ID更新数据
        UpdateWrapper<HaRechargeRecordsDo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id);
        return this.getOne(updateWrapper);
    }

    public IPage<HaRechargeRecordsVo> pageRecharge(PageQuery recodeQuery) {
        Page<HaRechargeRecordsDo> rechargePage = page(new Page<>(recodeQuery.getPageNum(), recodeQuery.getPageSize()), new LambdaQueryWrapper<HaRechargeRecordsDo>()
                // 自己的
                .eq(HaRechargeRecordsDo::getUserId, FrontUserUtil.getUserId())
                // 根据主键降序
                .orderByDesc(HaRechargeRecordsDo::getId));
        return PageUtil.toPage(rechargePage, entityToListVO(rechargePage.getRecords()));
    }

    @Override
    public List<HaRechargeRecordsDo> getPermissionList() {
        UpdateWrapper<HaRechargeRecordsDo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("is_Add_Points", 0);
        updateWrapper.eq("status", PayStatusEnum.SUCCESS);
        return this.list(updateWrapper);
    }

    @Override
    public List<HaRechargeRecordsDo> getNotPayList(PayTypeEnum type) {
        UpdateWrapper<HaRechargeRecordsDo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("type", type.getCode());
        updateWrapper.eq("status", PayStatusEnum.INIT);
        updateWrapper.lt("create_time", LocalDateTime.now().minus(30, ChronoUnit.MINUTES));
        return this.list(updateWrapper);
    }
    @Override
    public List<HaRechargeRecordsDo> getMobileNotPayList(PayTypeEnum type) {
        UpdateWrapper<HaRechargeRecordsDo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("type", type.getCode());
        updateWrapper.eq("status", PayStatusEnum.INIT);
        updateWrapper.gt("create_time", LocalDateTime.now().minus(30, ChronoUnit.MINUTES));
        return this.list(updateWrapper);
    }

    private List<HaRechargeRecordsVo> entityToListVO(List<HaRechargeRecordsDo> recordsDos) {
        if ( recordsDos == null ) {
            return null;
        }

        List<HaRechargeRecordsVo> list = new ArrayList<HaRechargeRecordsVo>( recordsDos.size() );
        for ( HaRechargeRecordsDo records : recordsDos ) {
            list.add( entityToListVO( records ) );
        }

        return list;
    }
    private HaRechargeRecordsVo entityToListVO(HaRechargeRecordsDo recordsDos) {
        if ( recordsDos == null ) {
            return null;
        }
        HaRechargeRecordsVo recodrsistVO = new HaRechargeRecordsVo();
        BeanUtils.copyProperties(recordsDos, recodrsistVO);
        PayStatusEnum status = recordsDos.getStatus();
        if(0 == status.getCode())
            recodrsistVO.setStatus("未支付");
        else if(1 == status.getCode())
            recodrsistVO.setStatus("支付成功");
        else if(2 == status.getCode())
            recodrsistVO.setStatus("支付失败");
        else if(3 == status.getCode())
            recodrsistVO.setStatus("支付异常");
        else if(4 == status.getCode())
            recodrsistVO.setStatus("支付关闭");
        else if(7 == status.getCode())
            recodrsistVO.setStatus("无需支付");
        else if(6 == status.getCode())
            recodrsistVO.setStatus("退还");
        return recodrsistVO;
    }
}
