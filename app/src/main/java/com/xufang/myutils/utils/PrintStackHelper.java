package com.xufang.myutils.utils;

import android.util.Log;

/**
 * Created by xufang on 2017/8/24.
 * use for debug
 */

public class PrintStackHelper {

    public static void printStack() {
        printStack("PrintStackHelper");
    }

    public static void printStack(String tag) {
        StringBuilder sb = new StringBuilder();

        Thread current = Thread.currentThread();
        StackTraceElement[] elements = current.getStackTrace();

        for (StackTraceElement element : elements) {
            sb.append(element.toString());
            sb.append("\r\n");
        }

        Log.d(tag, sb.toString());
    }
}
