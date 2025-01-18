package com.hncboy.beehive.base.util;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.dtflys.forest.Forest;
import com.dtflys.forest.http.ForestRequest;
import com.dtflys.forest.http.ForestResponse;
import com.hncboy.beehive.base.enums.CommonEnum;
import com.hncboy.beehive.base.exception.ServiceException;
import com.hncboy.beehive.base.resource.aip.JdOSSAipHandler;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

/**
 * @author ll
 * @date 2023/5/20
 * 文件相关工具类
 */
@UtilityClass
@Slf4j
public class FileUtil {

    /**
     * 下载网络图片到本地
     *
     * @param fileUrl  文件地址
     * @param savePath 保存地址
     */
    public void downloadFromUrl(String fileUrl, String savePath) {
        try {
            // 构建完整路径
            savePath = getFileSavePathPrefix().concat(savePath);

            // 构建请求
            ForestRequest<?> forestRequest = Forest.get(fileUrl);
            ForestRequestUtil.buildProxy(forestRequest);
            // 发起请求
            ForestResponse<?> forestResponse = forestRequest.execute(ForestResponse.class);

            // 保存文件
            Files.copy(forestResponse.getInputStream(), Path.of(savePath), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            log.error("网络地址下载文件到本地失败，fileUrl：{}，savePath：{}", fileUrl, savePath, e);
        }
    }

    /**
     * 下载 MultipartFile 到本地
     *
     * @param multipartFile 文件
     * @param fileName      文件名
     */
    public void downloadFromMultipartFile(MultipartFile multipartFile, String fileName) {
        // 构建完整路径
        String savePath = getFileSavePathPrefix().concat(fileName);
        File targetFile = new File(savePath);
        try (InputStream inputStream = multipartFile.getInputStream();
             OutputStream outputStream = new FileOutputStream(targetFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            log.error("MultipartFile 下载文件到本地失败，savePath：{}", savePath, e);
        }
    }
    public void putFromMultipartFile(MultipartFile multipartFile, String newFileName,String fileType) {

        try {
                InputStream inputStream = multipartFile.getInputStream();
            Pair<Boolean, String> pb = JdOSSAipHandler.putStream( newFileName,   inputStream, fileType, CommonEnum.upfile);
            if(!pb.getKey()){
                throw new ServiceException(pb.getValue());
            }

        } catch (Exception e) {
            log.error("OSS上传失败，savePath：{}", newFileName, e);
            throw new ServiceException("上传失败，请稍后重试");
        }
    }

    public Pair<Boolean, String> putDalleFile(String key, String dalleUrl) {

        try {
            URL url = new URL(dalleUrl);
            InputStream inputStream = url.openStream();
            Pair<Boolean, String> pb = JdOSSAipHandler.putStream( key,   inputStream, CommonEnum.pictureType, CommonEnum.dallefile);
            return pb;

        } catch (Exception e) {
            log.error("dalle OSS上传失败，savePath：{}", key, e);
            //throw new ServiceException("上传失败，请稍后重试");
            return new Pair<>(false, e.getMessage());
        }
    }
    public boolean downloadNetFile(String netImg, String fileName) {
        String savePath = getFileSavePathPrefix().concat(fileName);
        try (InputStream in = new URL(netImg).openStream();
             OutputStream out = new FileOutputStream(savePath)) {
            byte[] buffer = new byte[2048];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }
public String getImglastName(String ulString)  {
        try {
            URL ul = new URL(ulString);
            HttpURLConnection connection = (HttpURLConnection) ul.openConnection();
            connection.setRequestMethod("HEAD"); // 使用HEAD方法以避免下载整个文件
            String contentType = connection.getContentType();
            return contentType.substring(contentType.lastIndexOf("/") + 1);
        }catch (Exception E){
            return null;
        }
}
    /**
     * 获取文件后缀名
     *
     * @param filename 文件名
     * @return 后缀名
     */
    public String getFileExtension(String filename) {
        if (StrUtil.isEmpty(filename)) {
            return null;
        }
        int dotIndex = filename.lastIndexOf(StrPool.DOT);
        if (dotIndex >= 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1);
        }
        return null;
    }

    /**
     * 获取文件保存路径前缀
     *
     * @return 前缀
     */
    public String getFileSavePathPrefix() {
        return SpringUtil.getApplicationContext().getEnvironment().getProperty("file-path.save-prefix");
    }

    /**
     * 获取文件路径访问前缀
     *
     * @return 前缀
     */
    public String getFilePathVisitPrefix() {
        return "/".concat(Objects.requireNonNull(SpringUtil.getApplicationContext().getEnvironment().getProperty("file-path.visit-prefix"))).concat("/");
    }
}
