package com.hncboy.beehive.cell.midjourney.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hncboy.beehive.base.domain.entity.RoomMidjourneyMsgDO;
import com.hncboy.beehive.base.domain.query.MjPageQuery;
import com.hncboy.beehive.base.handler.mp.IBeehiveService;
import com.hncboy.beehive.cell.midjourney.domain.request.MjConvertRequest;
import com.hncboy.beehive.cell.midjourney.domain.request.MjDescribeRequest;
import com.hncboy.beehive.cell.midjourney.domain.request.MjImagineRequest;
import com.hncboy.beehive.cell.midjourney.domain.vo.RoomMidjourneyMsgVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author ll
 * @date 2023/5/18
 * Midjourney 房间消息业务接口
 */
public interface RoomMidjourneyMsgService extends IBeehiveService<RoomMidjourneyMsgDO> {

    /**
     * 查询消息列表
     *
     * @return 消息列表
     */
    List<RoomMidjourneyMsgVO> listMj(Integer mjType);//RoomMsgCursorQuery cursorQuery
    IPage<RoomMidjourneyMsgVO> pageMj(MjPageQuery mjPageQuery);
    /**
     * 查询消息详情
     *
     * @param msgId 消息 id
     * @return 消息详情
     */
    RoomMidjourneyMsgVO detail(Long msgId);
    /**
     * 查询消息详情
     *
     * @param msgId 消息 id
     * @return 消息详情
     */
    Boolean delete(Long msgId);
    /**
     * 根据描述创建图像
     *
     * @param imagineRequest 请求参数
     */
    void imagine(MjImagineRequest imagineRequest);

    /**
     * 根据描述创建图像
     *
     * @param imagineRequest 请求参数
     */
    RoomMidjourneyMsgVO image(MjImagineRequest imagineRequest);

    /**
     * u 转换
     *
     * @param convertRequest 请求参数
     */
    void upscale(MjConvertRequest convertRequest);

    /**
     * v 转换
     *
     * @param convertRequest 请求参数
     */
    void variation(MjConvertRequest convertRequest);

    /**
     * 关联动作
     *
     * @param convertRequest 请求参数
     */
    void cpzClick(MjConvertRequest convertRequest);
    public void blendMj(MjConvertRequest convertRequest);

    /**
     * 关联动作
     *  局部重绘
     * @param convertRequest 请求参数   modalImg
     */
    void modalImg(MjConvertRequest convertRequest);
    /**
     * 根据图片生成描述
     *
     * @param describeRequest 请求参数
     */
    void describe(MjDescribeRequest describeRequest);


    /**
     * 上传图片
     *
     * @param file 请求参数
     */
    String upload(MultipartFile file,String type);
    List<RoomMidjourneyMsgDO> getNoPayMjList();
    List<RoomMidjourneyMsgDO> getComMjList();
    boolean update(RoomMidjourneyMsgDO roomMidjourneyMsgDO);
    List<RoomMidjourneyMsgDO> getMjList(int status,int mjType);;
    List<RoomMidjourneyMsgDO> getRuntMjList(Integer mjType);;
    List<RoomMidjourneyMsgDO> getTimeOutMjList();
}
