package com.xufang.myutils;

import android.app.Application;
import android.content.Context;

/**
 * Created by xufang on 2017/12/7.
 */

public class MyApp extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getAppContext() {
        return context;
    }
}
