//package com.hncboy.beehive.cell.midjourney.handler.scheduler;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.google.gson.Gson;
//import com.hncboy.beehive.base.domain.entity.RoomMidjourneyMsgDO;
//import com.hncboy.beehive.base.enums.CommonEnum;
//import com.hncboy.beehive.base.enums.MidjourneyMsgStatusEnum;
//import com.hncboy.beehive.base.enums.MjMsgActionEnum;
//import com.hncboy.beehive.cell.midjourney.service.FileUploadService;
//import com.hncboy.beehive.cell.midjourney.service.RoomMidjourneyMsgService;
//import jakarta.annotation.Resource;
//import jodd.util.StringUtil;
//import lombok.extern.slf4j.Slf4j;
//import okhttp3.*;
//import org.json.JSONObject;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.URL;
//import java.text.ParseException;
//import java.time.format.DateTimeFormatter;
//import java.util.Base64;
//import java.util.Date;
//import java.util.List;
//
///**
// * @author ll
// * @date 2023/7/1
// * 执行duck MJ任务    换成fast那个了
// */
//@Slf4j
//@Component
//public class RunDuckMjScheduler{// implements ApplicationRunner
//
//    @Resource
//    private RoomMidjourneyMsgService midjourneyMsgService;
//    @Autowired
//    @Resource
//    private FileUploadService fileUploadService;
//
//    @Scheduled(cron = "0/3 * * * * ?")
//    public void handler() {
//        if(!CommonEnum.isMainRun)
//            return;
//        log.info("1、  run duck MJ thread start.");
//            //while(true) {
//                try {
//                    if(CommonEnum.isRunMj)
//                        getRuntMj();
//                    if(CommonEnum.isWaitMj)
//                        getWaitMj();
//                        getTimeOutMj();
//                    // 安排每2秒钟执行一次deductedMJ()方法
//                } catch (Exception e) {
//                    log.error("执行MJ出现错误。" + e.getMessage());
//                }finally {
//                    try {
//                        Thread.sleep(2*1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            //}
//    }
//
//    @Transactional(rollbackFor = Exception.class)
//    public void getWaitMj() throws IOException {
//        try{
//            List<RoomMidjourneyMsgDO> mjList = midjourneyMsgService.getMjList(MidjourneyMsgStatusEnum.SYS_WAIT.getCode(),CommonEnum.commonOne);
//        if(mjList.size() == 0) {
//            CommonEnum.isWaitMj = false; //获取不到等待的mj任务，暂停查询数据库
//            return;
//        }
//        //HaProductsDo hpd = RecordsEnum.getByName(RecordsEnum.IMG_MJ);
//        //MidjourneyProperties midjourneyProperties = MidjourneyProperties.init();
//        for(RoomMidjourneyMsgDO da : mjList){
//
//            createAnswerMessage(da);
//
//        }
//    }catch (Exception e){
//        log.error("执行wait fast mj出现错误。" + e.getMessage());
//    }
//    }
//    public void getRuntMj() throws IOException, ParseException {
//        try{
//        List<RoomMidjourneyMsgDO> mjList = midjourneyMsgService.getMjList(MidjourneyMsgStatusEnum.MJ_IN_PROGRESS.getCode(),CommonEnum.commonOne);
//        if(mjList.size() == 0) {
//            CommonEnum.isRunMj = false; //获取不到等待的mj任务，暂停查询数据库
//            return;
//        }
//        for(RoomMidjourneyMsgDO da : mjList){
//            processTask(da);
//
//        }
//    }catch (Exception e){
//        log.error("执行wait falst mj出现错误。" + e.getMessage());
//    }
//    }
//
//    public void getTimeOutMj() throws IOException, ParseException {
//        try{
//            List<RoomMidjourneyMsgDO> mjList = midjourneyMsgService.getTimeOutMjList();
//        if(mjList.size() == 0) {
//            CommonEnum.isTimeOutMj = false; //获取不到等待的mj任务，暂停查询数据库
//            return;
//        }
//        for(RoomMidjourneyMsgDO da : mjList){
//
//            timeOutTask(da);
//
//        }
//    }catch (Exception e){
//        log.error("执行timeout fast mj出现错误。" + e.getMessage());
//    }
//    }
//
//    /**
//     * 创建回答消息
//     *
//     * @param answerMessage        回答消息
//     */
//    @Transactional(rollbackFor = Exception.class)
//    public void createAnswerMessage(RoomMidjourneyMsgDO answerMessage) throws IOException {
//        // 填充公共字段
//        answerMessage.setIsDeleted(false);
//
//        if(!answerMessage.getAction().equals(MjMsgActionEnum.IMAGINE.getAction())){
//            actionImg(answerMessage);
//            return;
//        }
//        ImageModel imageModel = new ImageModel();
//        if(StringUtil.isNotBlank(answerMessage.getBaseImg())) {
//            imageModel.setBase64Array(new String[]{urlToBase64Array(answerMessage.getBaseImg())});
//
//            //imageModel.setPrompt(answerMessage.getBaseImg() + " " + answerMessage.getPrompt());
//        }else {
//            imageModel.setBase64Array(new String[]{});
//        }
//        imageModel.setPrompt(answerMessage.getPrompt()+" "+answerMessage.getParams());
//        imageModel.setState("");
//
//        imageModel.setNotifyHook("");
//
//        // 创建任务并返回状态
//        OkHttpClient client = new OkHttpClient().newBuilder()
//                .build();
//        MediaType mediaType = MediaType.parse("application/json");
//        //RequestBody body = RequestBody.create(mediaType, "{\r\n  \"base64Array\": [],\r\n  \"notifyHook\": \"\",\r\n  \"prompt\": \"Cat\",\r\n  \"state\": \"\"\r\n}");
//        RequestBody body = RequestBody.create(mediaType, imageModel.toJson());
//
//        Request request = new Request.Builder()
//                .url(CommonEnum.mjApi.get(0).getBaseUrl() + "mj/submit/imagine")//https://api.duckagi.com/
//                .method("POST", body)
//                .addHeader("Authorization", "Bearer "+ CommonEnum.mjApi.get(0).getApiKey())//YOUR_API_KEY
//                .addHeader("User-Agent", "Apifox/1.0.0 (https://apifox.com)")
//                .addHeader("Content-Type", "application/json")
//                .build();
//        Response response = client.newCall(request).execute();
//        //更新返回信息
//        if(response.code() == 200){
//            String responseBody = response.body().string();
//            JSONObject jsonResponse = new JSONObject(responseBody);
//
//            // 提取响应中的字段值
//            int code = jsonResponse.getInt("code");
//            String description = jsonResponse.getString("description");
//            String result = jsonResponse.optString("result");
//
//            answerMessage.setProgressing(1);
//            answerMessage.setDuckId(result);
//            answerMessage.setResponseContent(description);
//            MidjourneyMsgStatusEnum statusEnum = MidjourneyMsgStatusEnum.MJ_IN_PROGRESS;
//            //状态码: 1(提交成功), 21(已存在), 22(排队中), 4(程序内部错误)
//            if(code == 4|| code == 24){
//                statusEnum=MidjourneyMsgStatusEnum.SYS_FAILURE;
//                if(StringUtil.isNotBlank(description))
//                    answerMessage.setFailureReason(description+"本次服务不收费");
//                else
//                    answerMessage.setFailureReason("MJ内部错误，本次服务不收费");
//            }else
//                statusEnum=MidjourneyMsgStatusEnum.MJ_IN_PROGRESS;
//            answerMessage.setUpdateTime(new Date());
//            answerMessage.setStatus(statusEnum);
//            answerMessage.setAction(MjMsgActionEnum.IMAGINE.getAction());
//            midjourneyMsgService.update(answerMessage);
//        }else{
//            System.out.println(response.message());
//            System.out.println(response.body().string());
//        }
//        CommonEnum.isRunMj = true;
//
//    }
//    //参考图转base64
//    public String urlToBase64Array(String imageUrl) throws IOException {
//        URL url = new URL(imageUrl);
//        InputStream inputStream = url.openStream();
//        byte[] imageBytes = inputStream.readAllBytes();
//
//        // 将图片数据转换为Base64格式
//        String base64Image = "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes) ;
//        return base64Image;
//    }
//
//
//    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//    //查询未完成的状态进行处理
//
//    @Transactional(rollbackFor = Exception.class)
//    public void processTask(RoomMidjourneyMsgDO answerMessage) throws IOException, ParseException {
//
//        if(StringUtil.isBlank(answerMessage.getDuckId()))
//            return;
//
//        OkHttpClient client = new OkHttpClient().newBuilder()
//                .build();
//        MediaType mediaType = MediaType.parse("text/plain");
//        RequestBody body = RequestBody.create(mediaType, "");
//        Request request = new Request.Builder()
//                .url(CommonEnum.mjApi.get(0).getBaseUrl() + "mj/task/"+answerMessage.getDuckId()+"/fetch")//https://api.duckagi.com/
//                //.url("https://api.duckagi.com/mj/task/"+answerMessage.getDuckId()+"/fetch")
//                //.method("GET", body)
//                .get()
//                .addHeader("Authorization", "Bearer "+ CommonEnum.mjApi.get(0).getApiKey())//YOUR_API_KEY
//                .addHeader("User-Agent", "Apifox/1.0.0 (https://apifox.com)")
//                .build();
//        Response response = client.newCall(request).execute();
//        if(response.code() == 200) {
//            String responseBody = response.body().string();
//            JSONObject jsonResponse = new JSONObject(responseBody);
//
//            // 提取响应中的字段值
//            String status = jsonResponse.getString("status");
//            String description = jsonResponse.getString("description");
//            String failReason = jsonResponse.getString("failReason");
//            String action = jsonResponse.getString("action");
//            String imageUrl = jsonResponse.getString("imageUrl");
//            String progress = jsonResponse.getString("progress");
//            String prompt = jsonResponse.getString("prompt");
//            String promptEn = jsonResponse.getString("promptEn");
//            long finishTime = jsonResponse.getLong("finishTime");
//            long startTime = jsonResponse.getLong("startTime");
//
//            MjMsgActionEnum act = MjMsgActionEnum.IMAGINE;
//            switch (action) {
//                case "UPSCALE":
//                    act = MjMsgActionEnum.UPSCALE;
//                    break;
//                case "VARIATION":
//                    act = MjMsgActionEnum.VARIATION;
//                    break;
//                case "REROLL":
//                    act = MjMsgActionEnum.REROLL;
//                    break;
//                case "DESCRIBE":
//                    act = MjMsgActionEnum.DESCRIBE;
//                    break;
//                case "BLEND":
//                    act = MjMsgActionEnum.BLEND;
//                    break;
//            }
//            answerMessage.setResponseContent(responseBody);
//            answerMessage.setAction(act.getAction());
//            answerMessage.setDiscordImageUrl(imageUrl);
//
//            answerMessage.setDiscordStartTime(new Date(startTime));
//            answerMessage.setDiscordFinishTime(new Date(finishTime));
//            answerMessage.setFinalPrompt(prompt);
//            //if(progress == 0)
//            int mjProcess = Integer.parseInt(progress.replace("%",""));
//            if((answerMessage.getProgressing()+1) > mjProcess)
//                mjProcess = answerMessage.getProgressing() + 1;
//            if(mjProcess >= 100)
//                mjProcess = 100;
//            if(mjProcess == 100 && Integer.parseInt(progress.replace("%","")) < 100)
//                mjProcess = 99;
//
//                        answerMessage.setProgressing(mjProcess);
//            //answerMessage.setDuckId(result);
//            answerMessage.setResponseContent(description);
//            MidjourneyMsgStatusEnum statusEnum = MidjourneyMsgStatusEnum.MJ_IN_PROGRESS;
//            //状态码: 1(提交成功), 21(已存在), 22(排队中), 4(程序内部错误)
//            switch (status) {
//                case "SUBMITTED":
//                    statusEnum = MidjourneyMsgStatusEnum.MJ_IN_PROGRESS;
//                    break;
//                case "IN_PROGRESS":
//                    statusEnum = MidjourneyMsgStatusEnum.MJ_IN_PROGRESS;
//                    break;
//                case "FAILURE":
//                    statusEnum = MidjourneyMsgStatusEnum.SYS_FAILURE;
//                    answerMessage.setFailureReason(failReason+"本次服务不收费");
//                    break;
//                case "SUCCESS":
//                    statusEnum = MidjourneyMsgStatusEnum.SYS_SUCCESS;
//                    break;
//            }
//            if(statusEnum.equals(MidjourneyMsgStatusEnum.SYS_SUCCESS) && StringUtil.isNotBlank(imageUrl)){
//                //String lastName = "";
//                //if(imageUrl.substring(imageUrl.lastIndexOf("/"+1)).contains("."))
//                //    lastName = imageUrl.substring(imageUrl.lastIndexOf("."+1));
//                //            else
//                //    lastName = FileUtil.getImglastName(imageUrl);
//                //if(StringUtil.isNotBlank(lastName) && FileUtil.downloadNetFile(imageUrl, "upload" + File.separator + "mj" + File.separator + imageUrl.substring(imageUrl.lastIndexOf("/")+1)+"."+lastName)) {
//                //    String showUrl = "upload" + File.separator + "mj" + File.separator + imageUrl.substring(imageUrl.lastIndexOf("/") + 1)+"."+lastName;
//                //    answerMessage.setCompressedImageName(showUrl);
//                //}
//                fileUploadService.submitUploadTasks(answerMessage.getId(),imageUrl);//添加到对象存储
//            }
//            answerMessage.setStatus(statusEnum);
//
//            midjourneyMsgService.update(answerMessage);
//        }else{
//            System.out.println(response.message());
//            System.out.println(response.body().string());
//        }
//    }
//    @Transactional(rollbackFor = Exception.class)
//    public void timeOutTask(RoomMidjourneyMsgDO answerMessage) throws IOException, ParseException {
//
//        answerMessage.setFailureReason("执行超时，本次服务不收费");
//        answerMessage.setStatus(MidjourneyMsgStatusEnum.SYS_FAILURE);
//
//        midjourneyMsgService.update(answerMessage);
//    }
//
//    @Transactional(rollbackFor = Exception.class)
//    public void actionImg(RoomMidjourneyMsgDO answerMessage) throws IOException {
//        String action = "UPSCALE";//UPSCALE(放大); VARIATION(变换);
//        if(answerMessage.getAction().equals(MjMsgActionEnum.VARIATION))
//            action = "VARIATION";
//
//        ActionImgModel aim = new ActionImgModel();
//        aim.setAction(action);
//        aim.setIndex(answerMessage.getUvIndex());
//        aim.setState(answerMessage.getParams());
//        aim.setTaskId(""+answerMessage.getUvParentId());//父类的ducid
//        ObjectMapper objectMapper = new ObjectMapper();
//        String json =objectMapper.writeValueAsString(aim);
//        OkHttpClient client = new OkHttpClient().newBuilder()
//                .build();
//        MediaType mediaType = MediaType.parse("application/json");
//        RequestBody body = RequestBody.create(mediaType, json);
//        Request request = new Request.Builder()
//                .url(CommonEnum.mjApi.get(0).getBaseUrl() + "mj/submit/change")
//                .method("POST", body)
//                .addHeader("Authorization", "Bearer "+ CommonEnum.mjApi.get(0).getApiKey())//YOUR_API_KEY
//                .addHeader("User-Agent", "Apifox/1.0.0 (https://apifox.com)")
//                .addHeader("Content-Type", "application/json")
//                .build();
//        Response response = client.newCall(request).execute();
//        if(response.code() == 200){
//            String responseBody = response.body().string();
//            JSONObject jsonResponse = new JSONObject(responseBody);
//
//            // 提取响应中的字段值
//            int code = jsonResponse.getInt("code");
//            String description = jsonResponse.getString("description");
//            String result = jsonResponse.getString("result");
//
//            answerMessage.setProgressing(1);
//            answerMessage.setDuckId(result);
//            answerMessage.setResponseContent(description);
//            MidjourneyMsgStatusEnum statusEnum = MidjourneyMsgStatusEnum.MJ_IN_PROGRESS;
//            //状态码: 1(提交成功), 21(已存在), 22(排队中), 4(程序内部错误)
//            if(code == 4){
//                statusEnum=MidjourneyMsgStatusEnum.SYS_FAILURE;
//                answerMessage.setFailureReason("MJ内部错误，本次服务不收费");
//            }else
//                statusEnum=MidjourneyMsgStatusEnum.MJ_IN_PROGRESS;
//            answerMessage.setUpdateTime(new Date());
//            answerMessage.setStatus(statusEnum);
//            answerMessage.setAction(MjMsgActionEnum.IMAGINE.getAction());
//            midjourneyMsgService.update(answerMessage);
//        }else{
//            System.out.println(response.message());
//            System.out.println(response.body().string());
//        }
//        CommonEnum.isRunMj = true;
//    }
//    // 定义用于映射JSON的Java对象类
//    class ActionImgModel {
//        private String action;
//        private int index;
//        private String notifyHook;
//        private String state;
//        private String taskId;
//
//        // 必须提供无参构造函数
//        public ActionImgModel() {
//        }
//
//        // 提供getters和setters方法
//        public String getAction() {
//            return this.action;
//        }
//
//        public void setAction(String action) {
//            this.action = action;
//        }
//
//        public int getIndex() {
//            return this.index;
//        }
//
//        public void setIndex(int index) {
//            this.index = index;
//        }
//
//        public String getNotifyHook() {
//            return this.notifyHook;
//        }
//
//        public void setNotifyHook(String notifyHook) {
//            this.notifyHook = notifyHook;
//        }
//
//        public String getState() {
//            return this.state;
//        }
//
//        public void setState(String state) {
//            this.state = state;
//        }
//
//        public String getTaskId() {
//            return this.taskId;
//        }
//
//        public void setTaskId(String taskId) {
//            this.taskId = taskId;
//        }
//    }
//    class ImageModel {
//        private String[] base64Array;
//        private String notifyHook;
//        private String prompt;
//        private String state;
//
//        public String[] getBase64Array() {
//            return base64Array;
//        }
//
//        public void setBase64Array(String[] base64Array) {
//            this.base64Array = base64Array;
//        }
//
//        public String getNotifyHook() {
//            return notifyHook;
//        }
//
//        public void setNotifyHook(String notifyHook) {
//            this.notifyHook = notifyHook;
//        }
//
//        public String getPrompt() {
//            return prompt;
//        }
//
//        public void setPrompt(String prompt) {
//            this.prompt = prompt;
//        }
//
//        public String getState() {
//            return state;
//        }
//
//        public void setState(String state) {
//            this.state = state;
//        }
//
//        public String toJson() {
//            Gson gson = new Gson();
//            return gson.toJson(this);
//        }
//    }
//
//}
