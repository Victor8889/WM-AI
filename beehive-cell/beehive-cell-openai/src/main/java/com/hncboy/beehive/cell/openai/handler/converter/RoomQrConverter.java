package com.hncboy.beehive.cell.openai.handler.converter;

import com.hncboy.beehive.base.domain.entity.HaQrInfoDo;
import com.hncboy.beehive.web.domain.vo.RoomShowHaQrInfoVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author ll
 * @date 2023/5/31
 * OpenAi 对话房间消息相关转换
 */
@Mapper
public interface RoomQrConverter {

    RoomQrConverter INSTANCE = Mappers.getMapper(RoomQrConverter.class);

    /**
     * List<HaQrInfoDo> 转 List<RoomShowHaQrInfoVO>
     *
     * @param roomOpenAiChatMsgDOList List<RoomOpenAiChatMsgDO>
     * @return List<RoomOpenAiChatMsgVO>
     */
    List<RoomShowHaQrInfoVO> entityToVO(List<HaQrInfoDo> roomOpenAiChatMsgDOList);


}
