package com.hncboy.beehive.base.config;

import com.hncboy.beehive.base.annotation.ApiAdminRestController;
import com.hncboy.beehive.base.constant.ApplicationConstant;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author ll
 * @date 2023-3-27
 * WebMvc 配置
 */
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(ApplicationConstant.ADMIN_PATH_PREFIX, c -> c.isAnnotationPresent(ApiAdminRestController.class));
    }
}

