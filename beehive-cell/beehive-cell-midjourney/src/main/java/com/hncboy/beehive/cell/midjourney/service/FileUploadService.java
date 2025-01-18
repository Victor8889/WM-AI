package com.hncboy.beehive.cell.midjourney.service;


import cn.hutool.core.lang.Pair;
import cn.hutool.extra.spring.SpringUtil;
import com.amazonaws.services.s3.AmazonS3;
import com.hncboy.beehive.base.domain.entity.RoomMidjourneyMsgDO;
import com.hncboy.beehive.base.enums.CommonEnum;
import com.hncboy.beehive.base.resource.aip.JdOSSAipHandler;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author ll
 * @date 2024-2-3
 * 线程池上传文件到OSS
 */
@Service
@Component
public class FileUploadService {

    @Resource
    private RoomMidjourneyMsgService midjourneyMsgService;
    // 线程池大小
    private static final int THREAD_POOL_SIZE = 9;

    // 创建线程池和OSS客户端
    private ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    // 提交上传任务
    public void submitUploadTasks(long id,String fileAddresses) {
        //for (String fileAddress : fileAddresses) {
            executorService.submit(() -> {
                uploadFileToOSS(id,fileAddresses);
            });
        //}
    }

    // 上传文件到OSS
    private void uploadFileToOSS(long id,String fileAddress) {
        try {
            String key = "";
            System.out.println(new Date() + "文件上传：start" );
            InputStream inputStream = null;// new FileInputStream(fileAddress);
            if(fileAddress.startsWith("http")){
                URL url = new URL(fileAddress);
                inputStream = url.openStream();
                key = extractFileName(url);

            }else {
                inputStream = new FileInputStream(fileAddress);
                File file = new File(fileAddress);
                key = file.getName();
            }
            String fileType = CommonEnum.mjfile;
            key = ""+id;
            System.out.println(new Date() + "文件上传成功：start" );
            //SpringUtil.getBean(AmazonS3.class).putObject(CommonEnum.bucketname, key, inputStream, objectMetadata);
            Pair<Boolean, String> pb = JdOSSAipHandler.putStream( key,   inputStream, CommonEnum.pictureType, fileType);
            //ossClient.putObject(BUCKET_NAME, generateOSSKey(fileAddress), new File(fileAddress));
            if(pb.getKey()){
                RoomMidjourneyMsgDO rj = new RoomMidjourneyMsgDO();
                rj.setId(id);
                rj.setCompressedImageName(CommonEnum.mjfile+"/"+key);
                //rj.setOriginalImageName(CommonEnum.mjfile+"/"+key);
                midjourneyMsgService.update(rj);
            }
            // 文件上传成功的处理逻辑
            System.out.println(new Date() + "文件上传成功：" + fileAddress);
        } catch (Exception e) {
            // 文件上传失败的处理逻辑
            System.out.println("文件上传失败：" + fileAddress);
            e.printStackTrace();
        }
    }
    private static String extractFileName(URL url) {
        String fileFullName = url.getFile();
        int indexOfLastSlash = fileFullName.lastIndexOf('/');
        if (indexOfLastSlash != -1 && indexOfLastSlash < fileFullName.length() - 1) {
            return fileFullName.substring(indexOfLastSlash + 1);
        }
        return "";
    }
    // 文件回源到OSS
    private void getUploadFileToOSS(String fileAddress) {
        try {
            // 实际的文件上传逻辑，使用您的OSS SDK实现
            // 示例代码使用的是阿里云OSS SDK
            String file_path = CommonEnum.mjfile;//"<your path>";
            String key = Paths.get(file_path).getFileName().toString();

//获取输入流
            InputStream inputStream = new FileInputStream(file_path);

            //SpringUtil.getBean(AmazonS3.class).putObject(CommonEnum.bucketname, key, inputStream, objectMetadata);
            JdOSSAipHandler.putStream( key,   inputStream, CommonEnum.pictureType, CommonEnum.mjfile);
            //ossClient.putObject(BUCKET_NAME, generateOSSKey(fileAddress), new File(fileAddress));

            // 文件上传成功的处理逻辑
            System.out.println("文件上传成功：" + fileAddress);
        } catch (Exception e) {
            // 文件上传失败的处理逻辑
            System.out.println("文件上传失败：" + fileAddress);
            e.printStackTrace();
        }
    }

    // 生成OSS对象存储的Key
    private String generateOSSKey(String fileAddress) {
        // 根据需求自行实现
        // 示例代码直接使用文件的名称作为Key
        int lastIndexOfSlash = fileAddress.lastIndexOf("/");
        return fileAddress.substring(lastIndexOfSlash + 1);
    }

    // 在Spring Boot应用关闭时释放资源
    @PreDestroy
    private void cleanup() {
        executorService.shutdown();
        SpringUtil.getBean(AmazonS3.class).shutdown();
    }
}

