package com.hncboy.beehive.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hncboy.beehive.base.domain.entity.NoticeDo;

import java.util.List;

/**
 * @author ll
 * @date 2023-7-25
 */
public interface HaNoticeService extends IService<NoticeDo> {

    List<NoticeDo> getList();
}
