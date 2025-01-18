package com.hncboy.beehive.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.yulichang.base.MPJBaseMapper;
import com.hncboy.beehive.base.domain.entity.HaUserPermissionsDo;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author ll
 * @date 2023-7-24
 */

@Mapper
public interface HaUserPermissionsMapper extends MPJBaseMapper<HaUserPermissionsDo> {
}
