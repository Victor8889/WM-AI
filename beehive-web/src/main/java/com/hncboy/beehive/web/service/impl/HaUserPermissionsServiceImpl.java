package com.hncboy.beehive.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.query.MPJQueryWrapper;
import com.hncboy.beehive.base.constant.HaUserConstant;
import com.hncboy.beehive.base.domain.entity.HaUserPermissionsDo;
import com.hncboy.beehive.base.enums.CommonEnum;
import com.hncboy.beehive.base.mapper.HaUserPermissionsMapper;
import com.hncboy.beehive.base.util.FrontUserUtil;
import com.hncboy.beehive.web.domain.vo.HaUserInfoQuery;
import com.hncboy.beehive.web.service.HaUserPermissionsService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * @author ll
 * @date 2023-7-25
 */
@Service
@AllArgsConstructor
public class HaUserPermissionsServiceImpl extends MPJBaseServiceImpl<HaUserPermissionsMapper,HaUserPermissionsDo> implements HaUserPermissionsService {
    @Autowired
    private HaUserPermissionsMapper haUserPermissionsMapper;
    @Override
    public boolean save(int userid) {
        HaUserPermissionsDo haUserPermissionsDo = HaUserConstant.setPermissionDefaultValues(userid);

        return this.save(haUserPermissionsDo);
    }
    @Override
    public boolean saveOtherId(int userid,int otherUserId) {
        HaUserPermissionsDo haUserPermissionsDo = HaUserConstant.setPermissionDefaultValues(userid);
        haUserPermissionsDo.setOtherUserId(otherUserId);
        return this.save(haUserPermissionsDo);
    }

    @Override
    public boolean update(HaUserPermissionsDo haUserPermissionsDo) {
        // 构建更新条件，根据实体对象的ID更新数据
        UpdateWrapper<HaUserPermissionsDo> updateWrapper = new UpdateWrapper<>();
        try {

            if (null != haUserPermissionsDo.getId() && haUserPermissionsDo.getId() != 0)
                updateWrapper.eq("id", haUserPermissionsDo.getId());
            if (null != haUserPermissionsDo.getUserId() && haUserPermissionsDo.getUserId() != 0)
                updateWrapper.eq("user_id", haUserPermissionsDo.getUserId());
            // 执行更新操作
            return this.update(haUserPermissionsDo, updateWrapper);
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updatePoints(int userId,int points) {
        // 构建更新条件，根据实体对象的ID更新数据
        UpdateWrapper<HaUserPermissionsDo> updateWrapper = new UpdateWrapper<>();
        try {
            //updateWrapper.set("is_")
            updateWrapper.setSql("remain_hold_bi = remain_hold_bi + " + points);
            updateWrapper.eq("user_id", userId);
            return this.update(null, updateWrapper);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public boolean updateFreePointCount(int userId,int points) {
        // 构建更新条件，根据实体对象的ID更新数据
        UpdateWrapper<HaUserPermissionsDo> updateWrapper = new UpdateWrapper<>();
        try {
            //updateWrapper.set("is_")
            updateWrapper.setSql("remain_hold_count = remain_hold_count + " + points);
            updateWrapper.eq("user_id", userId);
            return this.update(null, updateWrapper);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public boolean updateFreePoints(int userId,int points) {
        // 构建更新条件，根据实体对象的ID更新数据
        UpdateWrapper<HaUserPermissionsDo> updateWrapper = new UpdateWrapper<>();
        try {
            //updateWrapper.set("is_")
            updateWrapper.setSql("remain_hold_count = " + points)
                    .setSql("is_giving = " + CommonEnum.isGiving);
            updateWrapper.eq("user_id", userId);
            return this.update(null, updateWrapper);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public boolean updateNoGiving(){
        // 构建更新条件，根据实体对象的ID更新数据
        UpdateWrapper<HaUserPermissionsDo> updateWrapper = new UpdateWrapper<>();
        try {
            //updateWrapper.set("is_")
            updateWrapper.setSql("is_giving = " + CommonEnum.noGiving);
            updateWrapper.eq("is_vip", CommonEnum.isVip);
            return this.update(null, updateWrapper);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateNoVip(int userId,int noVip) {
        // 构建更新条件，根据实体对象的ID更新数据
        UpdateWrapper<HaUserPermissionsDo> updateWrapper = new UpdateWrapper<>();
        try {
            //updateWrapper.set("is_")
            updateWrapper.setSql("is_vip =" + noVip);
            updateWrapper.setSql("vip_time = null");
            updateWrapper.eq("user_id", userId);
            return this.update(null, updateWrapper);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public HaUserPermissionsDo getOne(int userId) {
        QueryWrapper<HaUserPermissionsDo> queryWrapper = new QueryWrapper<>();
        if(userId > 0)
            queryWrapper.eq("user_id",userId);
        else
            queryWrapper.eq("user_id", FrontUserUtil.getUserId());

        // 根据条件查询一条数据
        HaUserPermissionsDo haUserParamDo = this.getOne(queryWrapper);
        return haUserParamDo;
    }
    @Override
    public HaUserPermissionsDo getOneByInvite(int inviteCode) {
        QueryWrapper<HaUserPermissionsDo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("invite_encode",inviteCode);

        // 根据条件查询一条数据
        HaUserPermissionsDo haUserParamDo = this.getOne(queryWrapper);
        return haUserParamDo;
    }

    @Override
    public List<HaUserPermissionsDo>  getAllVip() {
        QueryWrapper<HaUserPermissionsDo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_vip", CommonEnum.isVip);
        return this.list(queryWrapper);

    }

    @Override
    public List<HaUserPermissionsDo>  getFreeList(int currentDayOfMonth) {
        QueryWrapper<HaUserPermissionsDo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_vip", CommonEnum.isVip)
                .eq("is_giving",CommonEnum.noGiving)
                .apply("DAY(vip_time) = " + currentDayOfMonth);
        return this.list(queryWrapper);

    }

    @Override
    public int getRemainPoints() {
        QueryWrapper<HaUserPermissionsDo> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("remain_hold_bi");
            queryWrapper.eq("user_id", FrontUserUtil.getUserId());

        // 根据条件查询一条数据
        HaUserPermissionsDo haUserParamDo = this.getOne(queryWrapper, false);
        if (haUserParamDo != null) {
            return haUserParamDo.getRemainHoldBi();
        } else {
            return 0;   // 如果没有查询到结果，默认返回0或其他合适的值
        }
        //return haUserParamDo.getRemainHoldBi();
    }

    @Override
    public HaUserPermissionsDo getRemainPointsCount(){
        QueryWrapper<HaUserPermissionsDo> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("remain_hold_bi,remain_hold_count,is_vip,is_enable,user_id");
        queryWrapper.eq("user_id", FrontUserUtil.getUserId());

        // 根据条件查询一条数据
        HaUserPermissionsDo haUserParamDo = this.getOne(queryWrapper, false);
        if (haUserParamDo != null) {
            return haUserParamDo;
        } else {
            haUserParamDo.setRemainHoldCount(0);
            haUserParamDo.setRemainHoldBi(0);
            return haUserParamDo;   // 如果没有查询到结果，默认返回0或其他合适的值
        }
        //return haUserParamDo.getRemainHoldBi();
    }

    @Override
    public boolean isRemainPoints(int points) {
        if(points <= getRemainPoints())
            return true;
        else
            return false;
    }

    //HaUserInfoQuery
    @Override
    public HaUserInfoQuery getInfo() {

        HaUserInfoQuery hul = haUserPermissionsMapper.selectJoinOne(HaUserInfoQuery.class,
                new MPJQueryWrapper<HaUserPermissionsDo>()
                        .select("t.remain_hold_bi as remainHoldBi,t.invite_encode as inviteEncode,t.validy_date as validyDate,t.is_vip as isVip,t.remain_hold_count as remainHoldCount ")
                        //.select("t.invite_encode as inviteEncode")
                        .select("em.username as userName")
                        .leftJoin("bh_front_user_extra_binding b on t.user_id = b.base_user_id")
                        .leftJoin("bh_front_user_extra_email em on em.id = b.extra_info_id")
                //.select(HaUserPermissionsDo::getRemainHoldBi,HaUserPermissionsDo::getInviteEncode)
                //.select(EmailVerifyCodeDO::getToEmailAddress)
                //.leftJoin(EmailVerifyCodeDO.class, EmailVerifyCodeDO::getId,HaUserPermissionsDo::getUserId)
                .eq("t.user_id", FrontUserUtil.getUserId())
        );


        return hul;
    }

}
