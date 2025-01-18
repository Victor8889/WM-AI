package com.hncboy.beehive.base.util;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author ll
 * @date 2023-8-31
 */
@Data
@Component
@ConfigurationProperties(prefix = "zfb-pay")
public class ZfbUtil implements InitializingBean {

    private String serverUrl;

    private String appid;

    private String privateKey;

    private String appCertPath;   //

    private String alipayPublicCertPath;   //

    private String alipayRootCertPath;

    private String notifyUrl;

    private String returnUrl;

    public static String SERVERURL;
    public static String APPID;
    public static String PRIVATEKEY;
    public static String APPCERPATH;
    public static String ALIPAYPUBLICCERTPATH;
    public static String ALIROOTCERTPATH;
    public static String NOTIFYURL;
    public static String RETURNURL;
    @Override
    public void afterPropertiesSet() throws Exception {
        SERVERURL = serverUrl;
        APPID = appid;
        PRIVATEKEY = privateKey;
        APPCERPATH = appCertPath;
        ALIPAYPUBLICCERTPATH = alipayPublicCertPath;
        ALIROOTCERTPATH = alipayRootCertPath;
        NOTIFYURL = notifyUrl;
        RETURNURL = returnUrl;
    }

    public static final String FORMAT = "json";
    public static final String CHARSET = "UTF-8";
    public static final String SIGN_TYPE = "RSA2";

    public static AlipayClient zfbClinet() throws AlipayApiException {
        AlipayConfig alipayConfig = new AlipayConfig();
        alipayConfig.setServerUrl(SERVERURL);
        alipayConfig.setAppId(APPID);
        alipayConfig.setPrivateKey(PRIVATEKEY);
        alipayConfig.setFormat(FORMAT);
        alipayConfig.setCharset(CHARSET);
        alipayConfig.setSignType(SIGN_TYPE);
        alipayConfig.setAppCertPath(APPCERPATH);
        alipayConfig.setAlipayPublicCertPath(ALIPAYPUBLICCERTPATH);
        alipayConfig.setRootCertPath(ALIROOTCERTPATH);
        AlipayClient alipayClient = new DefaultAlipayClient(alipayConfig);

        return alipayClient;
    }

}
