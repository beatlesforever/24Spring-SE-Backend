package com.example.sebackend.context;

public class BaseContext {


    //ThreadLocal并不是一个Thread,而是Thread的局部变量。
    // ThreadLocal为每个线程提供单独一份存储空间，具有线程隔离的效果，
    // 只有在线程内才能获取到对应的值，线程外则不能访问。

    public static ThreadLocal<String> threadLocal = new ThreadLocal<>();

    public static void setCurrentUser(String username) {
        threadLocal.set(username);
    }

    public static String getCurrentUser() {
        return threadLocal.get();
    }

    public static void removeCurrentId() {
        threadLocal.remove();
    }

}
