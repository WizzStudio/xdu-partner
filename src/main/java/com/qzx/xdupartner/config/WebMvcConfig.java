package com.qzx.xdupartner.config;


import com.qzx.xdupartner.intercepter.LoginInterceptor;
import com.qzx.xdupartner.intercepter.TokenInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * <a href="https://blog.csdn.net/jerry11112/article/details/108352526">...</a>
 */
@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    public static final String[] URL_WHITELISTS = {
            "/swagger-ui.html",
            "/swagger-ui/*",
            "/swagger-resources/**",
            "/v2/api-docs",
            "/v3/api-docs",

            "/webjars/**",
            "/static/**",

            "/user/login", // todo
            "/blog/getTagWordCount", // todo
            "/blog/query**", //todo

            "/blog/query/**",

            "/blog/read",
            "/blog/readBlog",//todo
            "/blog/search*",//todo
            "/wx/user/**",
            "/verify/sendCode",//todo

    };
    @Resource
    private TokenInterceptor tokenInterceptor;
    @Resource
    private LoginInterceptor loginInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor).addPathPatterns("/**").order(0);
        registry.addInterceptor(loginInterceptor).addPathPatterns("/**").excludePathPatterns(URL_WHITELISTS).order(1);
    }
}
