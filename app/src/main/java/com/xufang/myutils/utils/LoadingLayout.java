package com.xufang.myutils.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.xufang.myutils.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xufang on 2017/8/31.
 */

public class LoadingLayout extends FrameLayout {
    private static final String TAG = "LoadingLayout";

    private static final int DEFAULT_PROGRESS_LAYOUT = R.layout.layout_progress_bar;

    private ProgressBar mProgressBar;
    private List<View> mContentViews;

    public LoadingLayout(Context context) {
        this(context, null);
    }

    public LoadingLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.LoadingLayout);
        int progressLayoutId = ta.getResourceId(R.styleable.LoadingLayout_ll_progress_layout_id, DEFAULT_PROGRESS_LAYOUT);
        ta.recycle();

        mContentViews = new ArrayList<>(1);

        mProgressBar = (ProgressBar) LayoutInflater.from(context).inflate(progressLayoutId, null);
        mProgressBar.setVisibility(GONE);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER;
        addView(mProgressBar, lp);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view instanceof ProgressBar || view.equals(mProgressBar)) {
                continue;
            }
            mContentViews.add(view);
            return;
        }
    }

    public void showLoading() {
        for (View view : mContentViews) {
            view.setVisibility(GONE);
        }
        mProgressBar.setVisibility(VISIBLE);
    }

    public void showContent() {
        for (View view : mContentViews) {
            view.setVisibility(VISIBLE);
        }
        mProgressBar.setVisibility(GONE);
    }
}
