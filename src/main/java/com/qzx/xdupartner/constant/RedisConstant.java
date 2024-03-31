package com.qzx.xdupartner.constant;

public class RedisConstant {
    public static final String USER_BLOG_LIKED_KEY = "blog:liked:";
    public static final String BLOG_READ_KEY = "blog:read:";
    public static final String USER_NEW_BLOG_SET_KEY = "user:blog:set:new:";
    public static final String USER_HOT_BLOG_SET_KEY = "user:blog:set:hot:";
    public static final String USER_SEARCH_BLOG_SET_KEY = "user:blog:set:search:";
    public static final String NEED_CAPTCHA_USER = "user:needcaptcha:";
    public static final String IM_SERVER_ONLINE_COUNT = "message:online:count:";
    public static final int USER_BLOG_SET_TIME = 2 * 60;

    public static final String LOGIN_PREFIX = "user:login:";
    public static final String MAIL_CODE_PREFIX = "user:mail:code";
    public static final Integer LOGIN_VALID_TTL = 24 * 14;
    public static final String DEFAULT_NICKNAME_INCREMENT = "default:nickname:";
    public static final String OFFLINE_MESSAGE = ":offline:message:from:";
    public static final String HISTORY_MESSAGE = ":history:message:from:";
    public static final String FRIEND_KEY = ":is:friend:";

    public static final int FRIEND_KEY_TTL = 30;
    public static final String USER_LIKE_BLOG_SET_KEY = "user:blog:set:like:";
    public static final String USER_ONES_BLOG_SET_KEY = "user:blog:set:ones:";
    public static final String SEND_MESSAGE = ":send:message:";
    public static final long IS_FRIEND_TTL = 5;
    public static final String IS_FRIEND = ":is:friend:";
    public static final String BLOG_CACHE = "blog:cache:";

    public static final String LOW_TAG_FREQUENCY = "low:tag:frequency:";
    public static final String USERVO_CACHE = "uservo:cache:";
    public static final int CACHE_TTL = 5;

    public static final String BOARD = "message:board";
    public static final String DICT_KEY = "blog:dict:";

}
