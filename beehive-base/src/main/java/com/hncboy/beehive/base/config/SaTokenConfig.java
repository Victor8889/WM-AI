package com.hncboy.beehive.base.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import com.hncboy.beehive.base.util.StpAdminUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author ll
 * @date 2023-3-28
 * SaToken 配置，目前针对管理端鉴权
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，校验规则为 StpUtil.checkLogin() 登录校验。
        registry.addInterceptor(new SaInterceptor(handler -> {
                // 管理端接口都必须管理端登录
                SaRouter.match("/admin/**").check(r -> StpAdminUtil.checkLogin());
                // 非管理端接口都必须 front 用户登录
                SaRouter.notMatch("/admin/**").check(r -> StpUtil.checkLogin());

                //校验积分是否充足---chat/里面分为翻译、思维导图
                //SaRouter.match("/room/openai_chat/send").check(r -> checkPoints());
                /////room/midjourney/image
                //    SaRouter.match("/room/midjourney/image").check(r -> checkPoints());
                /////room/openai_image/send
                //    SaRouter.match("/room/openai_image/send").check(r -> checkPoints());
                }))
                // 放行管理端登录接口
                .excludePathPatterns("/admin/sys_user/login")
                // 放行用户端校验邮箱验证码
                .excludePathPatterns("/user/verify_email_code")
                //找回密码--获取验证码
                .excludePathPatterns("/user/send_forget_code")
                //找回密码--获取验证码   reset_password
                .excludePathPatterns("/user/reset_password")
                // 放行用户端邮箱注册
                .excludePathPatterns("/user/register/email")
                // 放行用户端图形验证码
                .excludePathPatterns("/user/get_pic_code")
                // 放行用户端邮箱登录
                .excludePathPatterns("/user/login/email")
                // 放行资源文件
                .excludePathPatterns("/resources/**")
                // 放行model
                .excludePathPatterns("user/show_model")
                // swagger 放行
                .excludePathPatterns("/swagger-ui/**")
                .excludePathPatterns("/user/products")
                .excludePathPatterns("/user/shops")
                //微信支付回调放行
                .excludePathPatterns("/pay/wx_native")
                .excludePathPatterns("/pay/zfb_back")
                .excludePathPatterns("/v3/api-docs/**");
    }

    @Bean
    public StpLogic getStpLogicJwt() {
        return new StpLogicJwtForSimple();
    }

    //private void checkPoints() {
    //    // 获取当前请求对象
    //    SaRequestForServlet request = (SaRequestForServlet) SaHolder.getRequest();
    //    String requestBody = getRequestBody(request);
    //
    //    // 使用 Gson 将 JSON 字符串转换为 Java 对象
    //    Gson gson = new Gson();
    //    RoomRequest roomRequest = gson.fromJson(requestBody, RoomRequest.class);
    //    String roomId = roomRequest.getRoomId();
    //    System.out.println(roomId);
    //    // 单个参数值
    //    Integer userId = StpUtil.getLoginIdAsInt();
    //
    //    // 获取用户的积分余额
    //    int userPoints = getUserPoints(userId);
    //
    //    // 判断积分是否足够
    //    int requiredPoints = 50; // 假设需要50个积分
    //    if (userPoints < requiredPoints) {
    //
    //        throw new InsufficientPointsException("积分不足，请充值");// 终止请求，确保不会进入到目标方法
    //    }
    //}

    private int getUserPoints(Integer userId) {
        // TODO: 根据用户标识查询用户的积分余额
        // 假设用户的积分余额为固定值
        return 10;
    }
}
