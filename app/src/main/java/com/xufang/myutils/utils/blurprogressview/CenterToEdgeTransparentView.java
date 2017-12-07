package com.xufang.myutils.utils.blurprogressview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.xufang.myutils.R;

/**
 * Created by xufang on 2017/11/21.
 * 这个又被产品改需求了，改成从上到下变模糊并且不带黄框，这个类先保留，也许万一又改回来，
 * 模糊效果请移步SideToSideTransparentView
 */

public class CenterToEdgeTransparentView extends ProgressTransparentView {
    private static final String TAG = "CenterToEdgeTransparentView";

    private static final int STROKE_COLOR = 0xff928425;
    private static final int STROKE_WIDTH = 8;

    private Rect mTransparentRect;
    private Paint mSolidPaint;
    private Paint mStrokePaint;
    private PorterDuffXfermode mPorterDuffXfermode;

    private int mStrokeColor;
    private int mStrokeWidth;

    private boolean mNeedDraw;
    private int mWidth, mHeight;
    private Point mCenter;

    public CenterToEdgeTransparentView(Context context) {
        super(context);
        init(context, null);
    }

    public CenterToEdgeTransparentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CenterToEdgeTransparentView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        mNeedDraw = false;

        mCenter = new Point();
        mTransparentRect = new Rect();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CenterToEdgeTransparentView);
        mStrokeColor = a.getColor(R.styleable.CenterToEdgeTransparentView_bcpv_stroke_color, STROKE_COLOR);
        mStrokeWidth = a.getDimensionPixelOffset(R.styleable.CenterToEdgeTransparentView_bcpv_stroke_width, STROKE_WIDTH);
        a.recycle();

        initPaintTools();
    }

    private void initPaintTools() {
        mStrokePaint = new Paint();
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setColor(mStrokeColor);
        mStrokePaint.setStrokeWidth(mStrokeWidth);

        mSolidPaint = new Paint();
        mSolidPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void setProgress(long progress, long max) {
        if (!progressValid(progress, max)) {
            return;
        }
        double percent = (double) progress / (double) max;
        if (percent == 0 || mWidth <= 0 || mHeight <= 0) {
            mNeedDraw = false;
            return;
        }

        if (percent > 1) {
            percent = 1;
        }

        mNeedDraw = true;
        int xOffset = (int) (percent * mWidth / 2);
        int yOffset = (int) (percent * mHeight / 2);

        int strokeWidthOffset = mStrokeWidth / 2;

        int mTransparentLeft = mCenter.x - xOffset + strokeWidthOffset;
        int mTransparentTop = mCenter.y - yOffset + strokeWidthOffset;
        int mTransparentRight = mCenter.x + xOffset - strokeWidthOffset;
        int mTransparentBottom = mCenter.y + yOffset - strokeWidthOffset;

        mTransparentRect.set(mTransparentLeft, mTransparentTop, mTransparentRight, mTransparentBottom);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mNeedDraw) {
            drawStroke(canvas);
            drawTransparent(canvas);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            mWidth = w;
            mHeight = h;
            mCenter.set(getLeft() + mWidth / 2, getTop() + mHeight / 2);
        }
    }

    private void drawStroke(Canvas canvas) {
        if (canvas == null) {
            return;
        }

        canvas.drawRect(mTransparentRect, mStrokePaint);
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
