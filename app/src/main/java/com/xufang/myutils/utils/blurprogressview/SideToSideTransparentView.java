package com.xufang.myutils.utils.blurprogressview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.xufang.myutils.R;

/**
 * Created by xufang on 2017/11/24.
 */

public class SideToSideTransparentView extends ProgressTransparentView implements ProgressAnimator.UpdateCallback {
    private static final String TAG = "SideToSideTransparentView";

    private static final int DRAW_FROM_LEFT = 1;
    private static final int DRAW_FROM_TOP = 2;
    private static final int DRAW_FROM_RIGHT = 3;
    private static final int DRAW_FROM_BOTTOM = 4;

    private ProgressAnimator mProgressAnimator;

    private Rect mTransparentRect;
    private Paint mSolidPaint;
    private PorterDuffXfermode mPorterDuffXfermode;

    private boolean mNeedDraw;
    private int mWidth, mHeight;
    private int mDrawFrom;

    public SideToSideTransparentView(Context context) {
        super(context);
        init(context, null);
    }

    public SideToSideTransparentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SideToSideTransparentView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        mNeedDraw = false;

        mTransparentRect = new Rect();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SideToSideTransparentView);
        mDrawFrom = a.getInt(R.styleable.SideToSideTransparentView_ststv_start_side, DRAW_FROM_TOP);
        a.recycle();

        mSolidPaint = new Paint();
        mSolidPaint.setStyle(Paint.Style.FILL);

        mProgressAnimator = new ProgressAnimator();
        mProgressAnimator.setCallback(this);
    }

    @Override
    public void update(float percent) {
        setRectByDirection(percent);
        invalidate();
    }

    @Override
    public void setProgress(long progress, long max) {
        if (!progressValid(progress, max)) {
            return;
        }
        float percent = (float) progress / (float) max;
        if (percent == 0 || mWidth <= 0 || mHeight <= 0) {
            mNeedDraw = false;
            return;
        }

        if (percent > 1) {
            percent = 1;
        }

        mNeedDraw = true;
        mProgressAnimator.startAnimator(percent);
    }

    private void setRectByDirection(float percent) {
        int mTransparentLeft = getLeft();
        int mTransparentTop = getTop();
        int mTransparentRight = getRight();
        int mTransparentBottom = getTop() + (int) (percent * mHeight);
        switch (mDrawFrom) {
            case DRAW_FROM_LEFT:
                mTransparentLeft = getLeft();
                mTransparentTop = getTop();
                mTransparentRight = getLeft() + (int) (percent * mWidth);
                mTransparentBottom = getBottom();
                break;
            case DRAW_FROM_TOP:
                mTransparentLeft = getLeft();
                mTransparentTop = getTop();
                mTransparentRight = getRight();
                mTransparentBottom = getTop() + (int) (percent * mHeight);
                break;
            case DRAW_FROM_RIGHT:
                mTransparentLeft = getRight() - (int) (percent * mWidth);
                mTransparentTop = getTop();
                mTransparentRight = getRight();
                mTransparentBottom = getBottom();
                break;
            case DRAW_FROM_BOTTOM:
                mTransparentLeft = getLeft();
                mTransparentTop = getBottom() - (int) (percent * mWidth);
                mTransparentRight = getRight();
                mTransparentBottom = getBottom();
                break;
            default:
                break;
        }

        mTransparentRect.set(mTransparentLeft, mTransparentTop, mTransparentRight, mTransparentBottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mNeedDraw) {
            drawTransparent(canvas);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            mWidth = w;
            mHeight = h;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        mProgressAnimator.cancelAnimator();
        super.onDetachedFromWindow();
    }

    private void drawTransparent(Canvas canvas) {
        if (canvas == null) {
            return;
        }

        if (mPorterDuffXfermode == null) {
            mPorterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
        }

        mSolidPaint.setXfermode(mPorterDuffXfermode);
        canvas.drawRect(mTransparentRect, mSolidPaint);
        mSolidPaint.setXfermode(null);
    }
}
