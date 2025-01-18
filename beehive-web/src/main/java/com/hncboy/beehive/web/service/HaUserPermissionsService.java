package com.hncboy.beehive.web.service;

import com.github.yulichang.base.MPJBaseService;
import com.hncboy.beehive.base.domain.entity.HaUserPermissionsDo;
import com.hncboy.beehive.web.domain.vo.HaUserInfoQuery;

import java.util.List;

/**
 * @author ll
 * @date 2023-7-25
 */
public interface HaUserPermissionsService extends MPJBaseService<HaUserPermissionsDo>{//IService<HaUserPermissionsDo> {//
    /**
     *
     *
     * @return id
     */
    boolean save(int userid);
    boolean saveOtherId(int userid,int otherUserId);
    /**
     * 参数更新
     *
     * @return id
     */
    boolean update(HaUserPermissionsDo haUserPermissionsDo);

    HaUserPermissionsDo getOne(int userId);
     HaUserInfoQuery getInfo();
    HaUserPermissionsDo getOneByInvite(int inviteCode);
    int getRemainPoints();
    HaUserPermissionsDo getRemainPointsCount();
    boolean isRemainPoints(int points);
    boolean updatePoints(int userId,int points);
    boolean updateFreePointCount(int userId,int points);
    List<HaUserPermissionsDo>  getAllVip();
    List<HaUserPermissionsDo>  getFreeList(int currentDayOfMonth);
    boolean updateFreePoints(int userId,int points);

    boolean updateNoVip(int userId,int noVip);

    boolean updateNoGiving();

}
