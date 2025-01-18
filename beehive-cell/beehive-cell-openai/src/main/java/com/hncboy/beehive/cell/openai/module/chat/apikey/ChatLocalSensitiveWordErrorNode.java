package com.hncboy.beehive.cell.openai.module.chat.apikey;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Pair;
import com.hncboy.beehive.base.domain.entity.RoomOpenAiChatMsgDO;
import com.hncboy.beehive.base.handler.SensitiveWordHandler;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author ll
 * @date 2023/6/10
 * 本地敏感词库校验错误处理节点
 */
@Component
public class ChatLocalSensitiveWordErrorNode implements ChatErrorNode {

    @Override
    public Pair<Boolean, String> doHandle(RoomOpenAiChatMsgDO questionMessage, Boolean enabled) {
        // 判断是否启用
        //boolean enabled = roomConfigParamAsMap.get(OpenAiChatCellConfigCodeEnum.ENABLED_LOCAL_SENSITIVE_WORD).asBoolean();
        if (!enabled) {
            return new Pair<>(true, null);
        }

        List<String> userMessageSensitiveWords = SensitiveWordHandler.checkWord(questionMessage.getContent());
        //List<String> systemMessageSensitiveWords = null;//SensitiveWordHandler.checkWord(roomConfigParamAsMap.get(OpenAiChatCellConfigCodeEnum.SYSTEM_MESSAGE).asString());
        if (CollectionUtil.isEmpty(userMessageSensitiveWords)) {    // && CollectionUtil.isEmpty(systemMessageSensitiveWords)
            return new Pair<>(true, null);
        }

        else {  // (CollectionUtil.isNotEmpty(systemMessageSensitiveWords))
            return new Pair<>(false, String.join(",",userMessageSensitiveWords));
        }

        //return new Pair<>(false, StrUtil.format("发送失败，发送消息包含敏感词{}", userMessageSensitiveWords));
    }

    @Override
    public String getWords(String str){

        return null;
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
}
