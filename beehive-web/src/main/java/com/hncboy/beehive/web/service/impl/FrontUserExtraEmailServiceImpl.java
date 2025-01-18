package com.hncboy.beehive.web.service.impl;

import cn.hutool.crypto.digest.MD5;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hncboy.beehive.base.domain.entity.FrontUserExtraEmailDO;
import com.hncboy.beehive.base.exception.ServiceException;
import com.hncboy.beehive.base.mapper.FrontUserExtraEmailMapper;
import com.hncboy.beehive.base.util.ThrowExceptionUtil;
import com.hncboy.beehive.web.service.FrontUserExtraEmailService;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * 前端用户邮箱扩展业务实现类
 *
 * @author CoDeleven
 */
@Service
public class FrontUserExtraEmailServiceImpl extends ServiceImpl<FrontUserExtraEmailMapper, FrontUserExtraEmailDO> implements FrontUserExtraEmailService {

    @Override
    public boolean isUsed(String username) {
        FrontUserExtraEmailDO userExtraEmail = this.getOne(new LambdaQueryWrapper<FrontUserExtraEmailDO>()
                .select(FrontUserExtraEmailDO::getVerified, FrontUserExtraEmailDO::getId)
                .eq(FrontUserExtraEmailDO::getUsername, username));
        if (Objects.isNull(userExtraEmail)) {
            return false;
        }
        return userExtraEmail.getVerified();
    }

    @Override
    public FrontUserExtraEmailDO getUnverifiedEmailAccount(String identity) {
        return this.getOne(new LambdaQueryWrapper<FrontUserExtraEmailDO>()
                .eq(FrontUserExtraEmailDO::getUsername, identity).eq(FrontUserExtraEmailDO::getVerified, false));
    }

    @Override
    public FrontUserExtraEmailDO getEmailAccount(String username) {
        return this.getOne(new LambdaQueryWrapper<FrontUserExtraEmailDO>()
                .eq(FrontUserExtraEmailDO::getUsername, username));
    }

    @Override
    public void verifySuccess(FrontUserExtraEmailDO emailExtraInfo) {
        ThrowExceptionUtil.isFalse(update(new FrontUserExtraEmailDO(), new LambdaUpdateWrapper<FrontUserExtraEmailDO>()
                        .set(FrontUserExtraEmailDO::getVerified, true)
                        .eq(FrontUserExtraEmailDO::getVerified, false)
                        .eq(FrontUserExtraEmailDO::getId, emailExtraInfo.getId())))
                .throwMessage("邮箱验证码失败");
    }

    @Override
    public boolean password(Integer id, String oldpass, String newpass) {
        FrontUserExtraEmailDO fu = this.getOne(new LambdaUpdateWrapper<FrontUserExtraEmailDO>()
                .eq(FrontUserExtraEmailDO::getId,id));
        if(null != fu) {
            // 二次加密，验证账号密码
            String afterEncryptedOldPassword = encryptRawPassword(oldpass, fu.getSalt());
            if(!afterEncryptedOldPassword.equals(fu.getPassword()))
                throw new ServiceException("旧密码不正确，请重新输入");
            // 二次加密，验证账号密码
            String afterEncryptedNewPassword = encryptRawPassword(newpass, fu.getSalt());
            return this.update(new FrontUserExtraEmailDO(), new LambdaUpdateWrapper<FrontUserExtraEmailDO>()
                    .set(FrontUserExtraEmailDO::getPassword, afterEncryptedNewPassword)
                    .eq(FrontUserExtraEmailDO::getId, id));
        }else
            throw new ServiceException("系统错误，请重试");
    }
    /**
     * 给原生密码+盐进行加密
     *
     * @return 返回加密后16进制的字符串
     */
    protected String encryptRawPassword(String rawPassword, String salt) {
        return MD5.create().digestHex16(rawPassword + salt, StandardCharsets.UTF_8);
    }

}
