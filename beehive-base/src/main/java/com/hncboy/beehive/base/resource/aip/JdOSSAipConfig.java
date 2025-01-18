package com.hncboy.beehive.base.resource.aip;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author ll
 * @date 2023/6/26
 * JD OSS 工具类
 */
@Slf4j
@Configuration
public class JdOSSAipConfig {

    @Bean
    public AmazonS3 JdAipContentCensor() {
        JdOSSProperties jdOSSProperties = JdOSSUtil.getJdOSSAipProperties();
        log.info("JD OSS 配置初始化：{}", jdOSSProperties);
        // 项目启动时初始化，如果要修改配置，需要重启项目
        ClientConfiguration config = new ClientConfiguration();

        AwsClientBuilder.EndpointConfiguration endpointConfig =
                new AwsClientBuilder.EndpointConfiguration(jdOSSProperties.getEndpoint(), "<REGION>");

        AWSCredentials awsCredentials = new BasicAWSCredentials(jdOSSProperties.getAccessKey(),jdOSSProperties.getSecretKey());//(accessKey,secretKey);
        AWSCredentialsProvider awsCredentialsProvider = new AWSStaticCredentialsProvider(awsCredentials);

        AmazonS3 s3 = AmazonS3Client.builder()
                .withEndpointConfiguration(endpointConfig)
                .withClientConfiguration(config)
                .withCredentials(awsCredentialsProvider)
                .disableChunkedEncoding()
                .build();
        return s3;
    }
}
