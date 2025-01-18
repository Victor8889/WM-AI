package com.hncboy.beehive.cell.core.hander;

import cn.hutool.core.util.ObjectUtil;
import com.hncboy.beehive.base.domain.entity.RoomDO;
import com.hncboy.beehive.base.enums.CellCodeEnum;
import com.hncboy.beehive.base.exception.ServiceException;
import com.hncboy.beehive.base.util.FrontUserUtil;
import com.hncboy.beehive.cell.core.cache.RoomCache;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author ll
 * @date 2023/5/29
 * 房间相关处理
 */
public class RoomHandler {

    /**
     * 校验房间是否并返回
     *
     * @param roomId 房间 id
     * @return 房间信息
     */
    public static RoomDO checkRoomExist(Long roomId) {
        // 从缓存获取房间信息
        RoomDO roomDO = RoomCache.getRoom(roomId);
        if (Objects.isNull(roomDO)) {
            throw new ServiceException("房间不存在");
        }
        // 校验房间是否属于当前用户
        if (ObjectUtil.notEqual(roomDO.getUserId(), FrontUserUtil.getUserId())) {
            //throw new ServiceException("房间不存在");
        }
        if (roomDO.getIsDeleted()) {
            throw new ServiceException("房间已删除");
        }
        return roomDO;
    }

    /**
     * 校验房间是否存在并且 cell 可使用
     *
     * @param roomId 房间 id
     * @return 房间信息
     */
    public static RoomDO checkRoomExistAndCellCanUse(Long roomId) {
        RoomDO roomDO = checkRoomExist(roomId);
        CellPermissionHandler.checkCanUse(roomDO.getCellCode());
        return roomDO;
    }

    /**
     * 校验房间是否存在并且图纸可使用
     * 并且房间图纸编码为 limitedCellCodeEnum
     *
     * @param roomId              房间 id
     * @param limitedCellCodeEnum 限制的房间 cell Code
     * @return 房间信息
     */
    public static RoomDO checkRoomExistAndCellCanUse(Long roomId, CellCodeEnum limitedCellCodeEnum) {
        return checkRoomExistAndCellCanUse(roomId, Collections.singletonList(limitedCellCodeEnum));
    }

    /**
     * 校验房间是否存在并且属于指定的 cell code
     *
     * @param roomId               房间 id
     * @param limitedCellCodeEnums 限制的房间 cell Code 列表
     * @return 房间信息
     */
    public static RoomDO checkRoomExistAndCellCanUse(Long roomId, List<CellCodeEnum> limitedCellCodeEnums) {
        RoomDO roomDO = checkRoomExist(roomId);
        // 校验房间 cell code
        if (!limitedCellCodeEnums.contains(roomDO.getCellCode())) {
            throw new ServiceException("房间不存在");
        }
        CellPermissionHandler.checkCanUse(roomDO.getCellCode());
        return roomDO;
    }
    public static Boolean checkRemainPoints(boolean isRemain){
        if(isRemain)
            return true;
        else
            throw new ServiceException("积分不足，请先充值");
    }

    //public static Boolean getRemainPoints(RecordsEnum re){
    //    if(RecordsEnum.lhd.size() == 0)
    //        throw new ServiceException("系统异常，请稍后再试");
    //    for (HaProductsDo phd : RecordsEnum.lhd) {
    //        if (re.getName().equals(phd.getName())) {
    //            return phd.getRecords();
    //        }
    //    }
    //}
}
