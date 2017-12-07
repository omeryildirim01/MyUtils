package com.xufang.myutils.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.duowan.mobile.entlive.R;
import com.yy.mobile.memoryrecycle.views.YYImageView;
import com.yy.mobile.memoryrecycle.views.YYLinearLayout;
import com.yy.mobile.util.CollectionsHelper;
import com.yy.mobile.util.log.MLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xufang on 2017/6/1.
 */

public class LoadingView extends YYLinearLayout {
    private static final String TAG = "LoadingView";

    private static final int DEFAULT_REFRESH_SPEED = 300;

    private int mRefreshIntervalMillis;
    private List<YYImageView> mImageChildList;
    private List<Float> mAlphaPercentList;
    private int mImageChildCount;
    private int mLoopTimes;

    private Runnable mRefreshTask = new Runnable() {
        @Override
        public void run() {
            updateChildViewsDrawable();

            removeCallbacks(this);
            postDelayed(this, mRefreshIntervalMillis);
        }
    };

    private void updateChildViewsDrawable() {
        mLoopTimes++;
        if (mLoopTimes == mImageChildCount) {
            mLoopTimes = 0;
        }
        int childIndex = 0;
        for (int i = mLoopTimes; i < mImageChildCount; i++) {
            YYImageView child = mImageChildList.get(childIndex);
            setChildDrawable(child, mAlphaPercentList.get(i));
            childIndex++;
        }
        for (int i = 0; i < mLoopTimes; i++) {
            YYImageView child = mImageChildList.get(childIndex);
            setChildDrawable(child, mAlphaPercentList.get(i));
            childIndex++;
        }
    }

    private void setChildDrawable(YYImageView child, float alphaPercent) {
        int alpha = (int) (alphaPercent * 255);
        Drawable drawable = child.getDrawable();
        if (drawable != null) {
            drawable.setAlpha(alpha);
        }
    }

    public LoadingView(Context context) {
        this(context, null);
    }

    public LoadingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.LoadingView);
        mRefreshIntervalMillis = ta.getInt(R.styleable.LoadingView_refresh_speed, DEFAULT_REFRESH_SPEED);
        ta.recycle();

        mImageChildCount = 0;
    }

    /**
     * @param alphaPercentList 透明度列表，透明度是浮点型，取值0-1，比如说0.5就标识50%的透明度。
     */
    public void setAlphaPercentList(List<Float> alphaPercentList) {
        mAlphaPercentList = new ArrayList<>(alphaPercentList);
        MLog.info(TAG, "setAlphaPercentList mAlphaPercentList.size():%d, mImageChildCount:%d",
                mAlphaPercentList.size(), mImageChildCount);
        if (mAlphaPercentList.size() != mImageChildCount) {
            return;
        }

        post(new Runnable() {
            @Override
            public void run() {
                if (mAlphaPercentList != null) {
                    for (int i = 0; i < mImageChildCount; i++) {
                        YYImageView imageView = mImageChildList.get(i);
                        setChildDrawable(imageView, mAlphaPercentList.get(i));
                    }
                }
            }
        });
    }

    public void startLoading() {
        if (CollectionsHelper.isNullOrEmpty(mAlphaPercentList)
                || CollectionsHelper.isNullOrEmpty(mImageChildList)
                || mAlphaPercentList.size() != mImageChildCount) {
            return;
        }

        MLog.info(TAG, "startLoading");
        postDelayed(mRefreshTask, mRefreshIntervalMillis);
    }

    public void stopLoading() {
        MLog.info(TAG, "stopLoading");
        removeCallbacks(mRefreshTask);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initChildList();
    }

    private void initChildList() {
        int childCount = getChildCount();
        MLog.info(TAG, "initChildList， childCount:%d", childCount);
        mImageChildList = new ArrayList<>(childCount);
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof YYImageView) {
                mImageChildList.add((YYImageView) child);
            }
        }
        mImageChildCount = mImageChildList.size();
    }

    @Override
    protected void onDetachedFromWindow() {
        stopLoading();
        super.onDetachedFromWindow();
    }
}
