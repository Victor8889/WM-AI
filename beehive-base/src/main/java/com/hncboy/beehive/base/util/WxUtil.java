package com.hncboy.beehive.base.util;

import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author ll
 * @date 2023-8-31
 */
@Data
@Component
@ConfigurationProperties(prefix = "wx-pay")
public class WxUtil  implements InitializingBean {

    private String privateKeyPath;

    private String merchantSerialNumber;

    private String apiV3Key;


    private String appid;   //


    private String mchid;   //

    private String naviteNotifyUrl;

    public static String NAVITENOTIFYURL;

    public static String MCHID;
    public static String APPID;

    public static String PRIVATEKEYPATH;
    public static String MERCHANTSERIALNUMBER;
    public static String APIV3KEY;
    @Override
    public void afterPropertiesSet() throws Exception {
        PRIVATEKEYPATH = privateKeyPath;
        MERCHANTSERIALNUMBER = merchantSerialNumber;
        APIV3KEY = apiV3Key;
        MCHID = mchid;
        APPID = appid;
        NAVITENOTIFYURL = naviteNotifyUrl;
    }
    public static Config wxConfig(){
        Config config = new RSAAutoCertificateConfig.Builder()
                .merchantId(MCHID)
                .privateKeyFromPath(PRIVATEKEYPATH)
                .merchantSerialNumber(MERCHANTSERIALNUMBER)
                .apiV3Key(APIV3KEY)
                .build();
        return config;
    }
}
