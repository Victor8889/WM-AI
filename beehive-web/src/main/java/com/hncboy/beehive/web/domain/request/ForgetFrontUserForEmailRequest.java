package com.hncboy.beehive.web.domain.request;

import com.hncboy.beehive.base.enums.FrontUserRegisterTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 前端用户注册请求
 *
 * @author CoDeleven
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Schema(title = "前端用户忘记密码请求，适用于找回密码")
public class ForgetFrontUserForEmailRequest {

    @Size(min = 6, max = 64, message = "用户名长度应为6~64个字符")
    @NotNull
    @Schema(title = "用户ID，可以为邮箱，可以为手机号")
    private String identity;

    @Schema(title = "密码")
    @NotNull(message = "密码不能为空")
    private String password;

    @Schema(title = "验证码")
    @NotNull(message = "验证码")
    private String code;


    private FrontUserRegisterTypeEnum registerType = FrontUserRegisterTypeEnum.EMAIL;
}
