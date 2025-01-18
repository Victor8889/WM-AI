package com.hncboy.beehive.web.service;

import com.hncboy.beehive.base.domain.entity.HaQrInfoDo;
import com.hncboy.beehive.base.domain.query.RoomQrQuery;
import com.hncboy.beehive.base.handler.mp.IBeehiveService;
import com.hncboy.beehive.web.domain.vo.RoomHaQrInfoVO;
import com.hncboy.beehive.web.domain.vo.RoomShowHaQrInfoVO;

import java.util.List;

/**
 * @author ll
 * @date 2023/7/31
 * OpenAi 对话房间消息业务接口
 */
public interface RoomQrService extends IBeehiveService<HaQrInfoDo> {

    /**
     * 查询消息列表
     *
     * @param cursorQuery 请求参数
     * @return 消息列表
     */
    List<RoomShowHaQrInfoVO> list(RoomQrQuery cursorQuery);

    /**
     * 发送消息
     *
     * @param sendRequest 请求参数
     * @return 响应参数
     */
    boolean send(RoomHaQrInfoVO sendRequest);
    List<HaQrInfoDo> getNotComQrList(int isCompleted);
    /**
     * 删除消息
     *
     * @return 响应参数
     */
    Boolean delete(String id);
    List<HaQrInfoDo> getComQrList();
    boolean update(HaQrInfoDo haQrInfoDo);
}
