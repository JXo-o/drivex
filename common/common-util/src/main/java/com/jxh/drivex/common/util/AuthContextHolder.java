package com.jxh.drivex.common.util;

public class AuthContextHolder {

    private static final ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        threadLocal.set(userId);
    }

    public static Long getUserId() {
        return threadLocal.get();
    }

    public static void removeUserId() {
        threadLocal.remove();
    }

}
