package com.hncboy.beehive.web.service.impl;

import cn.hutool.core.lang.Pair;
import com.hncboy.beehive.base.enums.EmailBizTypeEnum;
import com.hncboy.beehive.base.resource.email.EmailRegisterLoginConfig;
import com.hncboy.beehive.base.resource.email.EmailUtil;
import com.hncboy.beehive.web.service.PhoneService;
import com.hncboy.beehive.web.service.SysEmailSendLogService;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.sms.v20190711.SmsClient;
import com.tencentcloudapi.sms.v20190711.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20190711.models.SendSmsResponse;
import com.tencentcloudapi.sms.v20190711.models.SendStatus;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 手机注册类型策略实现类
 *
 * @author CoDeleven
 */
@Slf4j
@Service
public class PhoneServiceImpl implements PhoneService {

    /**
     * 手机注册模板内容 sysParamKey
     */
    private static final String REGISTER_PHONE_TEMPLATE_CONTENT_SYS_PARAM_KEY = "phone-registerTemplateContent";

    @Resource
    private SysEmailSendLogService emailLogService;

    @Override
    public Pair<Boolean, String> sendForVerifyCode(String targetPhone, String verifyCode) {
        // 记录日志
        EmailRegisterLoginConfig emailRegisterLoginConfig = EmailUtil.getRegisterAccountConfig();
        //String sendContent = getSendContent(emailRegisterLoginConfig, verifyCode);
        //sendContent = sendContent.replace("${verifyCode}",verifyCode);
        //MailAccount mailAccount = EmailUtil.getMailAccount();

        try {
            boolean isSuccess = sendMessage( targetPhone, verifyCode);
            emailLogService.createSuccessLogBySysLog("", "phoneAccount,status:" + isSuccess, targetPhone, EmailBizTypeEnum.REGISTER_VERIFY, "验证码："+verifyCode);
            if(isSuccess)
                return new Pair<>(isSuccess, null);
            else
                return new Pair<>(isSuccess, "验证码发送失败，请30秒后重试，如果还是无法发送，请联系管理员");
        } catch (Exception e) {
            // 邮件发送失败
            emailLogService.createFailedLogBySysLog("","error phoneAccount", targetPhone, EmailBizTypeEnum.REGISTER_VERIFY, "验证码："+verifyCode, e.getMessage());
            return new Pair<>(false, "验证码发送失败，请30秒后重试，如果还是无法发送，请联系管理员");
        }
    }
    @Override
    public Pair<Boolean, String> sendForgetCode(String targetPhone, String verifyCode) {
        // 记录日志
        //EmailRegisterLoginConfig emailRegisterLoginConfig = EmailUtil.getRegisterAccountConfig();
        //emailRegisterLoginConfig.setRegisterTemplateSubject("【万码AI】密码找回");
        //String sendContent = getSendContent(emailRegisterLoginConfig, verifyCode);
        //sendContent = sendContent.replace("${verifyCode}",verifyCode);
        //MailAccount mailAccount = EmailUtil.getMailAccount();

        try {
            boolean isSucc = sendMessage(targetPhone, verifyCode);
            emailLogService.createSuccessLogBySysLog("", "phoneAccount，stauts："+isSucc, targetPhone, EmailBizTypeEnum.REGISTER_VERIFY, verifyCode);
            if(isSucc)
                return new Pair<>(isSucc, null);
            else
                return new Pair<>(isSucc, "验证码发送失败，请30秒后重试，如果还是无法发送，请联系管理员");
        } catch (Exception e) {
            // 验证码发送失败
            emailLogService.createFailedLogBySysLog("", "phoneAccount", targetPhone, EmailBizTypeEnum.REGISTER_VERIFY, verifyCode, e.getMessage());
            return new Pair<>(false, "验证码发送失败，请30秒后重试，如果还是无法发送，请联系管理员");
        }
    }

    /**
     * 发送消息
     *
     * @param targetPhone              目标手机号地址
     * @param code                  内容
     * @return 响应
     */
    private boolean sendMessage( String targetPhone, String code) {
        try {
            /* 必要步骤：
             * 实例化一个认证对象，入参需要传入腾讯云账户密钥对 secretId 和 secretKey
             * 本示例采用从环境变量读取的方式，需要预先在环境变量中设置这两个值
             * 您也可以直接在代码中写入密钥对，但需谨防泄露，不要将代码复制、上传或者分享给他人
             * CAM 查询：https://console.cloud.tencent.com/cam/capi
             */


            // 实例化一个 http 选项，可选，无特殊需求时可以跳过
            HttpProfile httpProfile = new HttpProfile();
            // 设置代理
            // httpProfile.setProxyHost("真实代理ip");
            // httpProfile.setProxyPort(真实代理端口);
            /* SDK 默认使用 POST 方法。
             * 如需使用 GET 方法，可以在此处设置，但 GET 方法无法处理较大的请求 */
            httpProfile.setReqMethod("POST");
            /* SDK 有默认的超时时间，非必要请不要进行调整
             * 如有需要请在代码中查阅以获取最新的默认值 */
            httpProfile.setConnTimeout(60);
            /* 指定接入地域域名，默认就近地域接入域名为 sms.tencentcloudapi.com ，也支持指定地域域名访问，例如广州地域的域名为 sms.ap-guangzhou.tencentcloudapi.com */
            httpProfile.setEndpoint("sms.tencentcloudapi.com");


            /* 非必要步骤:
             * 实例化一个客户端配置对象，可以指定超时时间等配置 */
            ClientProfile clientProfile = new ClientProfile();
            /* SDK 默认用 TC3-HMAC-SHA256 进行签名
             * 非必要请不要修改该字段 */
            clientProfile.setSignMethod("xxx");
            clientProfile.setHttpProfile(httpProfile);
            /* 实例化 SMS 的 client 对象
             * 第二个参数是地域信息，可以直接填写字符串 ap-guangzhou，或者引用预设的常量 */
            SmsClient client = new SmsClient(cred, "",clientProfile);
            /* 实例化一个请求对象，根据调用的接口和实际情况，可以进一步设置请求参数
             * 您可以直接查询 SDK 源码确定接口有哪些属性可以设置
             * 属性可能是基本类型，也可能引用了另一个数据结构
             * 推荐使用 IDE 进行开发，可以方便地跳转查阅各个接口和数据结构的文档说明 */
            SendSmsRequest req = new SendSmsRequest();


            /* 填充请求参数，这里 request 对象的成员变量即对应接口的入参
             * 您可以通过官网接口文档或跳转到 request 对象的定义处查看请求参数的定义
             * 基本类型的设置:
             * 帮助链接：
             * 短信控制台：https://console.cloud.tencent.com/smsv2
             * sms helper：https://cloud.tencent.com/document/product/382/3773 */


            /* 短信应用 ID: 在 [短信控制台] 添加应用后生成的实际 SDKAppID，例如1400006666 */
            String appid = "xxxxxx";
            req.setSmsSdkAppid(appid);


            /* 短信签名内容: 使用 UTF-8 编码，必须填写已审核通过的签名，可登录 [短信控制台] 查看签名信息 */
            String sign = "xxxxxx";
            req.setSign(sign);


            /* 国际/港澳台短信 senderid: 国内短信填空，默认未开通，如需开通请联系 [sms helper] */
            String senderid = "";
            req.setSenderId(senderid);


            /* 用户的 session 内容: 可以携带用户侧 ID 等上下文信息，server 会原样返回 */
            String session = "xxx";
            req.setSessionContext(session);


            /* 短信码号扩展号: 默认未开通，如需开通请联系 [sms helper] */
            String extendcode = "";
            req.setExtendCode(extendcode);


            /* 模板 ID: 必须填写已审核通过的模板 ID，可登录 [短信控制台] 查看模板 ID */
            String templateID = "xxxxxx";
            req.setTemplateID(templateID);


            /* 下发手机号码，采用 e.164 标准，+[国家或地区码][手机号]
             * 例如+8613711112222， 其中前面有一个+号 ，86为国家码，13711112222为手机号，最多不要超过200个手机号*/
            String[] phoneNumbers = {"+86" + targetPhone};
            req.setPhoneNumberSet(phoneNumbers);


            /* 模板参数: 若无模板参数，则设置为空*/
            String[] templateParams = {code};
            req.setTemplateParamSet(templateParams);


            /* 通过 client 对象调用 SendSms 方法发起请求。注意请求方法名与请求对象是对应的
             * 返回的 res 是一个 SendSmsResponse 类的实例，与请求对象对应 */
            SendSmsResponse res = client.SendSms(req);


            // 输出 JSON 格式的字符串回包
            //System.out.println(SendSmsResponse.toJsonString(res));
            SendStatus[] status = res.getSendStatusSet();
            emailLogService.createSuccessLogBySysLog(targetPhone+targetPhone, "phoneAccount,phone:" + status[0].getCode(), targetPhone, EmailBizTypeEnum.REGISTER_VERIFY, "requestId："+res.getRequestId());

            if(status[0].getCode().equalsIgnoreCase("Ok"))

            // 可以取出单个值，您可以通过官网接口文档或跳转到 response 对象的定义处查看返回字段的定义
            //System.out.println(res.getRequestId());

            return true;
            else
                return false;
        } catch (TencentCloudSDKException e) {
            e.printStackTrace();
            return false;
        }

    }

}
