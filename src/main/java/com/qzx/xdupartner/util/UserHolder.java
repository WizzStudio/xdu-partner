package com.qzx.xdupartner.util;

import cn.hutool.core.util.RandomUtil;
import com.qzx.xdupartner.entity.User;

import java.util.concurrent.atomic.AtomicLong;


public class UserHolder {
    private static final ThreadLocal<User> tl = new ThreadLocal<>();
    private static final AtomicLong anyId = new AtomicLong(Long.MIN_VALUE);

    public static void saveUser(User user) {
        tl.set(user);
    }

    public static User getUser() {
        return tl.get();
    }

    public static void removeUser() {
        tl.remove();
    }

    public static Long getUserId() {
        User user = getUser();
        if (user == null) {
            return anyId.addAndGet(RandomUtil.randomInt(1));
        }
        return user.getId();
    }

    public static String getUserSessionKey() {
        User user = getUser();
        return user.getSessionKey();
    }
}
