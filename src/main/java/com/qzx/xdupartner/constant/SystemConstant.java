package com.qzx.xdupartner.constant;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SystemConstant {
    public static final int RANDOM_TTL_MIN = 300;
    public static final int RANDOM_TTL_MAX = 360;
    @Value("${urlPrefix}")
    public String urlPrefix;

    @PostConstruct
    private void init() {
        URL_PREFIX = urlPrefix;
    }
    public static final int RANDOM_ICON_MIN = 1;
    public static final int RANDOM_ICON_MAX = 6;
    public static String URL_PREFIX;
    public static final long MB = 1024 * 1024;
    public static final int MAX_PAGE_SIZE = 10;
    public static final int LIKE_PAGE_SIZE = 6;
    public static final String ANONYMOUS_ICON = "upload/image/defaultIcon.png";
    public static final String LOW_TAG_CONJUNCTION = "_lowTag_";
    public static final String PICTURE_CONJUNCTION = "&";

    public static final String DEFAULT_NICKNAME = "默认昵称_";

    public static class CommandType {
        /**
         * 登录
         */
        public static final int CONNECT = 1;
        /**
         * 业务消息
         */
        public static final int CHAT = 2;

        /**
         * ping
         */
        public static final int PING = 3;
    }
}
