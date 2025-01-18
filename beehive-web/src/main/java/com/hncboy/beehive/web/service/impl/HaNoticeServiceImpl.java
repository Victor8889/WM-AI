package com.hncboy.beehive.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hncboy.beehive.base.domain.entity.NoticeDo;
import com.hncboy.beehive.base.mapper.NoticeMapper;
import com.hncboy.beehive.web.service.HaNoticeService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author ll
 * @date 2023-7-25
 */
@Service
public class HaNoticeServiceImpl extends ServiceImpl<NoticeMapper, NoticeDo> implements HaNoticeService {

    @Override
    public List<NoticeDo> getList() {
        QueryWrapper<NoticeDo> queryWrapper = new QueryWrapper<>();
        queryWrapper
                .eq("is_enable",1)
                .orderByDesc("id");// orderByAsc(HaProductsDo::getId);
        return this.list(queryWrapper);
    }
}
