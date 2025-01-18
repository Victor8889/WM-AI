package com.hncboy.beehive.web.controller;

import cn.hutool.core.lang.Pair;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hncboy.beehive.base.domain.bo.JwtUserInfoBO;
import com.hncboy.beehive.base.domain.entity.HaProductsDo;
import com.hncboy.beehive.base.domain.entity.HaShopsDo;
import com.hncboy.beehive.base.domain.entity.HaVipPriceDo;
import com.hncboy.beehive.base.domain.entity.NoticeDo;
import com.hncboy.beehive.base.domain.query.PageQuery;
import com.hncboy.beehive.base.domain.vo.ModelSelectVO;
import com.hncboy.beehive.base.enums.CommonEnum;
import com.hncboy.beehive.base.enums.FrontUserRegisterTypeEnum;
import com.hncboy.beehive.base.enums.RechargeEnum;
import com.hncboy.beehive.base.enums.RecordsEnum;
import com.hncboy.beehive.base.exception.ServiceException;
import com.hncboy.beehive.base.handler.response.R;
import com.hncboy.beehive.base.util.FrontUserUtil;
import com.hncboy.beehive.web.domain.request.ForgetFrontUserForEmailRequest;
import com.hncboy.beehive.web.domain.request.HaUserParamRequest;
import com.hncboy.beehive.web.domain.request.LoginFrontUserByEmailRequest;
import com.hncboy.beehive.web.domain.request.RegisterFrontUserForEmailRequest;
import com.hncboy.beehive.web.domain.vo.*;
import com.hncboy.beehive.web.service.FrontUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 前端用户控制器
 *
 * @author CoDeleven
 */
@AllArgsConstructor
@Tag(name = "用户相关接口")
@RestController
@RequestMapping("/user")
public class FrontUserController {

    private final FrontUserService frontUserService;

    @Resource
    private com.hncboy.beehive.web.service.HaUserParamService haUserParamService;

    @Resource
    private com.hncboy.beehive.web.service.HaUserPermissionsService haUserPermissionsService;
    @Resource
    private com.hncboy.beehive.web.service.HaExpenseRecordsService haExpenseRecordsService;
    @Resource
    private com.hncboy.beehive.web.service.HaRechargeRecordsService haRechargeRecordsService;
    @Resource
    private com.hncboy.beehive.web.service.HaInviteService haInviteService;
    @Resource
    private com.hncboy.beehive.web.service.HaShopsService haShopsService;

    @Operation(summary = "邮件验证回调")
    @GetMapping("/verify_email_code")   //inviteCode
    public R<Boolean> verifyEmailCode(@Parameter(description = "验证码") @RequestParam("email") String email,@Parameter(description = "验证码") @RequestParam("code") String code,@RequestParam("inviteCode") String inviteCode) {
        frontUserService.verifyCode(FrontUserRegisterTypeEnum.EMAIL,email, code,inviteCode);
        return R.data(true);
    }
    //手机号和邮箱合二为一
    @Operation(summary = "发送邮件验证")
    @GetMapping("/sen_email_code")
    public R<Boolean> sendEmailCode(@Validated @RequestBody RegisterFrontUserForEmailRequest request) {
        if(request.getIdentity().contains("@"))
            request.setRegisterType(FrontUserRegisterTypeEnum.EMAIL);
        else
            request.setRegisterType(FrontUserRegisterTypeEnum.PHONE);
        Pair<Boolean, String> registerResult = frontUserService.register(request);
        if (registerResult.getKey()) {
            return R.data(true);
        }
        // 这里抛出异常防止 service 抛出回滚
        throw new ServiceException(registerResult.getValue());
    }
    @Operation(summary = "找回密码")
    @PostMapping("/reset_password")
    public R<Boolean> resetPassword(@Validated @RequestBody ForgetFrontUserForEmailRequest request) {
        if (frontUserService.resetPassword(request)) {
            return R.data(true);
        }else
        // 这里抛出异常防止 service 抛出回滚
        throw new ServiceException("重置密码失败，请重试。");
    }

    //手机号和邮箱合二为一
    @Operation(summary = "发送邮件验证")
    @GetMapping("/send_forget_code")
    public R<Boolean> forgetEmailCode(@Parameter(description = "邮箱/手机号") @RequestParam("identity") String identity) {
        if(identity.contains("@")){
            if (frontUserService.sendCode(FrontUserRegisterTypeEnum.EMAIL, identity)) {
                return R.data(true);
            }else
                // 这里抛出异常防止 service 抛出回滚
                throw new ServiceException("发送验证码失败");
        }else{
            if (frontUserService.sendCode(FrontUserRegisterTypeEnum.PHONE, identity)) {
                return R.data(true);
            }else
                // 这里抛出异常防止 service 抛出回滚
                throw new ServiceException("发送验证码失败");
        }


    }

    //手机号和邮箱合二为一
    @Operation(summary = "邮箱注册")
    @PostMapping("/register/email")
    public R<Boolean> registerFrontUser(@Validated @RequestBody RegisterFrontUserForEmailRequest request) {
        if(request.getIdentity().contains("@"))
            request.setRegisterType(FrontUserRegisterTypeEnum.EMAIL);
        else
            request.setRegisterType(FrontUserRegisterTypeEnum.PHONE);
            Pair<Boolean, String> registerResult = frontUserService.register(request);
            if (registerResult.getKey()) {
                return R.data(true);
            }
            // 这里抛出异常防止 service 抛出回滚
            throw new ServiceException(registerResult.getValue());
        //}else{
        //    Pair<Boolean, String> registerResult = frontUserService.register(request);
        //    if (registerResult.getKey()) {
        //        return R.data(true);
        //    }
        //    // 这里抛出异常防止 service 抛出回滚
        //    throw new ServiceException(registerResult.getValue());
        //}
    }
    @Operation(summary = "手机号注册")
    @PostMapping("/register/phone")
    public R<Boolean> registerFrontUserPhone(@Validated @RequestBody RegisterFrontUserForEmailRequest request) {
        Pair<Boolean, String> registerResult = frontUserService.register(request);
        if (registerResult.getKey()) {
            return R.data(true);
        }
        // 这里抛出异常防止 service 抛出回滚
        throw new ServiceException(registerResult.getValue());
    }

    @Operation(summary = "用户信息")
    @GetMapping("/info")
    public R<UserInfoVO> getUserInfo() {

        return R.data(frontUserService.getLoginUserInfo());
    }

    @Operation(summary = "获取图片验证码")
    @GetMapping("/get_pic_code")
    public R<RegisterCaptchaVO> getPictureVerificationCode() {
        return R.data(frontUserService.generateCaptcha());
    }

    @Operation(summary = "邮箱登录")
    @PostMapping("/login/email")
    public R<LoginInfoVO> login(@RequestBody LoginFrontUserByEmailRequest request) {
        return R.data(frontUserService.login(FrontUserRegisterTypeEnum.EMAIL, request.getUsername(), request.getPassword()));
    }
    @Operation(summary = "退出")
    @GetMapping("/logout")
    public R<Boolean> logout() {
        return R.data(frontUserService.logout());
    }
    @Operation(summary = "获取聊天参数")
    @GetMapping("/setting/get_user_param")
    public R<HaUserParamRequest> getUserParam() {
        HaUserParamRequest hp = haUserParamService.getOne();
        if(hp == null)
            return R.fail("获取参数异常");
        return R.data(hp);
    }
    @Operation(summary = "更新聊天参数")
    @PostMapping("/setting/user_param")
    public R<Boolean> login(@RequestBody HaUserParamRequest haUserParamRequest) {
        if(haUserParamService.update(haUserParamRequest))
            return R.success("更新成功");
        else
            return R.fail("更新失败");
    }
    //haUserPermissionsService
    @GetMapping("/user_info")
    public R<HaUserInfoQuery> getInfo() {
        return R.data(haUserPermissionsService.getInfo());
    }

    @PostMapping("/setting/password")
    public R<Boolean> password(@RequestParam("oldpass") String oldpawss,@RequestParam("newpass") String newpass) {

        JwtUserInfoBO jwtUserInfo = FrontUserUtil.getJwtUserInfo();
        if(frontUserService.password(jwtUserInfo.getRegisterType(),oldpawss,newpass))
            return R.success("更新成功");
        else
            return R.fail("更新失败");
    }

    @Operation(summary = "消费记录分页列表")
    @GetMapping("/page_records")
    public R<IPage<HaExpenseRecordsVo>> pageRoom(@Validated PageQuery recodeQuery) {
        return R.data(haExpenseRecordsService.pageRoom(recodeQuery));
    }
    @Operation(summary = "充值记录分页列表")
    @GetMapping("/page_recharge")
    public R<IPage<HaRechargeRecordsVo>> pageRecharge(@Validated PageQuery recodeQuery) {
        return R.data(haRechargeRecordsService.pageRecharge(recodeQuery));
    }

    @Operation(summary = "充值记录分页列表")
    @GetMapping("/page_invite")
    public R<IPage<HaInviteVo>> pageInvite(@Validated PageQuery recodeQuery) {
        return R.data(haInviteService.pageInvite(recodeQuery));
    }

    @Operation(summary = "商品列表")
    @GetMapping("/shops")
    public R<List<HaShopsDo>> pageInvite() {
        return R.data(RechargeEnum.lhsd);
    }

    @Operation(summary = "价格列表")
    @GetMapping("/products")
    public R<List<HaProductsDo>> productInvite() {
        return R.data(RecordsEnum.showLhd);
    }

    @GetMapping("/show_model")
    public R<List<ModelSelectVO>> showModel() {
        return R.data(CommonEnum.modelSelect);
    }

    @Operation(summary = "VIP价格")
    @GetMapping("/vip_price")
    public R<List<HaVipPriceDo>> vipPrice() {
        return R.data(CommonEnum.vipl);
    }


    @GetMapping("/notice")
    public R<List<NoticeDo>> notice() {
        return R.data(CommonEnum.noticeList);
    }

}
