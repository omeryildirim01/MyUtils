package com.xufang.myutils.utils;

import android.app.Activity;
import android.os.Build;
import android.view.View;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by xufang on 2017/9/7.
 */

public class SmartBarUtils {
    private static final String TAG = "SmartBarUtils";
    private static Boolean mHasSmartBar = null;

    public static void tryToHideSmartBar(Activity activity) {
        if (!hasSmartBar()) {
            return;
        }

        try {
            @SuppressWarnings("rawtypes")
            Class[] arrayOfClass = new Class[1];
            arrayOfClass[0] = Integer.TYPE;
            Method localMethod = View.class.getMethod("setSystemUiVisibility",
                    arrayOfClass);
            Field localField = View.class
                    .getField("SYSTEM_UI_FLAG_HIDE_NAVIGATION");
            Object[] arrayOfObject = new Object[1];
            try {
                arrayOfObject[0] = localField.get(null);
            } catch (Exception e) {

            }
            localMethod.invoke(activity.getWindow().getDecorView(), arrayOfObject);
            return;
        } catch (Exception e) {

        }
    }

    private static boolean hasSmartBar() {
        if (mHasSmartBar != null) {
            return mHasSmartBar;
        }

        mHasSmartBar = false;

        try {
            // 新型号可用反射调用Build.hasSmartBar()
            Method method = Class.forName("android.os.Build").getMethod("hasSmartBar");
            mHasSmartBar = ((Boolean) method.invoke(null)).booleanValue();
        } catch (Exception e) {

        }

        // 反射不到Build.hasSmartBar()，则用Build.DEVICE判断
        if (Build.DEVICE.equals("mx2")) {
            mHasSmartBar = true;
        } else if (Build.DEVICE.equals("mx") || Build.DEVICE.equals("m9")) {
            mHasSmartBar = false;
        }

        return mHasSmartBar;
    }
}
