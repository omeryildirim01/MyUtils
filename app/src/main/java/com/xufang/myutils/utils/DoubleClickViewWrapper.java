package com.xufang.myutils.utils;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by xufang on 2017/8/23.
 */

public class DoubleClickViewWrapper {
    private View mView;
    private DoubleClickListener mDoubleClickListener;
    private DoubleClickListenerEx mDoubleClickListenerEx;
    private GestureDetectorCompat mGestureDetectorCompat;

    public DoubleClickViewWrapper(View view) {
        if (view == null) {
            return;
        }

        mView = view;
        mGestureDetectorCompat = getGestureDetectorCompat();
        mView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mGestureDetectorCompat != null) {
                    mGestureDetectorCompat.onTouchEvent(event);
                }
                return true;
            }
        });
    }

    private GestureDetectorCompat getGestureDetectorCompat() {
        Context context = mView.getContext();
        if (context == null) {
            return null;
        }

        return new GestureDetectorCompat(mView.getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (mDoubleClickListener != null) {
                    mDoubleClickListener.onDoubleClick(mView);
                }
                if (mDoubleClickListenerEx != null) {
                    mDoubleClickListenerEx.onDoubleClick(mView, e);
                }
                return mDoubleClickListener != null || mDoubleClickListenerEx != null;
            }
        });
    }

    public void setDoubleClickListener(DoubleClickListener doubleClickListener) {
        mDoubleClickListener = doubleClickListener;
    }

    public void setDoubleClickListenerEx(DoubleClickListenerEx doubleClickListenerEx) {
        mDoubleClickListenerEx = doubleClickListenerEx;
    }

    public interface DoubleClickListener {
        void onDoubleClick(View view);
    }

    public interface DoubleClickListenerEx {
        void onDoubleClick(View view, MotionEvent event);
    }
}
