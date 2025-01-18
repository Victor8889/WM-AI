package com.hncboy.beehive.web.controller;

import com.dtflys.forest.http.HttpStatus;
import com.hncboy.beehive.base.enums.CommonEnum;
import com.hncboy.beehive.base.enums.RechargeEnum;
import com.hncboy.beehive.base.exception.ServiceException;
import com.hncboy.beehive.base.handler.response.R;
import com.hncboy.beehive.base.util.WxUtil;
import com.hncboy.beehive.web.domain.vo.PayInfoVo;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.exception.ValidationException;
import com.wechat.pay.java.core.notification.NotificationConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * @author ll
 * @date 2023-8-31
 */
@AllArgsConstructor
@Tag(name = "支付相关接口")
@RestController
@RequestMapping("/pay")
public class PayController {

    @Resource
    private com.hncboy.beehive.web.service.PayService payService;

    @GetMapping("/wx/code_url")
    public R<PayInfoVo> password(@RequestParam("amount") Integer amount, @RequestParam("points") Integer points) {

        return R.data(payService.nativeCodeUrl(amount,points));
    }

    @PostMapping("/wx_native")
    public  ResponseEntity.BodyBuilder combineTransactionCallback(
            @RequestHeader("Wechatpay-Serial") String wechatPaySerial,
            @RequestHeader("Wechatpay-Signature") String wechatSignature,
            @RequestHeader("Wechatpay-Timestamp") String wechatTimestamp,
            @RequestHeader("Wechatpay-Nonce") String wechatpayNonce,
            HttpServletRequest request) {
        try {
            byte[] bytes = request.getInputStream().readAllBytes();

        String body = new String(bytes, StandardCharsets.UTF_8);
        // 构造 RequestParam
        com.wechat.pay.java.core.notification.RequestParam requestParam = new com.wechat.pay.java.core.notification.RequestParam.Builder()
                .serialNumber(wechatPaySerial)
                .nonce(wechatpayNonce)
                .signature(wechatSignature)
                .timestamp(wechatTimestamp)
                .body(body)
                .build();

// 如果已经初始化了 RSAAutoCertificateConfig，可直接使用
// 没有的话，则构造一个
        NotificationConfig config = (NotificationConfig)WxUtil.wxConfig();
                //new RSAAutoCertificateConfig.Builder()
                //.merchantId(WxUtil.MERCHANTID)
                //.privateKeyFromPath(WxUtil.PRIVATEKEYPATH)
                //.merchantSerialNumber(WxUtil.MERCHANTSERIALNUMBER)
                //.apiV3Key(WxUtil.APIV3KEY)
                //.build();

// 初始化 NotificationParser
        NotificationParser parser = new NotificationParser(config);

        try {
            // 以支付通知回调为例，验签、解密并转换成 Transaction
            Transaction transaction = (Transaction) parser.parse(requestParam, Transaction.class);
        } catch (ValidationException e) {
            // 签名验证失败，返回 401 UNAUTHORIZED 状态码
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED);
        }

// 如果处理失败，应返回 4xx/5xx 的状态码，例如 500 INTERNAL_SERVER_ERROR
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR);
        }

// 处理成功，返回 200 OK 状态码
        return ResponseEntity.status(HttpStatus.OK);
    }
    /** 关闭订单 */
    public static void closeOrder() {

        CloseOrderRequest request = new CloseOrderRequest();
        // 调用request.setXxx(val)设置所需参数，具体参数可见Request定义
        // 调用接口
        Config config = WxUtil.wxConfig();
        // 构建service
        NativePayService service = new NativePayService.Builder().config(config).build();
        service.closeOrder(request);
    }
    /** Native支付预下单 */
    public static PrepayResponse prepay() {
        PrepayRequest request = new PrepayRequest();
        // 调用request.setXxx(val)设置所需参数，具体参数可见Request定义

        Config config = WxUtil.wxConfig();
        // 构建service
        NativePayService service = new NativePayService.Builder().config(config).build();
        // 调用接口
        return service.prepay(request);
    }
    /** 微信支付订单号查询订单 */
    public static Transaction queryOrderById() {

        QueryOrderByIdRequest request = new QueryOrderByIdRequest();
        // 调用request.setXxx(val)设置所需参数，具体参数可见Request定义

        Config config = WxUtil.wxConfig();
        // 构建service
        NativePayService service = new NativePayService.Builder().config(config).build();
        // 调用接口
        return service.queryOrderById(request);
    }
    /** 商户订单号查询订单 */
    public static Transaction queryOrderByOutTradeNo() {

        QueryOrderByOutTradeNoRequest request = new QueryOrderByOutTradeNoRequest();
        // 调用request.setXxx(val)设置所需参数，具体参数可见Request定义

        Config config = WxUtil.wxConfig();
        // 构建service
        NativePayService service = new NativePayService.Builder().config(config).build();
        // 调用接口
        return service.queryOrderByOutTradeNo(request);
    }

    /**********************************************************************************************/
    @GetMapping("/zfb/code_url")
    public R<PayInfoVo> zfbCodeUrl(@RequestParam("amount") Integer amount, @RequestParam("points") Integer points) {
        try{
            if(!RechargeEnum.isValid(amount,points))
                throw new Exception("数据不合法，请重试");
            return R.data(payService.zfbCodeUrl(amount,points));
        }catch (Exception e){
            throw new ServiceException(e.getMessage().toString());
        }
    }
    @GetMapping("/zfb/mobile_url")
    public R<PayInfoVo> zfbMobileCodeUrl(@RequestParam("amount") Integer amount, @RequestParam("points") Integer points) {
        try{
            if(!RechargeEnum.isValid(amount,points))
                throw new Exception("数据不合法，请重试");
            return R.data(payService.zfbMobileCodeUrl(amount,points));
        }catch (Exception e){
            throw new ServiceException(e.getMessage().toString());
        }
    }

    @GetMapping("/zfb/vip_code_url")
    public R<PayInfoVo> zfbVipCodeUrl(@RequestParam("amount") Integer price, @RequestParam("points") Integer duration) {
        try{
            if(!CommonEnum.isValid(price,duration))
                throw new Exception("数据不合法，请重试");
            return R.data(payService.zfbVipCodeUrl(price,duration));
        }catch (Exception e){
            throw new ServiceException(e.getMessage().toString());
        }
    }
    @GetMapping("/zfb/vip_mobile_url")
    public R<PayInfoVo> zfbVipMobileCodeUrl(@RequestParam("amount") Integer price, @RequestParam("points") Integer duration) {
        try{
            if(!CommonEnum.isValid(price,duration))
                throw new Exception("数据不合法，请重试");
            return R.data(payService.zfbVipMobileCodeUrl(price,duration));
        }catch (Exception e){
            throw new ServiceException(e.getMessage().toString());
        }
    }
    @PostMapping("/zfb_back")
    public void zfbBack(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        if(payService.zfbCallBack(request))
            out.print("success");
        else
            out.print("fail");
    }

    @GetMapping("/zfb_status")
    public  R<Boolean> zfbStatus(@RequestParam("orderId") String orderId,@RequestParam("id") Integer id,
                                 @RequestParam("type") Integer type,@RequestParam("points") Integer points) {

        return R.data(payService.zfbStatus(orderId,id,type, points));
    }

}
