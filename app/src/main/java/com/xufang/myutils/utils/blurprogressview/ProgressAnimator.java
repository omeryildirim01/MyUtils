package com.xufang.myutils.utils.blurprogressview;

import android.animation.Animator;
import android.animation.ValueAnimator;

/**
 * Created by xufang on 2017/11/28.
 */

public class ProgressAnimator implements ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {
    private static final String TAG = "ProgressAnimator";

    private UpdateCallback mCallback;
    private ValueAnimator mAnimator;
    private float mLastPercent;
    private boolean mStarted;

    public ProgressAnimator() {
        mAnimator = ValueAnimator.ofFloat(0, 0);
        mAnimator.addUpdateListener(this);
        mAnimator.addListener(this);
    }

    public void setCallback(UpdateCallback callback) {
        mCallback = callback;
    }

    public void startAnimator(float percent) {
        if (mStarted) {
            return;
        }

        if (mLastPercent != percent) {
            mAnimator.setFloatValues(mLastPercent, percent);
            mAnimator.start();
        }
    }

    public void cancelAnimator() {
        mLastPercent = 0;
        mStarted = false;
        mAnimator.cancel();
    }

    @Override
    public void onAnimationStart(Animator animation) {
        mStarted = true;
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        mStarted = false;
    }

    @Override
    public void onAnimationCancel(Animator animation) {
        mStarted = false;
    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        mLastPercent = (float) animation.getAnimatedValue();
        if (mCallback != null) {
            mCallback.update(mLastPercent);
        }
    }

    public interface UpdateCallback {
        void update(float percent);
    }
}
