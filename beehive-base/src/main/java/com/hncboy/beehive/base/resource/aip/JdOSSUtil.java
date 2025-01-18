package com.hncboy.beehive.base.resource.aip;

import com.hncboy.beehive.base.cache.SysParamCache;
import com.hncboy.beehive.base.enums.SysParamKeyEnum;
import com.hncboy.beehive.base.util.ObjectMapperUtil;
import lombok.experimental.UtilityClass;

/**
 * @author ll
 * @date 2023/6/26
 * JD OSS 工具类
 */
@UtilityClass
public class JdOSSUtil {

    /**
     * 获取 JDOSSProperties
     *
     * @return JDOSSProperties
     */
    public JdOSSProperties getJdOSSAipProperties() {
        String jdOSSAipConfigStr = SysParamCache.get(SysParamKeyEnum.JDOSS_AIP);
        return ObjectMapperUtil.fromJson(jdOSSAipConfigStr, JdOSSProperties.class);
    }
}
