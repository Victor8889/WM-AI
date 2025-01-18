package com.hncboy.beehive.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hncboy.beehive.base.constant.HaUserConstant;
import com.hncboy.beehive.base.domain.entity.HaExpenseRecordsDo;
import com.hncboy.beehive.base.domain.entity.HaInviteDo;
import com.hncboy.beehive.base.domain.entity.HaUserParamDo;
import com.hncboy.beehive.base.domain.query.PageQuery;
import com.hncboy.beehive.base.enums.RecordsEnum;
import com.hncboy.beehive.base.mapper.HaInviteMapper;
import com.hncboy.beehive.base.mapper.HaUserParamMapper;
import com.hncboy.beehive.base.util.FrontUserUtil;
import com.hncboy.beehive.base.util.PageUtil;
import com.hncboy.beehive.web.domain.request.HaUserParamRequest;
import com.hncboy.beehive.web.domain.vo.HaExpenseRecordsVo;
import com.hncboy.beehive.web.domain.vo.HaInviteVo;
import com.hncboy.beehive.web.service.HaInviteService;
import com.hncboy.beehive.web.service.HaUserParamService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ll
 * @date 2023-7-25
 */
@Service
public class HaInviteServiceImpl extends ServiceImpl<HaInviteMapper, HaInviteDo> implements HaInviteService {
    @Override
    public boolean saveDo(HaInviteDo haInviteDo) {

        return this.save(haInviteDo);
    }

    @Override
    public boolean update(HaInviteDo haInviteDo) {
        return false;
    }


    @Override
    public HaInviteDo getOne() {
        // 构建查询条件，根据userid查询数据
        QueryWrapper<HaInviteDo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userid", FrontUserUtil.getUserId());

        // 根据条件查询一条数据
        HaInviteDo haUserParamDo = this.getOne(queryWrapper);

        if(haUserParamDo == null)
            return null;
        // 将实体对象转换为请求对象并返回
        HaInviteDo haUserParamRequest = new HaInviteDo();
        BeanUtils.copyProperties(haUserParamDo, haUserParamRequest);
        return haUserParamRequest;
    }

    @Override
    public IPage<HaInviteVo> pageInvite(PageQuery recodeQuery) {
        Page<HaInviteDo> roomPage = page(new Page<>(recodeQuery.getPageNum(), recodeQuery.getPageSize()), new LambdaQueryWrapper<HaInviteDo>()
                // 自己的房间
                .eq(HaInviteDo::getUserId, FrontUserUtil.getUserId())
                // 根据主键降序
                .orderByDesc(HaInviteDo::getId));
        return PageUtil.toPage(roomPage, entityToListVO(roomPage.getRecords()));
    }
    private List<HaInviteVo> entityToListVO(List<HaInviteDo> inviteDos) {
        if ( inviteDos == null ) {
            return null;
        }
        List<HaInviteVo> list = new ArrayList<HaInviteVo>( inviteDos.size() );
        for ( HaInviteDo invite : inviteDos ) {
            list.add( entityToListVO( invite) );
        }

        return list;
    }
    private HaInviteVo entityToListVO(HaInviteDo inviteDo) {
        if ( inviteDo == null ) {
            return null;
        }
        HaInviteVo haInviteVo = new HaInviteVo();
        BeanUtils.copyProperties(inviteDo, haInviteVo);

        return haInviteVo;
    }
}
