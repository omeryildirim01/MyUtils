package com.xufang.myutils.utils.like;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import com.xufang.myutils.utils.DimensUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by xufang on 2017/9/27.
 */

public class LikeItem {
    private static final String TAG = "LikeItem";
    private AnimatorSet mAlphaAnimSet;
    private AnimatorSet mTranslateYAnimSet;
    private AnimatorSet mScaleAnimSet;
    private List<AnimatorSet> mAnimatorSets;

    private Context mContext;
    private Paint mPaint;
    private Matrix mMatrix;
    private Bitmap mLikeBitmap;
    private Point mTouchPoint;

    private volatile boolean mIsAnimating;
    private float mScale, mTranslationY;
    private float mAlpha; //0-255
    private float mRotateAngle;

    public LikeItem(Context context, Bitmap likeBitmap) {
        mContext = context;
        mMatrix = new Matrix();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mTouchPoint = new Point();
        mLikeBitmap = likeBitmap;
        initAnimatorSets();
    }

    public void reset() {
        mScale = 0;
        mAlpha = 0;
        mTranslationY = 0;
        mIsAnimating = false;
    }

    public void setTouchPoint(Point touchPoint) {
        if (touchPoint != null) {
            mTouchPoint.set(touchPoint.x, touchPoint.y);
        }
    }

    public void setTouchPoint(int x, int y) {
        mTouchPoint.set(x, y);
    }

    public void start() {
        mIsAnimating = true;
        mRotateAngle = new Random(System.currentTimeMillis()).nextInt(61) - 30;
        startAnims();
    }

    public boolean isAnimating() {
        return mIsAnimating;
    }

    public void updateFrame(Canvas canvas) {
        if (!mIsAnimating) {
            Log.d(TAG, "not running, skip draw");
            return;
        }

        mPaint.setAlpha((int) mAlpha);
        mMatrix.reset();
        mMatrix.postScale(mScale, mScale, mLikeBitmap.getWidth() / 2, mLikeBitmap.getHeight() / 2);
        mMatrix.postRotate(mRotateAngle, mLikeBitmap.getWidth() / 2, mLikeBitmap.getHeight() / 2);
        mMatrix.postTranslate(mTouchPoint.x, mTouchPoint.y - mTranslationY);
        canvas.drawBitmap(mLikeBitmap, mMatrix, mPaint);
    }

    public void stop() {
        if (mIsAnimating) {
            mIsAnimating = false;
            mAlphaAnimSet.cancel();
            mTranslateYAnimSet.cancel();
            mScaleAnimSet.cancel();
        }
    }

    private void initAnimatorSets() {
        mAlphaAnimSet = new AnimatorSet();
        mScaleAnimSet = new AnimatorSet();
        mTranslateYAnimSet = new AnimatorSet();
        mAnimatorSets = new ArrayList<>(3);
        mAnimatorSets.add(mAlphaAnimSet);
        mAnimatorSets.add(mScaleAnimSet);
        mAnimatorSets.add(mTranslateYAnimSet);
        initAnimListener();
        initAnims();
    }

    private void initAnims() {
        initScaleAnim();
        initAlphaAnim();
        initTranslateAnim();
    }

    private void initTranslateAnim() {
        mTranslateYAnimSet.playSequentially(getAnimator(0f, 0f, 800, AnimType.TRANSLATE),
                getAnimator(0f, DimensUtils.dip2pixel(mContext, 50), 200, AnimType.TRANSLATE));
    }

    private void initAlphaAnim() {
        mAlphaAnimSet.playSequentially(getAnimator(0f, 255f, 150, AnimType.ALPHA),
                getAnimator(255f, 255f, 150, AnimType.ALPHA),
                getAnimator(255f, 255f, 150, AnimType.ALPHA),
                getAnimator(255f, 255f, 350, AnimType.ALPHA),
                getAnimator(255f, 0f, 200, AnimType.ALPHA));
    }

    private void initScaleAnim() {
        mScaleAnimSet.playSequentially(getAnimator(1f, 0.5f, 150, AnimType.SCALE),
                getAnimator(0.5f, 0.6f, 150, AnimType.SCALE),
                getAnimator(0.6f, 0.55f, 150, AnimType.SCALE),
                getAnimator(0.55f, 0.55f, 350, AnimType.SCALE),
                getAnimator(0.55f, 1f, 200, AnimType.SCALE));
    }

    private ValueAnimator getAnimator(float start, float end, long duration, final AnimType animType) {
        final ValueAnimator animator = ValueAnimator.ofFloat(start, end).setDuration(duration);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                Log.d(TAG, "update:" + animType + "  value:" + valueAnimator.getAnimatedValue());
                switch (animType) {
                    case SCALE:
                        mScale = (float) valueAnimator.getAnimatedValue();
                        break;
                    case ALPHA:
                        mAlpha = (float) valueAnimator.getAnimatedValue();
                        break;
                    case TRANSLATE:
                        mTranslationY = (float) valueAnimator.getAnimatedValue();
                        break;
                    default:
                        break;
                }
            }
        });
        return animator;
    }

    private void initAnimListener() {
        Animator.AnimatorListener listener = new SimpleAnimatorListener() {
            int endTimes = 0;

            @Override
            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                endTimes++;
                if (endTimes == mAnimatorSets.size() || mAnimatorSets.isEmpty()) {
                    endTimes = 0;
                    mIsAnimating = false;
                }
            }
        };
        for (AnimatorSet set : mAnimatorSets) {
            set.addListener(listener);
        }
    }

    private void startAnims() {
        mAlphaAnimSet.start();
        mScaleAnimSet.start();
        mTranslateYAnimSet.start();
    }

    private enum AnimType {
        SCALE, ALPHA, TRANSLATE
    }

}
