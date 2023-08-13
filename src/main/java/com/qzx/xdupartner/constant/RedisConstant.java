package com.qzx.xdupartner.constant;

import org.springframework.data.redis.core.script.DefaultRedisScript;

public class RedisConstant {
    public static final String USESR_BLOG_LIKED_KEY = "blog:liked:";
    public static final String BLOG_READ_KEY = "blog:read:";
    public static final String USER_NEW_BLOG_SET_KEY = "user:blog:set:new:";
    public static final String USER_HOT_BLOG_SET_KEY = "user:blog:set:hot:";
    public static final String USER_SEARCH_BLOG_SET_KEY = "user:blog:set:search:";
    public static final String NEED_CAPTCHA_USER = "user:needcaptcha:";
    public static final String IM_SERVER_ONLINE_COUNT = "message:online:count:";
    public static final int USER_BLOG_SET_TIME = 10 * 60;

    public static final String LOGIN_PREFIX = "user:login:";
    public static final Integer LOGIN_VALID_TTL = 24 * 3;
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
    public static final int MESSAGE_MAX_SIZE = 100;
    public static final String LOW_TAG_FREQUENCY = "low:tag:frequency:";
    public static final String USERVO_CACHE = "uservo:cache:";
    public static final int CACHE_TTL = 5;

    public static DefaultRedisScript<Object> insertMessage = new DefaultRedisScript<>();

    static {
        insertMessage.setScriptText(
                "local key = KEYS[1]  -- 传入的键参数\n" +
                        "local score = tonumber(ARGV[1])  -- 传入的分值参数\n" +
                        "local member = ARGV[2]  -- 传入的成员参数\n" +
                        "local maxSize = tonumber(ARGV[3])  -- 传入的最大长度参数\n" +
                        "\n" +
                        "-- 判断 zset 的长度是否超过阈值\n" +
                        "local currentSize = redis.call('ZCARD', key)\n" +
                        "if currentSize >= maxSize then\n" +
                        "    -- 获取最旧的记录\n" +
                        "    local oldestMember = redis.call('ZRANGE', key, 0, 0)[1]\n" +
                        "    -- 删除最旧的记录\n" +
                        "    redis.call('ZREM', key, oldestMember)\n" +
                        "end\n" +
                        "\n" +
                        "-- 插入新记录\n" +
                        "redis.call('ZADD', key, score, member)\n" +
                        "\n" +
                        "return \"OK\"  -- 返回一个标识表示操作成功"
        );
    }
}
