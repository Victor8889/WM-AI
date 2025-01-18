package com.hncboy.beehive.base.resource.aip;

import lombok.Data;

/**
 * @author ll
 * @date 2023/6/6
 * jd OSS 配置参数
 */
@Data
public class JdOSSProperties {

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * accessKey
     */
    private String accessKey;

    /**
     * secretKey
     */
    private String secretKey;

    /**
     * endpoint
     */
    private String endpoint;
    /**
     * bucketUrl
     */
    private String bucketUrl;
}
