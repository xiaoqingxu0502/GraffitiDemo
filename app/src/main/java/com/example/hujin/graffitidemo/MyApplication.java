package com.example.hujin.graffitidemo;

import android.app.Application;

/**
 * @Package com.example.hujin.graffitidemo
 * @Description: MyApplication
 * @Author hujin
 * @Date 2017/6/2 上午11:03
 * @Email xiaoqingxu0502@gamil.com
 * @Blog:richtime.vip
 */
public class MyApplication extends Application {
    public static Application appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
    }
}
