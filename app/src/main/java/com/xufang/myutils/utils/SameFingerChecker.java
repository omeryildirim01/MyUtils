package com.xufang.myutils.utils;

import android.view.MotionEvent;

/**
 * Created by xufang on 2017/10/19.
 */

public class SameFingerChecker {
    private static int mDownPointerId;
    private static boolean mHasSetDownPointerId;

    public static void onDownEvent(MotionEvent event) {
        if (event == null || event.getAction() != MotionEvent.ACTION_DOWN) {
            mHasSetDownPointerId = false;
            return;
        }
        mHasSetDownPointerId = true;
        mDownPointerId = event.getPointerId(0);
    }

    public static boolean checkIsTheSameFinger(MotionEvent event) {
        if (!mHasSetDownPointerId || event == null) {
            return false;
        }
        int pointerId = event.getPointerId(0);
        return pointerId == mDownPointerId;
    }
}
