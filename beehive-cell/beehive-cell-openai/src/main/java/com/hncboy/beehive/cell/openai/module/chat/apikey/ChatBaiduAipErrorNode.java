package com.hncboy.beehive.cell.openai.module.chat.apikey;

import cn.hutool.core.lang.Pair;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hncboy.beehive.base.domain.entity.RoomOpenAiChatMsgDO;
import com.hncboy.beehive.base.enums.RoomOpenAiChatMsgStatusEnum;
import com.hncboy.beehive.base.resource.aip.BaiduAipHandler;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import org.springframework.stereotype.Component;

/**
 * @author ll
 * @date 2023/6/10
 * 百度内容审核错误节点
 */
@Component
public class ChatBaiduAipErrorNode implements ChatErrorNode {

    @Override
    public Pair<Boolean, String> doHandle(RoomOpenAiChatMsgDO questionMessage, Boolean enabled) {
        // 判断是否启用
        //boolean enabled = roomConfigParamAsMap.get(OpenAiChatCellConfigCodeEnum.ENABLED_BAIDU_AIP).asBoolean();
        if (!enabled) {
            return new Pair<>(true, null);
        }

        // 获取系统消息
        //String systemMessage = roomConfigParamAsMap.get(OpenAiChatCellConfigCodeEnum.SYSTEM_MESSAGE).asString();
        // 拼接系统消息
        Pair<Boolean, String> checkTextPassPair = BaiduAipHandler.isCheckTextPass(String.valueOf(questionMessage.getId()), questionMessage.getContent());
        if (checkTextPassPair.getKey()) {
            return new Pair<>(true, null);
        }

        // 审核失败状态
        questionMessage.setStatus(RoomOpenAiChatMsgStatusEnum.CONTENT_CHECK_FAILURE);
        questionMessage.setResponseErrorData(checkTextPassPair.getValue());

        return new Pair<>(false, checkTextPassPair.getValue());
    }

    @Override
    public String getWords(String str) {
        JsonElement jsonElement = JsonParser.parseString(str);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String words = "";
        JsonArray dataArray = jsonObject.getAsJsonArray("data");
        for (JsonElement element : dataArray) {
            JsonObject dataObject = element.getAsJsonObject();
            if (dataObject.has("hits")) {
                JsonArray hitsArray = dataObject.getAsJsonArray("hits");
                for (JsonElement hitElement : hitsArray) {
                    JsonObject hitObject = hitElement.getAsJsonObject();
                    if (hitObject.has("words")) {
                        JsonArray wordsArray = hitObject.getAsJsonArray("words");
                        if (null != wordsArray && !wordsArray.isEmpty())
                            for(int i=0;i<wordsArray.size();i++){
                                words += wordsArray.get(i).toString().replaceAll("\"","") + ",";
                            }
                    }
                }
            }
        }
        if(words.endsWith(","))
            return words.substring(0,words.length());
        else
            return words;
    }

    HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();

    @Override
    public  String getFirstLetter(String str) {
        StringBuffer pybf = new StringBuffer();
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        if(str.matches("[a-zA-Z]+")){
            pybf.append(str.substring(0,1));
        }else {
            char[] arr = str.toCharArray();
            for (int i = 0; i < arr.length; i++) {
                //if (arr[i] > 128) {
                //匹配是否是汉字
                if (Character.toString(arr[i]).matches("[\\u4E00-\\u9FA5]+")) {
                    try {
                        //获取汉字拼音字符串数组
                        String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(arr[i]);
                        if (pinyinArray != null) {
                            pybf.append(pinyinArray[0].charAt(0));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    pybf.append(arr[i]);
                }
            }
        }
        return pybf.toString();
    }

    public  Boolean checkImgPass(String url, Boolean enabled) {
        if (!enabled) {
            return true;
        }

        // 获取系统消息
        //String systemMessage = roomConfigParamAsMap.get(OpenAiChatCellConfigCodeEnum.SYSTEM_MESSAGE).asString();
        // 拼接系统消息
        Pair<Boolean, String> checkTextPassPair = BaiduAipHandler.isCheckImgPass("图片检测", url);
        if (checkTextPassPair.getKey()) {
            return true;
        }

        return false;
    }
}

