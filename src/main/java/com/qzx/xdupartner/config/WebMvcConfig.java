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
//    @Value("${file.request-path}")
//    private String reqPath; // 请求地址
//    @Value("${file.local-path}")
//    private String localPath; // 本地存放资源目录的绝对路径

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        File logoDir = new File(localPath);
//        boolean flag = false;
//        if (!logoDir.exists()) {
//            flag = logoDir.mkdirs();
//        }
//        if (flag) {
//            log.info("已成功创建资源 logo 目录：{}", localPath);
//        }
//
//        log.info("getAbsolutePath = {}", logoDir.getAbsolutePath());
//        log.info("getPath = {}", logoDir.getPath());

//        registry.addResourceHandler(reqPath)
//                .addResourceLocations("file:" + logoDir.getAbsolutePath() + File.separator);
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Resource
    private TokenInterceptor tokenInterceptor;
    @Resource
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(tokenInterceptor).addPathPatterns("/**").excludePathPatterns(URL_WHITELISTS).order(0);
        registry.addInterceptor(tokenInterceptor).addPathPatterns("/**").order(0);
        registry.addInterceptor(loginInterceptor).addPathPatterns("/**").excludePathPatterns(URL_WHITELISTS).order(1);
    }

    public static final String[] URL_WHITELISTS = {
            "/swagger-ui.html",
            "/swagger-ui/*",
            "/swagger-resources/**",
            "/v2/api-docs",
            "/v3/api-docs",
            "/webjars/**",
            "/static/**",
            "/user/login",
            "/blog/getTagWordCount",
            "/blog/query**",
            "/blog/readBlog",
            "/blog/search*",
//            "/api/file/upload",
//            "/upload/**"
            //以下测试结束后删掉
//            "/blog/**"
    };
}
