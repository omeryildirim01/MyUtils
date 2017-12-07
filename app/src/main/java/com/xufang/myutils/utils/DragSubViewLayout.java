package com.xufang.myutils.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.duowan.mobile.entlive.R;
import com.yy.mobile.memoryrecycle.views.YYFrameLayout;
import com.yy.mobile.util.log.MLog;

/**
 * Created by xufang on 2017/6/2.
 */

public class DragSubViewLayout extends YYFrameLayout {
    private static final String TAG = "DragSubViewLayout";

    private static final int DRAW_NOTING = 0;
    private static final int DRAW_BEFORE_DRAG = 1;
    private static final int DRAW_AFTER_DRAG = 2;

    private ViewDragHelper mDragHelper;
    private DragViewCallback mCallback;

    private View mChild;

    private boolean mIsVertical;
    private boolean mIsTouchInChild;
    private Paint mCoverPaint;
    private ChildPositionInfo mChildPositionInfo;

    private int mDrawState = DRAW_NOTING;

    public DragSubViewLayout(@NonNull Context context) {
        this(context, null);
    }

    public DragSubViewLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragSubViewLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() > 0) {
            mChild = getChildAt(0);
            mChild.setBackgroundResource(R.drawable.shape_draggable_small_window_view);
            mChild.setVisibility(INVISIBLE);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        MLog.debug(TAG, "onDraw mDrawState:%d", mDrawState);
        if (mDrawState == DRAW_NOTING) {
            return;
        }

        if (mCoverPaint == null) {
            mCoverPaint = new Paint();
            mCoverPaint.setColor(0x00FFFFFF);
            mCoverPaint.setStyle(Paint.Style.FILL);
        }

        if (mDrawState == DRAW_BEFORE_DRAG) {
            mCoverPaint.setColor(0x59FFFFFF);
        } else if (mDrawState == DRAW_AFTER_DRAG) {
            mCoverPaint.setColor(0x00FFFFFF);
        }

        if (mChildPositionInfo != null) {
            canvas.drawRect(mChildPositionInfo.left, mChildPositionInfo.top, mChildPositionInfo.right, mChildPositionInfo.bottom, mCoverPaint);
        }

        mDrawState = DRAW_NOTING;
    }

    private void init() {
        ViewDragCallback dragCallback = new ViewDragCallback();
        mDragHelper = ViewDragHelper.create(this, 1.0f, dragCallback);
        setWillNotDraw(false);
    }

    private void invalidateThis() {
        if (mChildPositionInfo != null) {
            invalidate(mChildPositionInfo.left, mChildPositionInfo.top, mChildPositionInfo.right, mChildPositionInfo.bottom);
        }
    }

    //step1
    public void setDragViewCallback(DragViewCallback callback) {
        mCallback = callback;
    }

    //step2
    public void updateUI(final boolean isVertical, final int childLeft, final int childTop, final int childRight, final int childBottom) {
        post(new Runnable() {
            @Override
            public void run() {
                setOrientation(isVertical);
                setChildPosition(childLeft, childTop, childRight, childBottom);
            }
        });
    }

    private void setOrientation(boolean isVertical) {
        mIsVertical = isVertical;
    }

    private void setChildPosition(int childLeft, int childTop, int childRight, int childBottom) {
        if (mChild == null) {
            return;
        }

        if (childLeft < getPaddingLeft() || childRight > getWidth() - getPaddingRight()
                || childTop < getPaddingTop() || childBottom > getHeight() - getPaddingBottom()) {
            return;
        }

        updateChildPositionInfo(childLeft, childTop, childRight, childBottom);

        updateChildLayoutParams();
    }

    private void updateChildLayoutParams() {
        if (mChildPositionInfo == null) {
            return;
        }

        int width = mChildPositionInfo.right - mChildPositionInfo.left;
        int height = mChildPositionInfo.bottom - mChildPositionInfo.top;
        if (width <= 0 || height <= 0) {
            return;
        }

        FrameLayout.LayoutParams params = (LayoutParams) mChild.getLayoutParams();
        params.width = width;
        params.height = height;
        params.leftMargin = mChildPositionInfo.left - getPaddingLeft();
        params.topMargin = mChildPositionInfo.top -getPaddingTop();
        mChild.setLayoutParams(params);
    }

    private void updateChildPositionInfo(int left, int top, int right, int bottom) {
        if (mChildPositionInfo == null) {
            mChildPositionInfo = new ChildPositionInfo();
        }
        mChildPositionInfo.left = left;
        mChildPositionInfo.top = top;
        mChildPositionInfo.right = right;
        mChildPositionInfo.bottom = bottom;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN && isTouchInChild(ev.getX(), ev.getY())) {
            mIsTouchInChild = true;
        }

        if (mIsTouchInChild) {
            mDragHelper.processTouchEvent(ev);
            if (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL) {
                mIsTouchInChild = false;
            }
            return true;
        }

        return super.dispatchTouchEvent(ev);
    }

    private boolean isTouchInChild(float x, float y) {
        return mChild != null && x >= mChild.getLeft() && x <= mChild.getRight()
                && y >= mChild.getTop() && y <= mChild.getBottom();
    }

    private class ViewDragCallback extends ViewDragHelper.Callback {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return true;
        }

        @Override
        public void onViewDragStateChanged(int state) {
            updateChildLayoutParams();

            MLog.debug(TAG, "onViewDragStateChanged state:%d", state);
            if (state == ViewDragHelper.STATE_DRAGGING) {
                mChild.setVisibility(VISIBLE);
                mDrawState = DRAW_BEFORE_DRAG;
                invalidateThis();
            } else if (state == ViewDragHelper.STATE_IDLE) {
                mChild.setVisibility(INVISIBLE);
            }
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            int leftBound = getPaddingLeft();
            int rightBound = getWidth() - child.getWidth() - getPaddingRight();
            return Math.min(rightBound, Math.max(left, leftBound));
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            final int topBound = getPaddingTop();
            final int bottomBound = getHeight() - child.getHeight();
            return Math.min(Math.max(top, topBound), bottomBound);
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            int left = releasedChild.getLeft();
            int top = releasedChild.getTop();
            int right = releasedChild.getRight();
            int bottom = releasedChild.getBottom();
            MLog.debug(TAG, "onViewReleased, left:%d, top:%d, right:%d, bottom:%d", left, top, right, bottom);
            if (mCallback != null) {
                mCallback.onDragFinishedPosition(left, top, right, bottom);
            }
            updateChildPositionInfo(left, top, right, bottom);
            mDrawState = DRAW_AFTER_DRAG;
            invalidateThis();
        }
    }

    private class ChildPositionInfo {
        int left;
        int top;
        int right;
        int bottom;
    }

    public interface DragViewCallback {
        void onDragFinishedPosition(int left, int top, int right, int bottom);
    }
}
