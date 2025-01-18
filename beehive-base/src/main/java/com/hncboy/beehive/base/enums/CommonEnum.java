package com.hncboy.beehive.base.enums;

import com.hncboy.beehive.base.domain.entity.HaProductsDo;
import com.hncboy.beehive.base.domain.entity.HaVipPriceDo;
import com.hncboy.beehive.base.domain.entity.NoticeDo;
import com.hncboy.beehive.base.domain.entity.OpenAiApiKeyDO;
import com.hncboy.beehive.base.domain.vo.ModelSelectVO;
import jodd.util.StringUtil;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ll
 * @date 2023-9-3
 * 一些公共常量参数
 */
@AllArgsConstructor
public class CommonEnum {
    public static boolean isRunMj = true;
    public static boolean isWaitMj = true;
    public static boolean isTimeOutMj = true;


    public static boolean isRunRelxMj = true;
    public static boolean isWaitRelxMj = true;
    public static boolean isTimeOutRelxMj = true;


    public static List<OpenAiApiKeyDO> dalle3Api;
    public static List<OpenAiApiKeyDO> mjApi;
    public static List<OpenAiApiKeyDO> mjRelaxApi;

    public static int commonZero = 0;
    public static int commonOne = 1;    //mj-fast
    public static int commonTwo = 2;    //mj-relax
    public static int commonThree = 3;

    public static boolean isRunUpdateFree = true;
    public static boolean isRunAddFree = true;

    public static String baseUrl = "openxs.top/resources/";

    //jdOSS
    public static String bucketname = "wama";
    public static String upfile = "/upfile";
    public static String mjfile = "/mjfile";
    public static String dallefile = "/dallefile";
    public static String pictureType = "image/jpeg";
    public static String pdfType = "application/pdf";
    public static String mswordType = "application/msword";
    public static String excelType = "application/vnd.ms-excel";
    public static String xmlType = "application/xml";
    public static String jsonType = "application/json";
    public static String htmlType = "application/html";

    public static List<HaVipPriceDo> vipl = new ArrayList<>();

    public static List<ModelSelectVO> modelSelect = new ArrayList<>();
    public static List<OpenAiApiKeyDO> openApiKey = new ArrayList<>();

    public static Map<String, HaProductsDo> NAME_MAP_PRODUCT = new HashMap<>();

    public static List<NoticeDo> noticeList = new ArrayList<>();

    //所有线程开关
    public static boolean isRun = true;

    //部分有线程开关，用于区分主备服务，跟主业务关系不大的可以在非主服务器运行
    //false的话跟上面相反，备区线程开关
    public static boolean isBakRun = true;//false;true;
    //主区线程开关，跟业务关联紧密
    public static boolean isMainRun = true;//false;true;

    public static boolean isValid(Integer price,int duration) {
        for (HaVipPriceDo haVipPriceDo : vipl) {
            if (haVipPriceDo.getPrice().intValue()==price) {
                if(haVipPriceDo.getDuration().intValue() == duration)
                    return true;
            }
        }
        return false;
    }
    public static HaVipPriceDo getByPrice(Integer price) {
        for (HaVipPriceDo haVipPriceDo : vipl) {
            if (haVipPriceDo.getPrice().intValue()==price) {
                return haVipPriceDo;
            }
        }
        return null;
    }

    public static String points = "points";
    public static String vip = "vip";


    public static int isVip = 1;
    public static int noVip = 2;

    public static int isGiving = 1;
    public static int noGiving = 2;
    public static int freePoints = 200;

    public static String isGpt = "gpt";
    public static String noGpt = "no_gpt";
    public static String isGpt4_0 = "gpt-4.0";

    public static String long_time = "long_time";


    public static ConcurrentHashMap<String, String> cacheMap = new ConcurrentHashMap<>();
    public static List<String> getUrl(String str){
        List<String> imageUrls = new ArrayList<>();
        String regex = "(http(s?):)([/|.|\\w|\\s|-])*\\.(?:jpg|jpag|gif|png)";// "(http(s?):)([/|.|\\w|\\s|-])*\\.(jpg|jpeg|png|gif)";//(http(s?):)([/|.|\\w|\\s|-])*\\.(?:jpg|gif|png)
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            imageUrls.add(matcher.group(0));
        }
        return imageUrls;
    }

    public static HaProductsDo getProductByApiModel(String apiModel){

        try {
            if (RecordsEnum.showLhd != null) {
                for (HaProductsDo pds : RecordsEnum.showLhd){
                    if(StringUtil.isNotBlank(pds.getApiModel()) && pds.getApiModel().equals(apiModel))
                        return pds;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;

    }
    public static OpenAiApiKeyDO getByUseScene(String useScene){

        if(CommonEnum.openApiKey != null){
            for(OpenAiApiKeyDO apis : CommonEnum.openApiKey){
                if(StringUtil.isNotBlank(apis.getUseScenes()) && apis.getUseScenes().contains(useScene))
                    return apis;
            }
        }
        return null;
    }
}
