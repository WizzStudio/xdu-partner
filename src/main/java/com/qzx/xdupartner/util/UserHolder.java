package com.qzx.xdupartner.util;

import java.util.concurrent.atomic.AtomicLong;

import com.qzx.xdupartner.entity.User;


public class UserHolder {
    private static AtomicLong anyId = new AtomicLong(Long.MIN_VALUE);
    private static final ThreadLocal<User> tl = new ThreadLocal<>();

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
            return anyId.incrementAndGet();
        }
        return user.getId();
    }
}
