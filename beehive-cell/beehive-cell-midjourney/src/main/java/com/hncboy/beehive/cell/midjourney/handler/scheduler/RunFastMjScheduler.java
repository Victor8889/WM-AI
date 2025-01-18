package com.hncboy.beehive.cell.midjourney.handler.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.hncboy.beehive.base.domain.entity.RoomMidjourneyMsgDO;
import com.hncboy.beehive.base.enums.CommonEnum;
import com.hncboy.beehive.base.enums.MidjourneyMsgStatusEnum;
import com.hncboy.beehive.base.enums.MjMsgActionEnum;
import com.hncboy.beehive.cell.midjourney.service.FileUploadService;
import com.hncboy.beehive.cell.midjourney.service.RoomMidjourneyMsgService;
import jakarta.annotation.Resource;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author ll
 * @date 2023/7/1
 * 执行duck MJ任务
 */
@Slf4j
@Component
public class RunFastMjScheduler {// implements ApplicationRunner

    @Resource
    private RoomMidjourneyMsgService midjourneyMsgService;
    @Autowired
    @Resource
    private FileUploadService fileUploadService;

    @Scheduled(cron = "0/3 * * * * ?")
    public void handler() {
        if(!CommonEnum.isMainRun)
            return;
        log.info("1、  run fast MJ thread start.");
            //while(true) {
                try {
                    if(CommonEnum.isRunMj)
                        getRuntMj();
                    if(CommonEnum.isWaitMj)
                        getWaitMj();
                    getTimeOutMj();
                    // 安排每2秒钟执行一次deductedMJ()方法
                } catch (Exception e) {
                    log.error("执行MJ出现错误。" + e.getMessage());
                }finally {
                    try {
                        Thread.sleep(2*1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            //}
    }

    @Transactional(rollbackFor = Exception.class)
    public void getWaitMj() throws IOException {
        try {
            List<RoomMidjourneyMsgDO> mjList = midjourneyMsgService.getMjList(MidjourneyMsgStatusEnum.SYS_WAIT.getCode(),CommonEnum.commonOne);
            if(mjList.size() == 0) {
            CommonEnum.isWaitMj = false; //获取不到等待的mj任务，暂停查询数据库
            return;
        }
        //HaProductsDo hpd = RecordsEnum.getByName(RecordsEnum.IMG_MJ);
        //MidjourneyProperties midjourneyProperties = MidjourneyProperties.init();
        for(RoomMidjourneyMsgDO da : mjList){

            createAnswerMessage(da);

        }
        }catch(Exception e){
            log.error("执行WaitMj出现错误。" + e.getMessage());
        }
    }
    public void getRuntMj() throws IOException, ParseException {
        try {
            List<RoomMidjourneyMsgDO> mjList = midjourneyMsgService.getMjList(MidjourneyMsgStatusEnum.MJ_IN_PROGRESS.getCode(),CommonEnum.commonOne);
            if (mjList.size() == 0) {
                CommonEnum.isRunMj = false; //获取不到等待的mj任务，暂停查询数据库
                return;
            }
            for (RoomMidjourneyMsgDO da : mjList) {
                processTask(da);

            }
        }catch(Exception e){
            log.error("执行RuntMj出现错误。" + e.getMessage());
        }
    }

    public void getTimeOutMj() throws IOException, ParseException {
        //try {
        //    List<RoomMidjourneyMsgDO> mjList = midjourneyMsgService.getTimeOutMjList();
        //    if (mjList.size() == 0) {
        //        CommonEnum.isTimeOutMj = false; //获取不到等待的mj任务，暂停查询数据库
        //        return;
        //    }
        //    for (RoomMidjourneyMsgDO da : mjList) {
        //
        //        timeOutTask(da);
        //
        //    }
        //}catch (Exception e){
        //    log.error("执行TimeOut出现错误。" + e.getMessage());
        //}
    }

    /**
     * 创建回答消息
     *
     * @param answerMessage        回答消息
     */
    @Transactional(rollbackFor = Exception.class)
    public void createAnswerMessage(RoomMidjourneyMsgDO answerMessage) throws IOException {
        try {// 填充公共字段
            answerMessage.setIsDeleted(false);

            if (answerMessage.getAction().equals(MjMsgActionEnum.UPSCALE.getAction()) || answerMessage.getAction().equals(MjMsgActionEnum.VARIATION.getAction())) {//这里面含有强弱变化
                actionImg(answerMessage);
                return;
            } else if (answerMessage.getAction().equals(MjMsgActionEnum.BLEND.getAction()) || MjMsgActionEnum.FACE.getAction().equals(answerMessage.getAction())) {//混图
                blendMessage(answerMessage);
                return;
            } else if (answerMessage.getAction().equals(MjMsgActionEnum.IMAGINE.getAction())) {//变焦
            } else if (answerMessage.getAction().contains(MjMsgActionEnum.INPAINT.getAction())) {//局部重绘
                modalImg(answerMessage);
                return;
            } else if (answerMessage.getAction().contains(MjMsgActionEnum.ZOOM.getAction())) {//自定义变焦
                zoomImg(answerMessage);
                return;
            } else {//扩展/强弱变化/变焦
                vpzImg(answerMessage);
                return;
            }
            ImageModel imageModel = new ImageModel();
            if (StringUtil.isNotBlank(answerMessage.getBaseImg())) {
                imageModel.setBase64Array(new String[]{urlToBase64Array(answerMessage.getBaseImg())});

                //imageModel.setPrompt(answerMessage.getBaseImg() + " " + answerMessage.getPrompt());
            } else {
                imageModel.setBase64Array(new String[]{});
            }
            imageModel.setPrompt(answerMessage.getPrompt() + " " + answerMessage.getParams());
            imageModel.setState("");

            imageModel.setNotifyHook("");

            // 创建任务并返回状态
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .callTimeout(25, TimeUnit.SECONDS) // 设置调用超时时间为10秒
                    .connectTimeout(25, TimeUnit.SECONDS)
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            //RequestBody body = RequestBody.create(mediaType, "{\r\n  \"base64Array\": [],\r\n  \"notifyHook\": \"\",\r\n  \"prompt\": \"Cat\",\r\n  \"state\": \"\"\r\n}");
            RequestBody body = RequestBody.create(mediaType, imageModel.toJson());

            Request request = new Request.Builder()
                    .url(CommonEnum.mjApi.get(0).getBaseUrl() + "mj/submit/imagine")//https://api.duckagi.com/
                    .method("POST", body)
                    .addHeader("Authorization", "Bearer " + CommonEnum.mjApi.get(0).getApiKey())//YOUR_API_KEY
                    .addHeader("User-Agent", "Apifox/1.0.0 (https://apifox.com)")
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = client.newCall(request).execute();
            //更新返回信息
            if (response.code() == 200) {
                String responseBody = response.body().string();
                JSONObject jsonResponse = new JSONObject(responseBody);

                // 提取响应中的字段值
                int code = jsonResponse.getInt("code");
                String description = jsonResponse.getString("description");
                String result = jsonResponse.optString("result");

                answerMessage.setProgressing(1);
                answerMessage.setDuckId(result);
                answerMessage.setResponseContent(description);
                MidjourneyMsgStatusEnum statusEnum = MidjourneyMsgStatusEnum.MJ_IN_PROGRESS;
                //状态码: 1(提交成功), 21(已存在), 22(排队中), 4(程序内部错误)
                if (code == 1 || code == 21 || code == 22) {
                    statusEnum = MidjourneyMsgStatusEnum.MJ_IN_PROGRESS;
                } else {
                    statusEnum = MidjourneyMsgStatusEnum.SYS_FAILURE;
                    answerMessage.setFailureReason(description + ",任务出错，本次服务不收费");
                }
                answerMessage.setUpdateTime(new Date());
                answerMessage.setStatus(statusEnum);
                //answerMessage.setAction(MjMsgActionEnum.IMAGINE);
                midjourneyMsgService.update(answerMessage);
            } else {
                System.out.println(response.message());
                System.out.println(response.body().string());
            }
        }catch(Exception e){
            answerMessage.setFailureReason("内部错误，本次服务不收费");
            answerMessage.setStatus(MidjourneyMsgStatusEnum.SYS_FAILURE);
            midjourneyMsgService.update(answerMessage);
        }
        CommonEnum.isRunMj = true;

    }
    @Transactional(rollbackFor = Exception.class)
    public void blendMessage(RoomMidjourneyMsgDO answerMessage) throws IOException {
        // 填充公共字段
        try {
            answerMessage.setIsDeleted(false);

            BlendModel imageModel = new BlendModel();

            // 创建任务并返回状态
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .callTimeout(25, TimeUnit.SECONDS) // 设置调用超时时间为10秒
                    .connectTimeout(25, TimeUnit.SECONDS)
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            //RequestBody body = RequestBody.create(mediaType, "{\r\n  \"base64Array\": [],\r\n  \"notifyHook\": \"\",\r\n  \"prompt\": \"Cat\",\r\n  \"state\": \"\"\r\n}");
            RequestBody body;
            Request request ;
            if(MjMsgActionEnum.BLEND.getAction().equals(answerMessage.getAction())) {
                imageModel.setBase64ArrayFromImageUrls(answerMessage.getOriginalImageName());

                //imageModel.setPrompt(answerMessage.getBaseImg() + " " + answerMessage.getPrompt());

                imageModel.setState("");

                imageModel.setDimensions(answerMessage.getFinalPrompt());//尺寸PORTRAIT(2:3); SQUARE(1:1); LANDSCAPE(3:2)

                imageModel.setNotifyHook("");
                body = RequestBody.create(imageModel.toJson(),mediaType);
                request = new Request.Builder()
                        .url(CommonEnum.mjApi.get(0).getBaseUrl() + "mj/submit/blend")//https://api.duckagi.com/
                        .method("POST", body)
                        .addHeader("Authorization", "Bearer " + CommonEnum.mjApi.get(0).getApiKey())//YOUR_API_KEY
                        .addHeader("User-Agent", "Apifox/1.0.0 (https://apifox.com)")
                        .addHeader("Content-Type", "application/json")
                        .build();
            }else {
                String[] urls = answerMessage.getOriginalImageName().split(";");
                FaceModel fm = new FaceModel();
                fm.setSourceBase64(urls[0]);
                fm.setTargetBase64(urls[1]);
                AccountFilter af = new AccountFilter();
                af.setInstanceId("");
                fm.setAccountFilter(af);
                body = RequestBody.create(fm.toJson(),mediaType);

                request = new Request.Builder()
                        .url(CommonEnum.mjApi.get(0).getBaseUrl() + "mj/insight-face/swap")
                        .method("POST", body)
                        .addHeader("Authorization", "Bearer " + CommonEnum.mjApi.get(0).getApiKey())//YOUR_API_KEY
                        .addHeader("User-Agent", "Apifox/1.0.0 (https://apifox.com)")
                        .addHeader("Content-Type", "application/json")
                        .build();
                //return;
            }


            Response response = client.newCall(request).execute();
            //更新返回信息
            if (response.code() == 200) {
                String responseBody = response.body().string();
                JSONObject jsonResponse = new JSONObject(responseBody);

                // 提取响应中的字段值
                int code = jsonResponse.getInt("code");
                String description = jsonResponse.getString("description");
                String result = jsonResponse.optString("result");

                answerMessage.setProgressing(1);
                answerMessage.setDuckId(result);
                answerMessage.setResponseContent(description);
                MidjourneyMsgStatusEnum statusEnum = MidjourneyMsgStatusEnum.MJ_IN_PROGRESS;
                //状态码: 1(提交成功), 21(已存在), 22(排队中), 4(程序内部错误)
                if (code == 1 || code == 21 || code == 22) {
                    statusEnum = MidjourneyMsgStatusEnum.MJ_IN_PROGRESS;
                } else {
                    statusEnum = MidjourneyMsgStatusEnum.SYS_FAILURE;
                    answerMessage.setFailureReason(description + ",任务出错，本次服务不收费");
                }
                answerMessage.setUpdateTime(new Date());
                answerMessage.setStatus(statusEnum);
                //answerMessage.setAction(MjMsgActionEnum.IMAGINE);
                midjourneyMsgService.update(answerMessage);
            } else {
                System.out.println(response.message());
                System.out.println(response.body().string());
            }
        }catch(Exception e){
            answerMessage.setFailureReason(e.getMessage() + "内部错误，本次服务不收费");
            answerMessage.setStatus(MidjourneyMsgStatusEnum.SYS_FAILURE);
            midjourneyMsgService.update(answerMessage);
        }
        CommonEnum.isRunMj = true;

    }
    //参考图转base64
    public String urlToBase64Array(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        InputStream inputStream = url.openStream();
        byte[] imageBytes = inputStream.readAllBytes();

        // 将图片数据转换为Base64格式
        String base64Image = "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes) ;//
        return base64Image;
    }


    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    //查询未完成的状态进行处理

    @Transactional(rollbackFor = Exception.class)
    public void processTask(RoomMidjourneyMsgDO answerMessage) throws IOException, ParseException {

        if(StringUtil.isBlank(answerMessage.getDuckId()))
            return;

        OkHttpClient client = new OkHttpClient().newBuilder()
                .callTimeout(25, TimeUnit.SECONDS) // 设置调用超时时间为10秒
                .connectTimeout(25, TimeUnit.SECONDS)
                .build();
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = RequestBody.create("",mediaType);
        Request request = new Request.Builder()
                .url(CommonEnum.mjApi.get(0).getBaseUrl() + "mj/task/"+answerMessage.getDuckId()+"/fetch")//https://api.duckagi.com/
                .get()
                .addHeader("Authorization", "Bearer "+ CommonEnum.mjApi.get(0).getApiKey())//YOUR_API_KEY
                .addHeader("User-Agent", "Apifox/1.0.0 (https://apifox.com)")
                .build();
        Response response = client.newCall(request).execute();
        if(response.code() == 200) {
            String responseBody = response.body().string();
            JSONObject jsonResponse = null;
            try {
                jsonResponse = new JSONObject(responseBody);

            }catch(Exception e){
                int mjProcess = 0;
                if((answerMessage.getProgressing()+1) > mjProcess)
                    mjProcess = answerMessage.getProgressing() + 1;
                if(mjProcess >= 100)
                    mjProcess = 100;
                if(mjProcess == 100 )
                    mjProcess = 99;
                answerMessage.setProgressing(mjProcess);
                answerMessage.setStatus( MidjourneyMsgStatusEnum.MJ_IN_PROGRESS);
                midjourneyMsgService.update(answerMessage);
            }

            // 提取响应中的字段值
            String status = jsonResponse.getString("status");
            String description = jsonResponse.getString("description");
            String failReason = jsonResponse.getString("failReason");
            String action = jsonResponse.getString("action");
            String imageUrl = jsonResponse.getString("imageUrl");
            String progress = jsonResponse.getString("progress");
            String prompt = jsonResponse.getString("prompt");
            JSONArray buttons ;
            try{
                if(jsonResponse.has("buttons")){
                    buttons = jsonResponse.getJSONArray("buttons");
                    answerMessage.setButtons(buttons.toString());
                }}catch(Exception e){

            }
            long finishTime = jsonResponse.getLong("finishTime");
            long startTime = jsonResponse.getLong("startTime");
            if("SUCCESS".equals(status) && StringUtil.isNotBlank(imageUrl)){
                //String lastName = "";
                //if(imageUrl.substring(imageUrl.lastIndexOf("/"+1)).contains("."))
                //    lastName = imageUrl.substring(imageUrl.lastIndexOf("."+1));
                //            else
                //    lastName = FileUtil.getImglastName(imageUrl);
                //if(StringUtil.isNotBlank(lastName) && FileUtil.downloadNetFile(imageUrl, "upload" + File.separator + "mj" + File.separator + imageUrl.substring(imageUrl.lastIndexOf("/")+1)+"."+lastName)) {
                //    String showUrl = "upload" + File.separator + "mj" + File.separator + imageUrl.substring(imageUrl.lastIndexOf("/") + 1)+"."+lastName;
                //    answerMessage.setCompressedImageName(showUrl);
                //}
                fileUploadService.submitUploadTasks(answerMessage.getId(),imageUrl);//添加到对象存储
            }

            answerMessage.setResponseContent(responseBody);
            answerMessage.setDiscordImageUrl(imageUrl);

            answerMessage.setDiscordStartTime(new Date(startTime));
            answerMessage.setDiscordFinishTime(new Date(finishTime));
            if(StringUtil.isNotBlank(prompt))
            answerMessage.setFinalPrompt(prompt);
            //if(progress == 0)
            int mjProcess = Integer.parseInt(progress.replace("%",""));
            if((answerMessage.getProgressing()+1) > mjProcess)
                mjProcess = answerMessage.getProgressing() + 1;
            if(mjProcess >= 100)
                mjProcess = 100;
            if(mjProcess == 100 && Integer.parseInt(progress.replace("%","")) < 100)
                mjProcess = 99;

                        answerMessage.setProgressing(mjProcess);
            //answerMessage.setDuckId(result);
            answerMessage.setResponseContent(description);
            MidjourneyMsgStatusEnum statusEnum = MidjourneyMsgStatusEnum.MJ_IN_PROGRESS;
            //状态码: 1(提交成功), 21(已存在), 22(排队中), 4(程序内部错误)
            switch (status) {
                case "SUBMITTED":
                    statusEnum = MidjourneyMsgStatusEnum.MJ_IN_PROGRESS;
                    break;
                case "IN_PROGRESS":
                    statusEnum = MidjourneyMsgStatusEnum.MJ_IN_PROGRESS;
                    break;
                case "FAILURE":
                    statusEnum = MidjourneyMsgStatusEnum.SYS_FAILURE;
                    answerMessage.setFailureReason(failReason+",本次服务不收费");
                    break;
                case "SUCCESS":
                    statusEnum = MidjourneyMsgStatusEnum.SYS_SUCCESS;
                    break;
            }

            answerMessage.setStatus(statusEnum);

            midjourneyMsgService.update(answerMessage);
        }else{
            System.out.println(response.message());
            System.out.println(response.body().string());
        }
    }
    @Transactional(rollbackFor = Exception.class)
    public void timeOutTask(RoomMidjourneyMsgDO answerMessage) throws IOException, ParseException {

        answerMessage.setFailureReason("执行超时，本次服务不收费");
        answerMessage.setStatus(MidjourneyMsgStatusEnum.SYS_FAILURE);

        midjourneyMsgService.update(answerMessage);
    }

    @Transactional(rollbackFor = Exception.class)
    public void actionImg(RoomMidjourneyMsgDO answerMessage) throws IOException {
      try{
        String action = "UPSCALE";//UPSCALE(放大); VARIATION(变换);
        if(answerMessage.getAction().equals(MjMsgActionEnum.VARIATION.getAction()))
            action = "VARIATION";

        ActionImgModel aim = new ActionImgModel();
        aim.setAction(action);
        aim.setIndex(answerMessage.getUvIndex());
        aim.setState(answerMessage.getParams());
        aim.setTaskId(""+answerMessage.getUvParentId());//父类的ducid
        ObjectMapper objectMapper = new ObjectMapper();
        String json =objectMapper.writeValueAsString(aim);
        OkHttpClient client = new OkHttpClient().newBuilder()
                .callTimeout(25, TimeUnit.SECONDS) // 设置调用超时时间为10秒
                .connectTimeout(25, TimeUnit.SECONDS) // 设置连接超时时间为10秒
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create( json,mediaType);
        Request request = new Request.Builder()
                .url(CommonEnum.mjApi.get(0).getBaseUrl() + "mj/submit/change")
                .method("POST", body)
                .addHeader("Authorization", "Bearer "+ CommonEnum.mjApi.get(0).getApiKey())//YOUR_API_KEY
                .addHeader("User-Agent", "Apifox/1.0.0 (https://apifox.com)")
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = client.newCall(request).execute();
        if(response.code() == 200){
            String responseBody = response.body().string();
            JSONObject jsonResponse = new JSONObject(responseBody);

            // 提取响应中的字段值
            int code = jsonResponse.getInt("code");
            String description = jsonResponse.getString("description");
            String result = jsonResponse.getString("result");

            answerMessage.setProgressing(1);
            answerMessage.setDuckId(result);
            answerMessage.setResponseContent(description);
            MidjourneyMsgStatusEnum statusEnum = MidjourneyMsgStatusEnum.MJ_IN_PROGRESS;
            //状态码: 1(提交成功), 21(已存在), 22(排队中), 4(程序内部错误)
            if(code == 1 || code == 21 || code == 22){
                statusEnum = MidjourneyMsgStatusEnum.MJ_IN_PROGRESS;
            }else {
                statusEnum=MidjourneyMsgStatusEnum.SYS_FAILURE;
                answerMessage.setFailureReason(description+",任务出错，本次服务不收费");
            }
            answerMessage.setUpdateTime(new Date());
            answerMessage.setStatus(statusEnum);
            midjourneyMsgService.update(answerMessage);
        }else{
            System.out.println(response.message());
            System.out.println(response.body().string());
        }
      }catch(Exception e){
          answerMessage.setFailureReason("内部错误，本次服务不收费");
          answerMessage.setStatus(MidjourneyMsgStatusEnum.SYS_FAILURE);
          midjourneyMsgService.update(answerMessage);
      }
        CommonEnum.isRunMj = true;
    }
    @Transactional(rollbackFor = Exception.class)
    public void vpzImg(RoomMidjourneyMsgDO answerMessage) throws IOException {
        try{
        czpImgModel aim = new czpImgModel();
        aim.setCustomId(answerMessage.getAction());
        aim.setState(answerMessage.getParams());
        aim.setTaskId(""+answerMessage.getUvParentId());//父类的ducid
        ObjectMapper objectMapper = new ObjectMapper();
        String json =objectMapper.writeValueAsString(aim);
        OkHttpClient client = new OkHttpClient().newBuilder()
                .callTimeout(25, TimeUnit.SECONDS) // 设置调用超时时间为10秒
                .connectTimeout(25, TimeUnit.SECONDS)
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create( json,mediaType);
        Request request = new Request.Builder()
                .url(CommonEnum.mjApi.get(0).getBaseUrl() + "mj/submit/action")
                .method("POST", body)
                .addHeader("Authorization", "Bearer "+ CommonEnum.mjApi.get(0).getApiKey())//YOUR_API_KEY
                .addHeader("User-Agent", "Apifox/1.0.0 (https://apifox.com)")
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = client.newCall(request).execute();
        if(response.code() == 200){
            String responseBody = response.body().string();
            JSONObject jsonResponse = new JSONObject(responseBody);

            // 提取响应中的字段值
            int code = jsonResponse.getInt("code");
            String description = jsonResponse.getString("description");
            String result = jsonResponse.getString("result");

            answerMessage.setProgressing(1);
            answerMessage.setDuckId(result);
            answerMessage.setResponseContent(description);
            MidjourneyMsgStatusEnum statusEnum = MidjourneyMsgStatusEnum.MJ_IN_PROGRESS;
            //状态码: 1(提交成功), 21(已存在), 22(排队中), 4(程序内部错误)
            if(code == 1 || code == 21 || code == 22){
                statusEnum = MidjourneyMsgStatusEnum.MJ_IN_PROGRESS;
            }else {
                statusEnum=MidjourneyMsgStatusEnum.SYS_FAILURE;
                answerMessage.setFailureReason(description+",任务出错，本次服务不收费");
            }
            answerMessage.setUpdateTime(new Date());
            answerMessage.setStatus(statusEnum);
            midjourneyMsgService.update(answerMessage);
        }else{
            System.out.println(response.message());
            System.out.println(response.body().string());
        }
        }catch(Exception e){
            answerMessage.setFailureReason("内部错误，本次服务不收费");
            answerMessage.setStatus(MidjourneyMsgStatusEnum.SYS_FAILURE);
            midjourneyMsgService.update(answerMessage);
        }
        CommonEnum.isRunMj = true;
    }
    @Transactional(rollbackFor = Exception.class)
    public void modalImg(RoomMidjourneyMsgDO answerMessage) throws IOException {//局部重绘
    try{
        //先发送命令确认
        czpImgModel aim = new czpImgModel();
        aim.setCustomId(answerMessage.getAction());
        aim.setState(answerMessage.getParams());
        aim.setTaskId(""+answerMessage.getUvParentId());//父类的ducid
        ObjectMapper objectMapper = new ObjectMapper();
        String json =objectMapper.writeValueAsString(aim);
        OkHttpClient client = new OkHttpClient().newBuilder()
                .callTimeout(25, TimeUnit.SECONDS) // 设置调用超时时间为10秒
                .connectTimeout(25, TimeUnit.SECONDS)
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create( json,mediaType);
        Request request = new Request.Builder()
                .url(CommonEnum.mjApi.get(0).getBaseUrl() + "mj/submit/action")
                .method("POST", body)
                .addHeader("Authorization", "Bearer "+ CommonEnum.mjApi.get(0).getApiKey())//YOUR_API_KEY
                .addHeader("User-Agent", "Apifox/1.0.0 (https://apifox.com)")
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = client.newCall(request).execute();
        if(response.code() == 200) {
            String responseBody = response.body().string();
            JSONObject jsonResponse = new JSONObject(responseBody);

            // 提取响应中的字段值
            int code = jsonResponse.getInt("code");
            if(code == 21){

                String result = jsonResponse.getString("result");

                modalImgModel aim1 = new modalImgModel();
                aim1.setMaskBase64(answerMessage.getOriginalImageName());
                aim1.setPrompt(answerMessage.getFinalPrompt());
                aim1.setTaskId(result);//父类的ducid
                objectMapper = new ObjectMapper();
                json =objectMapper.writeValueAsString(aim1);
                client = new OkHttpClient().newBuilder()
                        .callTimeout(25, TimeUnit.SECONDS) // 设置调用超时时间为10秒
                        .connectTimeout(25, TimeUnit.SECONDS)
                        .build();
                mediaType = MediaType.parse("application/json");
                body = RequestBody.create( json,mediaType);
                request = new Request.Builder()
                        .url(CommonEnum.mjApi.get(0).getBaseUrl() + "mj/submit/modal")
                        .method("POST", body)
                        .addHeader("Authorization", "Bearer "+ CommonEnum.mjApi.get(0).getApiKey())//YOUR_API_KEY
                        .addHeader("User-Agent", "Apifox/1.0.0 (https://apifox.com)")
                        .addHeader("Content-Type", "application/json")
                        .build();
                response = client.newCall(request).execute();
                if(response.code() == 200){
                    responseBody = response.body().string();
                    jsonResponse = new JSONObject(responseBody);

                    // 提取响应中的字段值
                    code = jsonResponse.getInt("code");

                    String description = "";
                    try {
                        description = jsonResponse.getString("description");
                        result = jsonResponse.getString("result");
                    }catch(Exception e){

                    }

                    answerMessage.setProgressing(1);
                    answerMessage.setDuckId(result);
                    answerMessage.setResponseContent(description);
                    MidjourneyMsgStatusEnum statusEnum = MidjourneyMsgStatusEnum.MJ_IN_PROGRESS;
                    //状态码: 1(提交成功), 21(已存在), 22(排队中), 4(程序内部错误)
                    if(code == 1 || code == 21 || code == 22){
                        statusEnum = MidjourneyMsgStatusEnum.MJ_IN_PROGRESS;
                    }else {
                        statusEnum=MidjourneyMsgStatusEnum.SYS_FAILURE;
                        answerMessage.setFailureReason(description+",任务出错，本次服务不收费");
                    }
                    answerMessage.setUpdateTime(new Date());
                    answerMessage.setStatus(statusEnum);
                    midjourneyMsgService.update(answerMessage);
                }else{
                    System.out.println(response.message());
                    System.out.println(response.body().string());
                }
            }else{
                String description = jsonResponse.getString("description");
                answerMessage.setFailureReason(description + ",MJ内部错误，本次服务不收费");
                answerMessage.setStatus(MidjourneyMsgStatusEnum.SYS_FAILURE);
                midjourneyMsgService.update(answerMessage);
            }
        }
    }catch(Exception e){
        answerMessage.setFailureReason("内部错误，本次服务不收费");
        answerMessage.setStatus(MidjourneyMsgStatusEnum.SYS_FAILURE);
        midjourneyMsgService.update(answerMessage);
    }
        CommonEnum.isRunMj = true;
    }
    @Transactional(rollbackFor = Exception.class)
    public void zoomImg(RoomMidjourneyMsgDO answerMessage) throws IOException {//自定义变焦
try {
    //先发送命令确认
    czpImgModel aim = new czpImgModel();
    aim.setCustomId(answerMessage.getAction());
    aim.setState(answerMessage.getParams());
    aim.setTaskId("" + answerMessage.getUvParentId());//父类的ducid
    ObjectMapper objectMapper = new ObjectMapper();
    String json = objectMapper.writeValueAsString(aim);
    OkHttpClient client = new OkHttpClient().newBuilder()
            .callTimeout(25, TimeUnit.SECONDS) // 设置调用超时时间为10秒
            .connectTimeout(25, TimeUnit.SECONDS)
            .build();
    MediaType mediaType = MediaType.parse("application/json");
    RequestBody body = RequestBody.create(json, mediaType);
    Request request = new Request.Builder()
            .url(CommonEnum.mjApi.get(0).getBaseUrl() + "mj/submit/action")
            .method("POST", body)
            .addHeader("Authorization", "Bearer " + CommonEnum.mjApi.get(0).getApiKey())//YOUR_API_KEY
            .addHeader("User-Agent", "Apifox/1.0.0 (https://apifox.com)")
            .addHeader("Content-Type", "application/json")
            .build();
    Response response = client.newCall(request).execute();
    if (response.code() == 200) {
        String responseBody = response.body().string();
        JSONObject jsonResponse = new JSONObject(responseBody);

        // 提取响应中的字段值
        int code = jsonResponse.getInt("code");
        if (code == 21) {

            String result = jsonResponse.getString("result");

            JSONObject properties ;
            String propmt = "";
            if(jsonResponse.has("properties")){
                properties = jsonResponse.getJSONObject("properties");
                propmt = properties.getString("finalPrompt");
            }

            zoomImgModel aim1 = new zoomImgModel();
            aim1.setPrompt(propmt+" "+answerMessage.getFinalPrompt());
            aim1.setTaskId(result);//父类的ducid
            objectMapper = new ObjectMapper();
            json = objectMapper.writeValueAsString(aim1);
            client = new OkHttpClient().newBuilder()
                    .callTimeout(25, TimeUnit.SECONDS) // 设置调用超时时间为10秒
                    .connectTimeout(25, TimeUnit.SECONDS)
                    .build();
            mediaType = MediaType.parse("application/json");
            body = RequestBody.create(json, mediaType);
            request = new Request.Builder()
                    .url(CommonEnum.mjApi.get(0).getBaseUrl() + "mj/submit/modal")
                    .method("POST", body)
                    .addHeader("Authorization", "Bearer " + CommonEnum.mjApi.get(0).getApiKey())//YOUR_API_KEY
                    .addHeader("User-Agent", "Apifox/1.0.0 (https://apifox.com)")
                    .addHeader("Content-Type", "application/json")
                    .build();
            response = client.newCall(request).execute();
            if (response.code() == 200) {
                responseBody = response.body().string();
                jsonResponse = new JSONObject(responseBody);

                // 提取响应中的字段值
                code = jsonResponse.getInt("code");
                String description = "";
                try {
                    description = jsonResponse.getString("description");
                    result = jsonResponse.getString("result");
                } catch (Exception e) {

                }

                answerMessage.setProgressing(1);
                answerMessage.setDuckId(result);
                answerMessage.setResponseContent(description);
                MidjourneyMsgStatusEnum statusEnum = MidjourneyMsgStatusEnum.MJ_IN_PROGRESS;
                //状态码: 1(提交成功), 21(已存在), 22(排队中), 4(程序内部错误)
                if (code == 1 || code == 21 || code == 22) {
                    statusEnum = MidjourneyMsgStatusEnum.MJ_IN_PROGRESS;
                } else {
                    statusEnum = MidjourneyMsgStatusEnum.SYS_FAILURE;
                    answerMessage.setFailureReason(description + ",任务出错，本次服务不收费");
                }
                answerMessage.setUpdateTime(new Date());
                answerMessage.setStatus(statusEnum);
                midjourneyMsgService.update(answerMessage);
            } else {
                System.out.println(response.message());
                System.out.println(response.body().string());
            }
        } else {
            String description = jsonResponse.getString("description");
            answerMessage.setFailureReason(description + ",MJ内部错误，本次服务不收费");
            answerMessage.setStatus(MidjourneyMsgStatusEnum.SYS_FAILURE);
            midjourneyMsgService.update(answerMessage);
        }
    }
}catch(Exception e){
    answerMessage.setFailureReason("内部错误，本次服务不收费");
    answerMessage.setStatus(MidjourneyMsgStatusEnum.SYS_FAILURE);
    midjourneyMsgService.update(answerMessage);
}
        CommonEnum.isRunMj = true;
    }

    // 定义用于映射JSON的Java对象类
    class ActionImgModel {
        private String action;
        private int index;
        private String notifyHook;
        private String state;
        private String taskId;

        // 必须提供无参构造函数
        public ActionImgModel() {
        }

        // 提供getters和setters方法
        public String getAction() {
            return this.action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public int getIndex() {
            return this.index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public String getNotifyHook() {
            return this.notifyHook;
        }

        public void setNotifyHook(String notifyHook) {
            this.notifyHook = notifyHook;
        }

        public String getState() {
            return this.state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getTaskId() {
            return this.taskId;
        }

        public void setTaskId(String taskId) {
            this.taskId = taskId;
        }
    }
    // 定义用于映射JSON的Java对象类
    class czpImgModel {
        private String customId;
        private String notifyHook;
        private String state;
        private String taskId;

        // 必须提供无参构造函数
        public czpImgModel() {
        }

        // 提供getters和setters方法
        public String getCustomId() {
            return this.customId;
        }

        public void setCustomId(String customId) {
            this.customId = customId;
        }


        public String getNotifyHook() {
            return this.notifyHook;
        }

        public void setNotifyHook(String notifyHook) {
            this.notifyHook = notifyHook;
        }

        public String getState() {
            return this.state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getTaskId() {
            return this.taskId;
        }

        public void setTaskId(String taskId) {
            this.taskId = taskId;
        }
    }
    // 定义用于映射JSON的Java对象类
    class modalImgModel {
        private String maskBase64;
        private String prompt;
        private String taskId;



        public String getMaskBase64() {
            return this.maskBase64;
        }

        public void setMaskBase64(String maskBase64) {
            this.maskBase64 = maskBase64;
        }

        public String getPrompt() {
            return this.prompt;
        }

        public void setPrompt(String prompt) {
            this.prompt = prompt;
        }

        public String getTaskId() {
            return this.taskId;
        }

        public void setTaskId(String taskId) {
            this.taskId = taskId;
        }
    }
    // 定义用于映射JSON的Java对象类
    class zoomImgModel {
        private String prompt;
        private String taskId;

        public String getPrompt() {
            return this.prompt;
        }

        public void setPrompt(String prompt) {
            this.prompt = prompt;
        }

        public String getTaskId() {
            return this.taskId;
        }

        public void setTaskId(String taskId) {
            this.taskId = taskId;
        }
    }
    class ImageModel {
        private String[] base64Array;
        private String notifyHook;
        private String prompt;
        private String state;

        public String[] getBase64Array() {
            return base64Array;
        }

        public void setBase64Array(String[] base64Array) {
            this.base64Array = base64Array;
        }

        public String getNotifyHook() {
            return notifyHook;
        }

        public void setNotifyHook(String notifyHook) {
            this.notifyHook = notifyHook;
        }

        public String getPrompt() {
            return prompt;
        }

        public void setPrompt(String prompt) {
            this.prompt = prompt;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String toJson() {
            Gson gson = new Gson();
            return gson.toJson(this);
        }
    }
    class BlendModel {
        private String[] base64Array;
        private String notifyHook;
        private String state;
        private String dimensions;

        public String[] getBase64Array() {
            return base64Array;
        }

        public void setBase64Array(String[] base64Array) {
            this.base64Array = base64Array;
        }

        public String getNotifyHook() {
            return notifyHook;
        }

        public void setNotifyHook(String notifyHook) {
            this.notifyHook = notifyHook;
        }


        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getDimensions() {
            return dimensions;
        }

        public void setDimensions(String dimensions) {
            this.dimensions = dimensions;
        }

        public String toJson() {
            Gson gson = new Gson();
            return gson.toJson(this);
        }
        // 根据图片地址字符串设置base64Array参数值
        public void setBase64ArrayFromImageUrls(String imageUrls) {
            String[] urls = imageUrls.split(";"); // 使用分号分割多个图片地址
            this.base64Array = new String[urls.length];
            for (int i = 0; i < urls.length; i++) {
                // 将每个图片地址转换为base64格式并存入数组
                this.base64Array[i] = urlToBase64Array1(urls[i]);
            }
        }
        public String urlToBase64Array1(String imageUrl)  {
            try {
                URL url = new URL(imageUrl);
                InputStream inputStream = url.openStream();
                byte[] imageBytes = inputStream.readAllBytes();

                // 将图片数据转换为Base64格式
                String base64Image = "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
                return base64Image;
            }catch(Exception e){
                return null;
            }
        }
    }
    class FaceModel {
        private String sourceBase64;
        private String targetBase64;
        private AccountFilter accountFilter;
        private String notifyHook;
        private String state;

        public String getSourceBase64() {
            return sourceBase64;
        }

        public void setSourceBase64(String sourceBase64) {
            this.sourceBase64 = urlToBase64Array1(sourceBase64);
        }

        public String getTargetBase64() {
            return targetBase64;
        }

        public void setTargetBase64(String targetBase64) {
            this.targetBase64 = urlToBase64Array1(targetBase64);
        }

        public AccountFilter getAccountFilter() {
            return accountFilter;
        }

        public void setAccountFilter(AccountFilter accountFilter) {
            this.accountFilter = accountFilter;
        }

        public String getNotifyHook() {
            return notifyHook;
        }

        public void setNotifyHook(String notifyHook) {
            this.notifyHook = notifyHook;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String toJson() {
            Gson gson = new Gson();
            return gson.toJson(this);
        }

        public String urlToBase64Array1(String imageUrl)  {
            try {
                URL url = new URL(imageUrl);
                InputStream inputStream = url.openStream();
                byte[] imageBytes = inputStream.readAllBytes();

                // 将图片数据转换为Base64格式
                String base64Image = "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
                return base64Image;
            }catch(Exception e){
                return null;
            }
        }
    }
    class AccountFilter {
        private String instanceId;

        public String getInstanceId() {
            return instanceId;
        }

        public void setInstanceId(String instanceId) {
            this.instanceId = instanceId;
        }
    }
}
