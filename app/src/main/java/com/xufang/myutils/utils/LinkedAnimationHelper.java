package com.xufang.myutils.utils;

import android.view.View;
import android.view.animation.Animation;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by xufang on 2017/8/25.
 */

public class LinkedAnimationHelper {
    private static final String TAG = "LinkedAnimationHelper";

    private EndListener mEndListener;
    private Queue<Animation> mAnimationQueue;

    private boolean mIsAnimationOver;

    public LinkedAnimationHelper() {
        mIsAnimationOver = true;
        mAnimationQueue = new LinkedList<>();
    }

    public void reset() {
        mIsAnimationOver = true;
        mEndListener = null;
        mAnimationQueue.clear();
    }

    public boolean isAnimationOver() {
        return mIsAnimationOver;
    }

    public void cancelAnimation() {
        if (isAnimationOver()) {
            return;
        }

        for (Animation animation : mAnimationQueue) {
            animation.cancel();
        }
        reset();
    }

    //step1
    public LinkedAnimationHelper add(Animation animation) {
        try {
            mAnimationQueue.add(animation);
        } catch (Exception e) {

        }
        return this;
    }

    //step2--optional
    public LinkedAnimationHelper setEndListener(EndListener endListener) {
        mEndListener = endListener;
        return this;
    }

    //step3
    public void startAnimation(final View animationView) {
        mIsAnimationOver = false;

        if (animationView == null) {
            mIsAnimationOver = true;
            return;
        }

        Animation animation = mAnimationQueue.poll();

        if (animation == null) {
            mIsAnimationOver = true;
            if (mEndListener != null) {
                mEndListener.onAnimationOver(animationView);
            }
            return;
        }

        animation.setAnimationListener(new EndAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                if (mEndListener != null) {
                    mEndListener.onAnimationEnd(animationView, animation);
                }
                startAnimation(animationView);
            }
        });
        animationView.startAnimation(animation);
    }

    public interface EndListener {
        void onAnimationEnd(View animationView, Animation animation);

        void onAnimationOver(View animationView);
    }

    private abstract class EndAnimationListener implements Animation.AnimationListener {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }
}
