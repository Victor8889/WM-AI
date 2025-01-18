package com.hncboy.beehive.web.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.hncboy.beehive.base.domain.entity.HaRechargeRecordsDo;
import com.hncboy.beehive.base.domain.entity.HaShopsDo;
import com.hncboy.beehive.base.domain.entity.HaVipPriceDo;
import com.hncboy.beehive.base.enums.CommonEnum;
import com.hncboy.beehive.base.enums.PayStatusEnum;
import com.hncboy.beehive.base.enums.PayTypeEnum;
import com.hncboy.beehive.base.enums.RechargeEnum;
import com.hncboy.beehive.base.util.FileUtil;
import com.hncboy.beehive.base.util.QrUtil;
import com.hncboy.beehive.base.util.WxUtil;
import com.hncboy.beehive.base.util.ZfbUtil;
import com.hncboy.beehive.web.domain.vo.PayInfoVo;
import com.hncboy.beehive.web.service.PayService;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.*;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author ll
 * @date 2023-8-31
 */

@Service
@AllArgsConstructor
public class PayServiceImpl implements PayService {

    @Resource
    private com.hncboy.beehive.web.service.HaRechargeRecordsService haRechargeRecordsService;

    @Resource
    private com.hncboy.beehive.web.service.HaUserPermissionsService haUserPermissionsService;

    @Override
    public Map createNative(Long orderId) throws Exception {
        return null;
    }
    @Override
    public PayInfoVo nativeCodeUrl(int amt, int points){
        Config config = WxUtil.wxConfig();
                //new RSAAutoCertificateConfig.Builder()
                //        .merchantId(WxUtil.MERCHANTID)
                //        .privateKeyFromPath(WxUtil.PRIVATEKEYPATH)
                //        .merchantSerialNumber(WxUtil.MERCHANTSERIALNUMBER)
                //        .apiV3Key(WxUtil.APIV3KEY)
                //        .build();
        // 构建service
        String orderId = "out_trade_no_001";
        NativePayService service = new NativePayService.Builder().config(config).build();
        // request.setXxx(val)设置所需参数，具体参数可见Request定义
        PrepayRequest request = new PrepayRequest();
        Amount amount = new Amount();
        amount.setTotal(amt);//订单金额
        request.setAmount(amount);
        request.setAppid(WxUtil.APPID);
        request.setMchid(WxUtil.MCHID);
        request.setDescription("测试商品标题");
        request.setNotifyUrl(WxUtil.NAVITENOTIFYURL);
        request.setOutTradeNo(orderId);//订单号
        // 调用下单方法，得到应答
        PrepayResponse response = service.prepay(request);
        // 使用微信扫描 code_url 对应的二维码，即可体验Native支付
        System.out.println(response.getCodeUrl());
        String qrFilePathName = FileUtil.getFileSavePathPrefix() + File.separator + "wxpay" + File.separator + orderId + ".png";
        if(QrUtil.createCodeToFile(response.getCodeUrl(),qrFilePathName)){
            PayInfoVo piv = new PayInfoVo();
            piv.setCodeUrl( File.separator + "wxpay" + File.separator + orderId + ".png");
            piv.setOrderId(orderId);
            return piv;
        }
        else
            return null;
    }

    public boolean wxOrderState(){
        Config config =WxUtil.wxConfig();
        // 构建service
        NativePayService service = new NativePayService.Builder().config(config).build();

        QueryOrderByIdRequest queryRequest = new QueryOrderByIdRequest();
        queryRequest.setMchid("190000****");
        queryRequest.setTransactionId("4200001569202208304701234567");

        try {
            Transaction result = service.queryOrderById(queryRequest);
            System.out.println(result.getTradeState());
        } catch (ServiceException e) {
            // API返回失败, 例如ORDER_NOT_EXISTS
            System.out.printf("code=[%s], message=[%s]\n", e.getErrorCode(), e.getErrorMessage());
            System.out.printf("reponse body=[%s]\n", e.getResponseBody());
        }
        return true;
    }
    public boolean wxCloseOrder(){
        Config config =WxUtil.wxConfig();
        // 构建service
        NativePayService service = new NativePayService.Builder().config(config).build();
        CloseOrderRequest closeRequest = new CloseOrderRequest();
        closeRequest.setMchid("190000****");
        closeRequest.setOutTradeNo("out_trade_no_001");
// 方法没有返回值，意味着成功时API返回204 No Content
        service.closeOrder(closeRequest);
        return true;
    }
    /*********************************************************************************************************/
    /********支付宝相关**********/
    /*********************************************************************************************************/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayInfoVo zfbCodeUrl(int amount, int points)throws Exception {

            String orderId =  getOrderId();
            int id = haRechargeRecordsService.save(amount,points, PayTypeEnum.PCALIPAY,orderId,amount + "元" + points + "积分",CommonEnum.points);
            if( id <= 0)
                throw new Exception("创建订单失败，请重试。");

            String product = amount + "元" + points + "积分";

            AlipayClient alipayClient = ZfbUtil.zfbClinet();
            //AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            request.setNotifyUrl(ZfbUtil.NOTIFYURL);
            JSONObject bizContent = new JSONObject();
            //商户订单号，商家自定义，保持唯一性
            bizContent.put("out_trade_no", orderId);
            //支付金额，最小值0.01元
            bizContent.put("total_amount", amount);   //amount / 100
            //订单标题，不可使用特殊符号
            bizContent.put("subject", product);
            //电脑网站支付场景固定传值FAST_INSTANT_TRADE_PAY
            bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
            bizContent.put("qr_pay_mode","4");
            bizContent.put("qrcode_width",200);

            request.setBizContent(bizContent.toString());
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
            if (response.isSuccess()) {
                String form = response.getBody();

                HaShopsDo re = RechargeEnum.getByamount(amount);
                if(re == null)
                    throw new Exception("数据不合法，请重试");
                PayInfoVo piv = new PayInfoVo();
                piv.setCodeUrl(form);
                piv.setId(id);
                piv.setOrderId(orderId);
                piv.setPoints(re.getPoints());
                return piv;
            } else
                return null;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayInfoVo zfbMobileCodeUrl(int amount, int points) throws Exception {

        String orderId =  getOrderId();
        int id = haRechargeRecordsService.save(amount,points, PayTypeEnum.MOBILEALIPAY,orderId,amount + "元" + points + "积分",CommonEnum.points);
        if( id <= 0)
            throw new Exception("创建订单失败，请重试。");

        String product = amount + "元" + points + "积分";

        AlipayClient alipayClient = ZfbUtil.zfbClinet();
        //AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest ();

        JSONObject bizContent = new JSONObject();
        //异步接收地址，仅支持http/https，公网可访问
        request.setNotifyUrl(ZfbUtil.NOTIFYURL);
        //同步跳转地址，仅支持http/https
        request.setReturnUrl(ZfbUtil.RETURNURL);
        //商户订单号，商家自定义，保持唯一性
        bizContent.put("out_trade_no", orderId);
        //支付金额，最小值0.01元
        bizContent.put("total_amount", amount);   //amount / 100
        //订单标题，不可使用特殊符号
        bizContent.put("subject", product);
        bizContent.put("quit_url", ZfbUtil.RETURNURL);
        //电脑网站支付场景固定传值FAST_INSTANT_TRADE_PAY
        bizContent.put("product_code", "QUICK_WAP_WAY");
        //bizContent.put("qr_pay_mode","4");
        //bizContent.put("qrcode_width",200);

        request.setBizContent(bizContent.toString());
        AlipayTradeWapPayResponse response = alipayClient.pageExecute(request);
        if (response.isSuccess()) {
            String form = response.getBody();

            HaShopsDo re = RechargeEnum.getByamount(amount);
            if(re == null)
                throw new Exception("数据不合法，请重试");
            PayInfoVo piv = new PayInfoVo();
            piv.setCodeUrl(form);
            piv.setId(id);
            piv.setOrderId(orderId);
            piv.setPoints(re.getPoints());
            return piv;
        } else
            return null;
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayInfoVo zfbVipCodeUrl(int price, int duration)throws Exception {

        String orderId =  getOrderId();//支付订单号
        HaVipPriceDo re = CommonEnum.getByPrice(price);
        if(re == null)
            throw new Exception("数据不合法，请重试");
        String product = re.getPrice()+"元/"+re.getMark();
        int id = haRechargeRecordsService.save(price,duration, PayTypeEnum.PCALIPAY,orderId,product,CommonEnum.vip);
        if( id <= 0)
            throw new Exception("创建订单失败，请重试。");


        AlipayClient alipayClient = ZfbUtil.zfbClinet();
        //AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(ZfbUtil.NOTIFYURL);
        JSONObject bizContent = new JSONObject();
        //商户订单号，商家自定义，保持唯一性
        bizContent.put("out_trade_no", orderId);
        //支付金额，最小值0.01元
        bizContent.put("total_amount", price);   //amount / 100
        //订单标题，不可使用特殊符号
        bizContent.put("subject", product);
        //电脑网站支付场景固定传值FAST_INSTANT_TRADE_PAY
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        bizContent.put("qr_pay_mode","4");
        bizContent.put("qrcode_width",200);

        request.setBizContent(bizContent.toString());
        AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
        if (response.isSuccess()) {
            String form = response.getBody();

            PayInfoVo piv = new PayInfoVo();
            piv.setCodeUrl(form);
            piv.setId(id);
            piv.setOrderId(orderId);
            piv.setPoints(re.getDuration());
            return piv;
        } else
            return null;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayInfoVo zfbVipMobileCodeUrl(int price, int duration) throws Exception {

        String orderId =  getOrderId();
        HaVipPriceDo re = CommonEnum.getByPrice(price);
        if(re == null)
            throw new Exception("数据不合法，请重试");
        String product = re.getPrice()+"元/"+re.getMark();
        int id = haRechargeRecordsService.save(price,duration, PayTypeEnum.PCALIPAY,orderId,product,CommonEnum.vip);
        if( id <= 0)
            throw new Exception("创建订单失败，请重试。");

        AlipayClient alipayClient = ZfbUtil.zfbClinet();
        //AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest ();

        JSONObject bizContent = new JSONObject();
        //异步接收地址，仅支持http/https，公网可访问
        request.setNotifyUrl(ZfbUtil.NOTIFYURL);
        //同步跳转地址，仅支持http/https
        request.setReturnUrl(ZfbUtil.RETURNURL);
        //商户订单号，商家自定义，保持唯一性
        bizContent.put("out_trade_no", orderId);
        //支付金额，最小值0.01元
        bizContent.put("total_amount", price);   //amount / 100
        //订单标题，不可使用特殊符号
        bizContent.put("subject", product);
        bizContent.put("quit_url", ZfbUtil.RETURNURL);
        //电脑网站支付场景固定传值FAST_INSTANT_TRADE_PAY
        bizContent.put("product_code", "QUICK_WAP_WAY");
        //bizContent.put("qr_pay_mode","4");
        //bizContent.put("qrcode_width",200);

        request.setBizContent(bizContent.toString());
        AlipayTradeWapPayResponse response = alipayClient.pageExecute(request);
        if (response.isSuccess()) {
            String form = response.getBody();

            PayInfoVo piv = new PayInfoVo();
            piv.setCodeUrl(form);
            piv.setId(id);
            piv.setOrderId(orderId);
            piv.setPoints(re.getDuration());
            return piv;
        } else
            return null;
    }
    /**
     * 不做实际处理
     * 不修改状态
     * @param request
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean zfbCallBack(HttpServletRequest request) {
        try {
            //获取支付宝POST过来反馈信息
            Map<String, String> params = new HashMap<String, String>();
            Map requestParams = request.getParameterMap();
            for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
                String name = (String) iter.next();
                String[] values = (String[]) requestParams.get(name);
                String valueStr = "";
                for (int i = 0; i < values.length; i++) {
                    valueStr = (i == values.length - 1) ? valueStr + values[i]
                            : valueStr + values[i] + ",";
                }
                //乱码解决，这段代码在出现乱码时使用。
                //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
                params.put(name, valueStr);
            }

//异步验签：切记alipaypublickey是支付宝的公钥，请去open.alipay.com对应应用下查看。
//公钥证书模式验签，alipayPublicCertPath是支付宝公钥证书引用路径地址，需在对应应用中下载
//boolean signVerified= AlipaySignature.rsaCertCheckV1(params, alipayPublicCertPath, charset,sign_type);
//普通公钥模式验签，切记alipaypublickey是支付宝的公钥，请去open.alipay.com对应应用下查看。
//        boolean flag = AlipaySignature.rsaCheckV1(params, alipaypublicKey, charset,"RSA2");
            boolean signVerified = AlipaySignature.rsaCertCheckV1(params, ZfbUtil.ALIPAYPUBLICCERTPATH, ZfbUtil.CHARSET, ZfbUtil.SIGN_TYPE);

/* 实际验证过程建议商户务必添加以下校验：
    1、需要验证该通知数据中的out_trade_no是否为商户系统中创建的订单号，
    2、判断total_amount是否确实为该订单的实际金额（即商户订单创建时的金额），
    3、校验通知中的seller_id（或者seller_email) 是否为out_trade_no这笔单据的对应的操作方（有的时候，一个商户可能有多个seller_id/seller_email）
    4、验证app_id是否为该商户本身。
    */
            if (signVerified) {//验证成功
                //商户订单号
                String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"), "UTF-8");
                //支付宝交易号
                String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"), "UTF-8");
                //交易状态
                String trade_status = new String(request.getParameter("trade_status").getBytes("ISO-8859-1"), "UTF-8");

                //判断该笔订单是否在商户网站中已经做过处理，此状态表示需要处理
                HaRechargeRecordsDo haRechargeRecordsDo = new HaRechargeRecordsDo();
                haRechargeRecordsDo.setOrderId(out_trade_no);
                haRechargeRecordsDo.setTradeNo(trade_no);
                haRechargeRecordsDo.setPayMark(trade_status);

                haRechargeRecordsService.updateByOrderId(haRechargeRecordsDo);
                return true;
            } else {//验证失败
                return false;
            }
        }catch (Exception e){
            return false;
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean zfbStatus(String orderId,int id, int type,int points) {
        try {
            //从ali获取状态，更新数据库
            AlipayClient alipayClient = ZfbUtil.zfbClinet();
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            JSONObject bizContent = new JSONObject();
            //商户订单号，商家自定义，保持唯一性
            bizContent.put("out_trade_no", orderId);

            request.setBizContent(bizContent.toString());

            AlipayTradeQueryResponse response = alipayClient.certificateExecute(request);
            if (response.isSuccess()) {
                //String content = response.getBody();
                //String sign = response.get
                //boolean signVerified = AlipaySignature.rsaCertCheck(content, sign, ZfbUtil.ALIPAYPUBLICCERTPATH, ZfbUtil.CHARSET, ZfbUtil.SIGN_TYPE);

                String trade_no = response.getTradeNo();
                String buyer_logon_id = response.getBuyerLogonId() + "、" + response.getBuyerUserName() +"、" + response.getBuyerPayAmount();
                String trade_status = response.getTradeStatus();
                if("TRADE_SUCCESS".equals(trade_status) || "TRADE_FINISHED".equals(trade_status)){
                    HaRechargeRecordsDo haRechargeRecordsDo = new HaRechargeRecordsDo();
                    haRechargeRecordsDo.setStatus(PayStatusEnum.SUCCESS);
                    haRechargeRecordsDo.setBuyerLogonId(buyer_logon_id);
                    //haRechargeRecordsDo.setOrderId(orderId);
                    haRechargeRecordsDo.setTradeNo(trade_no);
                    haRechargeRecordsDo.setIsAddPoints(0);
                    haRechargeRecordsDo.setId(id);

                    if(haRechargeRecordsService.updateById(haRechargeRecordsDo)) {


                        return true;
                    }else
                        return false;
                }
            } else {
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return false;
    }


    private String getOrderId() {
        // 步骤1：创建一个Calendar对象
        Calendar calendar = Calendar.getInstance();

        // 步骤2：获取当前时间的秒数
        int seconds = calendar.get(Calendar.SECOND);

        // 步骤3：获取当前时间的微秒数
        long currentTimeMillis = System.currentTimeMillis();
        int microseconds = (int) (currentTimeMillis % 1000) * 1000 + calendar.get(Calendar.MILLISECOND);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");

        return sdf.format(new Date()) + microseconds;
    }
}
