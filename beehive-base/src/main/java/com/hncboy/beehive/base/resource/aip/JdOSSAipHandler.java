package com.hncboy.beehive.base.resource.aip;

import cn.hutool.core.lang.Pair;
import cn.hutool.extra.spring.SpringUtil;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.hncboy.beehive.base.enums.CommonEnum;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

/**
 * @author ll
 * @date 2023/6/6
 * jd OSS 处理
 */
@Slf4j
public class JdOSSAipHandler {

     /**
     * 流式上传
     *
     */
     public static Pair<Boolean, String> putStream(String key,InputStream inputStream,
                                                   String contentType,String dir){

        try {
        if (!JdOSSUtil.getJdOSSAipProperties().getEnabled()) {
            return new Pair<>(false, "对象存储被禁用");
        }

        //String bucket_name = "<your bucketname>";
        //String file_path = CommonEnum.mjfile;//"<your path>";
        //String key = Paths.get(file_path).getFileName().toString();

//获取输入流
//        InputStream inputStream = new FileInputStream(file_path);

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(contentType);
        //objectMetadata.setContentLength(contentLength);

//上传文件流
            //s3.putObject(bucket_name, key, inputStream, objectMetadata);
            SpringUtil.getBean(AmazonS3.class).putObject(CommonEnum.bucketname+dir, key, inputStream, objectMetadata);
            System.out.format("Uploading %s to OSS bucket %s...\n", key, CommonEnum.bucketname);
            return new Pair<>(true, "上传出错" + key);
        } catch (AmazonServiceException e) {
            e.printStackTrace();
            return new Pair<>(false, "上传出错" + e.getMessage());
        }

    }

}
