package com.xufang.myutils.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewStub;

import com.xufang.myutils.MyApp;

/**
 * Created by xufang on 2017/8/28.
 */

public class ShowGuideOnceHelper {
    private static final String TAG = "ShowGuideOnceHelper";

    public static void showGuide(String sharePrefKey, ViewStub guideStub, InflatedCallback callback) {
        if (sharePrefKey == null || sharePrefKey.isEmpty()) {
            if (callback != null) {
                callback.onGuideStubInflated(false, null);
            }
            return;
        }

        if (!shouldShowGuide(sharePrefKey)) {
            if (callback != null) {
                callback.onGuideNoNeedShow();
            }
            return;
        }

        if (guideStub == null) {
            if (callback != null) {
                callback.onGuideStubInflated(false, null);
            }
            return;
        }

        SharedPreferences sharpf = MyApp.getAppContext().getSharedPreferences("Sample", Context.MODE_PRIVATE);
        sharpf.edit().putBoolean(sharePrefKey, true).apply();
        View inflated = null;
        try {
            inflated = guideStub.inflate();
        } catch (Exception e) {

        }

        if (callback != null) {
            callback.onGuideStubInflated(inflated != null, inflated);
        }
    }

    private static boolean shouldShowGuide(String sharePrefKey) {
        SharedPreferences sharpf = MyApp.getAppContext().getSharedPreferences("Sample", Context.MODE_PRIVATE);
        boolean hasShown = sharpf.getBoolean(sharePrefKey, false);
        return !hasShown;
    }

    public interface InflatedCallback {
        void onGuideNoNeedShow();

        void onGuideStubInflated(boolean success, View inflatedView);
    }
}
