package com.hncboy.beehive.web.service.strategy.user;

import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.hncboy.beehive.base.constant.ApplicationConstant;
import com.hncboy.beehive.base.constant.HaUserConstant;
import com.hncboy.beehive.base.domain.entity.*;
import com.hncboy.beehive.base.enums.EmailBizTypeEnum;
import com.hncboy.beehive.base.enums.FrontUserRegisterTypeEnum;
import com.hncboy.beehive.base.enums.FrontUserStatusEnum;
import com.hncboy.beehive.base.enums.PayStatusEnum;
import com.hncboy.beehive.base.exception.ServiceException;
import com.hncboy.beehive.base.resource.email.EmailRegisterLoginConfig;
import com.hncboy.beehive.base.resource.email.EmailUtil;
import com.hncboy.beehive.base.util.WebUtil;
import com.hncboy.beehive.web.domain.request.ForgetFrontUserForEmailRequest;
import com.hncboy.beehive.web.domain.request.RegisterFrontUserForEmailRequest;
import com.hncboy.beehive.web.domain.vo.LoginInfoVO;
import com.hncboy.beehive.web.domain.vo.UserInfoVO;
import com.hncboy.beehive.web.service.*;
import jakarta.annotation.Resource;
import jodd.util.StringUtil;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

import static com.hncboy.beehive.base.constant.ApplicationConstant.FRONT_JWT_EXTRA_USER_ID;
import static com.hncboy.beehive.base.constant.ApplicationConstant.FRONT_JWT_USERNAME;

/**
 * 邮箱注册策略
 *
 * @author CoDeleven
 */
@Lazy
@Component("EmailRegisterStrategy")
public class EmailAbstractRegisterStrategy extends AbstractRegisterTypeStrategy {
    @Resource
    private HaRechargeRecordsService haRechargeRecordsService;
    @Resource
    private com.hncboy.beehive.web.service.HaUserParamService haUserParamService;
    @Resource
    private com.hncboy.beehive.web.service.HaUserPermissionsService haUserPermissionsService;
    @Resource
    private FrontUserExtraEmailService userExtraEmailService;

    @Resource
    private FrontUserBaseService baseUserService;

    @Resource
    private EmailVerifyCodeService emailVerifyCodeService;

    @Resource
    private FrontUserExtraBindingService bindingService;

    @Resource
    private EmailService emailService;
    @Resource
    private PhoneService phoneService;

    @Resource
    private SysFrontUserLoginLogService loginLogService;

    @Override
    public boolean identityUsed(String identity) {
        return userExtraEmailService.isUsed(identity);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void checkVerifyCode(String identity, String verifyCode,String inviteCode) {
        // 校验邮箱验证码
        EmailVerifyCodeDO availableVerifyCode = emailVerifyCodeService.findAvailableByVerifyCode(identity,verifyCode);
        if (Objects.isNull(availableVerifyCode)) {
            throw new ServiceException("验证码不存在或已过期，请重新发起...");
        }
        // 验证通过，生成基础用户信息并做绑定
        FrontUserBaseDO baseUser = baseUserService.createEmptyBaseUser();
        // 获取邮箱信息表
        FrontUserExtraEmailDO emailExtraInfo = userExtraEmailService.getUnverifiedEmailAccount(availableVerifyCode.getToEmailAddress());
        // 绑定两张表
        bindingService.bindEmail(baseUser, emailExtraInfo);
        // 验证完毕，写入日志
        emailVerifyCodeService.verifySuccess(availableVerifyCode);
        // 设置邮箱已验证
        userExtraEmailService.verifySuccess(emailExtraInfo);
        //保存用户参数、保存用户权限
        haUserParamService.save(baseUser.getId());
        if(StringUtil.isNotBlank(inviteCode)) {
            HaUserPermissionsDo hpd = haUserPermissionsService.getOneByInvite(Integer.parseInt(inviteCode));
            if(null != hpd)
                haUserPermissionsService.saveOtherId(baseUser.getId(),hpd.getUserId());
        }
        else
            haUserPermissionsService.save(baseUser.getId());
        HaRechargeRecordsDo haRechargeRecordsDo = new HaRechargeRecordsDo();
        haRechargeRecordsDo.setStatus(PayStatusEnum.NOTHING);
        haRechargeRecordsDo.setMark("注册赠送"+ HaUserConstant.DEFAULT_HOLD_BI);
        haRechargeRecordsDo.setIsAddPoints(1);
        haRechargeRecordsDo.setAmount(0);
        haRechargeRecordsDo.setPoints(HaUserConstant.DEFAULT_HOLD_BI);
        haRechargeRecordsDo.setPayResult("新用户注册赠送");
        haRechargeRecordsDo.setUserId(baseUser.getId());
        haRechargeRecordsService.save(haRechargeRecordsDo);
    }

    @Override
    public boolean resetPassword(ForgetFrontUserForEmailRequest request) {
        // 校验邮箱验证码
        EmailVerifyCodeDO availableVerifyCode = emailVerifyCodeService.findAvailableByVerifyCode(request.getIdentity(),request.getCode());
        if (Objects.isNull(availableVerifyCode)) {
            throw new ServiceException("验证码不存在或已过期，请重新发起...");
        }
        // 获取邮箱信息表
        FrontUserExtraEmailDO emailExtraInfo = userExtraEmailService.getEmailAccount(availableVerifyCode.getToEmailAddress());

        //验证通过，修改密码。
        String newPassword = encryptRawPassword(request.getPassword(), emailExtraInfo.getSalt());

        emailExtraInfo.setPassword(newPassword);
        return userExtraEmailService.updateById(emailExtraInfo);

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Pair<Boolean, String> register(RegisterFrontUserForEmailRequest request) {
        // 校验邮箱注册权限
        EmailRegisterLoginConfig emailRegisterAccountConfig = EmailUtil.getRegisterAccountConfig();
        emailRegisterAccountConfig.checkRegisterPermission(request.getIdentity());

        // 查找邮箱账号是否存在
        FrontUserExtraEmailDO existsEmailDO = userExtraEmailService.getUnverifiedEmailAccount(request.getIdentity());
        String salt = RandomUtil.randomString(6);
        // 构建新的邮箱信息
        if (Objects.isNull(existsEmailDO)) {
            existsEmailDO = FrontUserExtraEmailDO.builder()
                    .password(this.encryptRawPassword(request.getPassword(), salt))
                    .salt(salt)
                    .username(request.getIdentity())
                    .verified(false)
                    .build();
            // 存储邮箱信息
            userExtraEmailService.save(existsEmailDO);
        } else {
            // 在未使用的邮箱基础上更新下密码信息，然后重新投入使用
            existsEmailDO.setSalt(salt);
            existsEmailDO.setVerified(false);
            existsEmailDO.setPassword(this.encryptRawPassword(request.getPassword(), salt));
            // 存储邮箱信息
            userExtraEmailService.updateById(existsEmailDO);
        }
        // 存储验证码记录
        EmailVerifyCodeDO emailVerifyCodeDO = emailVerifyCodeService.createVerifyCode(EmailBizTypeEnum.REGISTER_VERIFY, request.getIdentity());

        if(request.getIdentity().contains("@"))
        // 发送邮箱验证信息
            return emailService.sendForVerifyCode(request.getIdentity(), emailVerifyCodeDO.getVerifyCode());
        else
            return phoneService.sendForVerifyCode(request.getIdentity(), emailVerifyCodeDO.getVerifyCode());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean sendCode(String identity){
        // 校验手机号|邮箱注册权限
        EmailRegisterLoginConfig emailRegisterAccountConfig = EmailUtil.getRegisterAccountConfig();
        emailRegisterAccountConfig.checkLoginPermission(identity);

        // 查找邮箱账号是否存在
        FrontUserExtraEmailDO existsEmailDO = userExtraEmailService.getEmailAccount(identity);
        if(null == existsEmailDO)
            throw new ServiceException("账号不存在");
        //验证是否有绑定信息，无绑定信息直接返回，让用户从新注册
        FrontUserExtraBindingDO fuebd= bindingService.findExtraBinding(FrontUserRegisterTypeEnum.EMAIL,existsEmailDO.getId());
        if(null == fuebd)
            throw new ServiceException("此账号之前未完成注册，请重新注册");
        // 存储验证码记录
        EmailVerifyCodeDO emailVerifyCodeDO = emailVerifyCodeService.createVerifyCode(EmailBizTypeEnum.REGISTER_VERIFY, identity);
        Pair<Boolean, String> pb = null;
        if(identity.contains("@"))
        // 发送邮箱验证信息
            pb = emailService.sendForgetCode(identity, emailVerifyCodeDO.getVerifyCode());
        else
           pb = phoneService.sendForgetCode(identity, emailVerifyCodeDO.getVerifyCode());
         return pb.getKey();
    }
    @Override
    public UserInfoVO getLoginUserInfo(Integer extraInfoId) {
        FrontUserExtraEmailDO extraEmailDO = userExtraEmailService.getById(extraInfoId);

        // 根据注册类型 + extraInfoId 获取 当前邮箱绑定在了哪个用户上
        FrontUserExtraBindingDO bindingRelations = bindingService.findExtraBinding(FrontUserRegisterTypeEnum.EMAIL, extraInfoId);
        if (Objects.isNull(bindingRelations)) {
            throw new ServiceException(StrUtil.format("注册方式：{} 额外信息ID：{} 绑定关系不存在",
                    FrontUserRegisterTypeEnum.EMAIL.getDesc(), extraInfoId));
        }
        // 根据绑定关系查找基础用户信息
        FrontUserBaseDO frontUserBaseDO = baseUserService.findUserInfoById(bindingRelations.getBaseUserId());
        if (Objects.isNull(frontUserBaseDO)) {
            throw new ServiceException(StrUtil.format("基础用户不存在：{}", bindingRelations.getBaseUserId()));
        }

        // 封装基础用户信息并返回
        return UserInfoVO.builder().baseUserId(frontUserBaseDO.getId())
                .description(frontUserBaseDO.getDescription())
                .nickname(frontUserBaseDO.getNickname())
                .email(extraEmailDO.getUsername())
                .status(frontUserBaseDO.getStatus())
                .avatarUrl("").build();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public LoginInfoVO login(String username, String password) {
        // 校验邮箱登录权限
        EmailRegisterLoginConfig emailRegisterAccountConfig = EmailUtil.getRegisterAccountConfig();
        emailRegisterAccountConfig.checkLoginPermission(username);

        // 验证账号信息
        FrontUserExtraEmailDO emailDO = userExtraEmailService.getEmailAccount(username);
        if (Objects.isNull(emailDO) || BooleanUtil.isFalse(emailDO.getVerified())) {
            throw new ServiceException("账号未注册");
        }

        // 二次加密，验证账号密码
        String afterEncryptedPassword = encryptRawPassword(password, emailDO.getSalt());

        // 密码不一致的情况
        if (ObjectUtil.notEqual(afterEncryptedPassword, emailDO.getPassword())) {
            Integer baseUserId = 0;
            // 获取绑定的基础用户 id
            FrontUserExtraBindingDO userExtraBindingDO = bindingService.findExtraBinding(FrontUserRegisterTypeEnum.EMAIL, emailDO.getId());
            if (Objects.nonNull(userExtraBindingDO)) {
                FrontUserBaseDO frontUserBaseDO = baseUserService.findUserInfoById(userExtraBindingDO.getBaseUserId());
                if (Objects.nonNull(frontUserBaseDO)) {
                    baseUserId = frontUserBaseDO.getId();
                }
            }

            // 记录登录失败日志
            loginLogService.loginFailed(FrontUserRegisterTypeEnum.EMAIL, emailDO.getId(), baseUserId, "账号或密码错误");
            throw new ServiceException("账号或密码错误");
        }

        // 获取登录用户信息
        UserInfoVO userInfoVO = getLoginUserInfo(emailDO.getId());

        if("120.41.145.179,121.41.171.230,121.41.38.112".contains(WebUtil.getIp())){
            loginLogService.loginFailed(FrontUserRegisterTypeEnum.EMAIL, emailDO.getId(), userInfoVO.getBaseUserId(), "用户被禁止登录，封禁IP");
            throw new ServiceException("您的账号异常，请联系管理员");
        }
        // 判断用户状态
        if (userInfoVO.getStatus() == FrontUserStatusEnum.BLOCK) {
            // 记录登录失败日志
            loginLogService.loginFailed(FrontUserRegisterTypeEnum.EMAIL, emailDO.getId(), userInfoVO.getBaseUserId(), "用户被禁止登录");
            throw new ServiceException("您已经被禁止登录，有问题请联系管理员");
        } else if (userInfoVO.getStatus() == FrontUserStatusEnum.WAIT_CHECK) {
            // 记录登录失败日志
            loginLogService.loginFailed(FrontUserRegisterTypeEnum.EMAIL, emailDO.getId(), userInfoVO.getBaseUserId(), "用户等待审核");
            throw new ServiceException("您的账号等待管理员审核");
        }

        // 执行登录
        StpUtil.login(userInfoVO.getBaseUserId(), SaLoginModel.create()
                .setExtra(FRONT_JWT_USERNAME, emailDO.getUsername())
                .setExtra(ApplicationConstant.FRONT_JWT_REGISTER_TYPE_CODE, FrontUserRegisterTypeEnum.EMAIL.getCode())
                .setExtra(FRONT_JWT_EXTRA_USER_ID, emailDO.getId()));

        // 记录登录日志
        loginLogService.loginSuccess(FrontUserRegisterTypeEnum.EMAIL, emailDO.getId(), userInfoVO.getBaseUserId());

        return LoginInfoVO.builder().token(StpUtil.getTokenValue()).baseUserId(userInfoVO.getBaseUserId()).build();
    }

    @Override
    public boolean password(String oldpass, String newpass) {
       int emailId =  bindingService.getEmailUserId();
       if(emailId > 0)
        userExtraEmailService.password(emailId,oldpass,newpass);
       else
           throw new ServiceException("用户不存在，请登录后重试");
        return true;
    }



}
