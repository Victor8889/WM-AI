package com.hncboy.beehive.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.hncboy.beehive.base.domain.entity.HaQrInfoDo;
import com.hncboy.beehive.base.domain.entity.RoomOpenAiChatMsgDO;
import com.hncboy.beehive.base.domain.query.RoomQrQuery;
import com.hncboy.beehive.base.enums.PayStatusEnum;
import com.hncboy.beehive.base.enums.RecordsEnum;
import com.hncboy.beehive.base.enums.RoomOpenAiChatMsgStatusEnum;
import com.hncboy.beehive.base.exception.ServiceException;
import com.hncboy.beehive.base.handler.mp.BeehiveServiceImpl;
import com.hncboy.beehive.base.mapper.HaQrInfoMapper;
import com.hncboy.beehive.base.util.FrontUserUtil;
import com.hncboy.beehive.web.domain.vo.RoomHaQrInfoVO;
import com.hncboy.beehive.web.domain.vo.RoomShowHaQrInfoVO;
import com.hncboy.beehive.web.service.RoomQrService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ll·
 * @date 2023/7/31
 * OpenAi 对话房间消息业务实现类
 */
@Slf4j
@Service
public class RoomQrServiceImpl extends BeehiveServiceImpl<HaQrInfoMapper, HaQrInfoDo> implements RoomQrService {

    @Resource
    private com.hncboy.beehive.web.service.HaUserPermissionsService haUserPermissionsService;

    @Override
    public List<RoomShowHaQrInfoVO> list(RoomQrQuery cursorQuery) {
        List<HaQrInfoDo> cursorList = cursorQrList(cursorQuery, HaQrInfoDo::getId, new LambdaQueryWrapper<HaQrInfoDo>()
                .eq(HaQrInfoDo::getUserId, FrontUserUtil.getUserId()));
        return entityToVO(cursorList);
    }
    private SFunction<RoomOpenAiChatMsgDO, RoomOpenAiChatMsgStatusEnum> getStatusColumn() {
        return RoomOpenAiChatMsgDO::getStatus;
    }
    @Override
    public boolean send(RoomHaQrInfoVO sendRequest) {
        //判断余额是否充足
        if(!haUserPermissionsService.isRemainPoints(RecordsEnum.getPointsByName(RecordsEnum.IMG_QR)))
            throw new ServiceException("余额不足，请先充值");
        List<HaQrInfoDo> hl = getQrList(0);
        if(null != hl && hl.size() >=2)
            throw new ServiceException("未开始制作的二维码数量过多，请稍后再试。");
        HaQrInfoDo haQrInfoDo = new HaQrInfoDo();
        // 将前端传递的参数拷贝到实体对象中
        BeanUtils.copyProperties(sendRequest, haQrInfoDo);
        haQrInfoDo.setUserId(FrontUserUtil.getUserId());
        haQrInfoDo.setCode(0);//等待
        haQrInfoDo.setIsCompleted(0);//等待
        haQrInfoDo.setStatus(0);//未删除

        return this.save(haQrInfoDo);
    }
    public List<HaQrInfoDo> getQrList(int isCompleted){
        // 构建查询条件，根据userid查询数据
        QueryWrapper<HaQrInfoDo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", FrontUserUtil.getUserId());
        if(isCompleted > -2)
            queryWrapper.eq("is_completed", isCompleted);
        // 根据条件查询一条数据
        return this.list(queryWrapper);

    }
    public List<HaQrInfoDo> getNotComQrList(int isCompleted){
        // 构建查询条件，根据userid查询数据
        QueryWrapper<HaQrInfoDo> queryWrapper = new QueryWrapper<>();
        if(isCompleted > -2)
            queryWrapper.eq("is_completed", isCompleted);
        // 根据条件查询一条数据
        return this.list(queryWrapper);

    }

    public List<HaQrInfoDo> getComQrList(){
        // 构建查询条件，根据userid查询数据
        QueryWrapper<HaQrInfoDo> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("user_id,id");

        queryWrapper.gt("create_time", LocalDateTime.now().minus(1, ChronoUnit.HOURS));
        queryWrapper.eq("is_completed", PayStatusEnum.SUCCESS.getCode());
        queryWrapper.eq("is_deducted", PayStatusEnum.INIT.getCode());
        // 根据条件查询一条数据
        return this.list(queryWrapper);

    }

    @Override
    public Boolean delete(String id){
        RoomOpenAiChatMsgDO newChat = new RoomOpenAiChatMsgDO();
        //newChat.setId((long)id);
        //newChat.setUserId(FrontUserUtil.getUserId());
        newChat.setStatus(RoomOpenAiChatMsgStatusEnum.DELETE_CHAT);//逻辑删除
        UpdateWrapper<RoomOpenAiChatMsgDO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id);
        updateWrapper.eq("user_id", FrontUserUtil.getUserId());
        return true;//this.update(newChat,updateWrapper);
    }
    public boolean update(HaQrInfoDo haQrInfoDo){

        return this.updateById(haQrInfoDo);
    }

    /**
     * 转换
     */
    private List<RoomShowHaQrInfoVO> entityToVO(List<HaQrInfoDo> roomOpenAiChatMsgDOList) {
        if ( roomOpenAiChatMsgDOList == null ) {
            return null;
        }

        List<RoomShowHaQrInfoVO> list = new ArrayList<RoomShowHaQrInfoVO>( roomOpenAiChatMsgDOList.size() );
        for ( HaQrInfoDo haQrInfoDo : roomOpenAiChatMsgDOList ) {
            list.add( haQrInfoDoToRoomShowHaQrInfoVO( haQrInfoDo ) );
        }

        return list;
    }

    protected RoomShowHaQrInfoVO haQrInfoDoToRoomShowHaQrInfoVO(HaQrInfoDo haQrInfoDo) {
        if ( haQrInfoDo == null ) {
            return null;
        }

        RoomShowHaQrInfoVO roomShowHaQrInfoVO = new RoomShowHaQrInfoVO();

        roomShowHaQrInfoVO.setId( String.valueOf( haQrInfoDo.getId() ) );
        roomShowHaQrInfoVO.setCreateTime( haQrInfoDo.getCreateTime() );
        roomShowHaQrInfoVO.setDescription( haQrInfoDo.getDescription() );
        roomShowHaQrInfoVO.setQrUrl( haQrInfoDo.getQrUrl() );
        if(haQrInfoDo.getIsCompleted() == 0)
            roomShowHaQrInfoVO.setIsCompleted("等待");
        if(haQrInfoDo.getIsCompleted() == 1)
            roomShowHaQrInfoVO.setIsCompleted("完成");
        if(haQrInfoDo.getIsCompleted() == 2)
            roomShowHaQrInfoVO.setIsCompleted("失败");
        if(haQrInfoDo.getIsCompleted() == 3)
            roomShowHaQrInfoVO.setIsCompleted("关闭");
        if(haQrInfoDo.getIsCompleted() == 4)
            roomShowHaQrInfoVO.setIsCompleted("资源受限，联系管理员");
        if(haQrInfoDo.getIsCompleted() == 5)
            roomShowHaQrInfoVO.setIsCompleted("进行中");

        return roomShowHaQrInfoVO;
    }
}
