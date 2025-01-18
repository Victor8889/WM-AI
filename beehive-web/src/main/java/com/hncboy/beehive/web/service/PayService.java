package com.hncboy.beehive.web.service;

import com.hncboy.beehive.web.domain.vo.PayInfoVo;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

/**
 * @author ll
 * @date 2023-8-31
 */
public interface PayService {
    Map createNative(Long orderId) throws Exception;
    PayInfoVo nativeCodeUrl(int amount, int points);

    PayInfoVo zfbCodeUrl(int amount, int points)throws Exception;
    PayInfoVo zfbMobileCodeUrl(int amount, int points)throws Exception;

    PayInfoVo zfbVipCodeUrl(int amount, int points)throws Exception;
    PayInfoVo zfbVipMobileCodeUrl(int amount, int points)throws Exception;

    Boolean zfbCallBack(HttpServletRequest request);
    Boolean zfbStatus(String orderId,int id,int type,int points);
}
