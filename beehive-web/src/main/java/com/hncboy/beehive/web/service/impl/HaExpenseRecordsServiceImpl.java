package com.hncboy.beehive.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hncboy.beehive.base.domain.entity.HaExpenseRecordsDo;
import com.hncboy.beehive.base.domain.query.PageQuery;
import com.hncboy.beehive.base.mapper.HaExpenseRecordsMapper;
import com.hncboy.beehive.base.util.FrontUserUtil;
import com.hncboy.beehive.base.util.PageUtil;
import com.hncboy.beehive.web.domain.vo.HaExpenseRecordsVo;
import com.hncboy.beehive.web.service.HaExpenseRecordsService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ll
 * @date 2023-7-25
 */
@Service
public class HaExpenseRecordsServiceImpl extends ServiceImpl<HaExpenseRecordsMapper, HaExpenseRecordsDo> implements HaExpenseRecordsService {

    @Override
    public boolean save(HaExpenseRecordsDo haExpenseRecordsDo) {
        return super.save(haExpenseRecordsDo);
    }

    @Override
    public boolean update(HaExpenseRecordsDo haExpenseRecordsDo) {
        return false;
    }

    @Override
    public HaExpenseRecordsDo getOne() {
        return null;
    }
    @Override
    public HaExpenseRecordsDo getOneByModelId(long id,String mark) {
        HaExpenseRecordsDo record = getOne(new LambdaQueryWrapper<HaExpenseRecordsDo>()
                .eq(HaExpenseRecordsDo::getModelId, id));
                //.eq(HaExpenseRecordsDo::getMark, mark));
        return record;
    }

    @Override
    public IPage<HaExpenseRecordsVo> pageRoom(PageQuery recodeQuery) {
        Page<HaExpenseRecordsDo> roomPage = page(new Page<>(recodeQuery.getPageNum(), recodeQuery.getPageSize()), new LambdaQueryWrapper<HaExpenseRecordsDo>()
                // 自己的房间
                .eq(HaExpenseRecordsDo::getUserId, FrontUserUtil.getUserId())
                // 根据主键降序
                .orderByDesc(HaExpenseRecordsDo::getId));
        return PageUtil.toPage(roomPage, entityToListVO(roomPage.getRecords()));
    }
    private List<HaExpenseRecordsVo> entityToListVO(List<HaExpenseRecordsDo> recordsDos) {
        if ( recordsDos == null ) {
            return null;
        }

        List<HaExpenseRecordsVo> list = new ArrayList<HaExpenseRecordsVo>( recordsDos.size() );
        for ( HaExpenseRecordsDo records : recordsDos ) {
            list.add( entityToListVO( records ) );
        }

        return list;
    }
    private HaExpenseRecordsVo entityToListVO(HaExpenseRecordsDo recordsDos) {
        if ( recordsDos == null ) {
            return null;
        }
        HaExpenseRecordsVo recodrsistVO = new HaExpenseRecordsVo();
        recodrsistVO.setId(recordsDos.getId());
        recodrsistVO.setHoldBi(recordsDos.getHoldBi());
        recodrsistVO.setModel(recordsDos.getModel());
        recodrsistVO.setCreateTime(recordsDos.getCreateTime());
        if(recordsDos.getMark().contains("GEMINI"))
            recodrsistVO.setMark("万码AI对话GEMINI");
        else
            recodrsistVO.setMark(recordsDos.getMark().replace("CHAT","万码AI对话"));


        return recodrsistVO;
    }

}
