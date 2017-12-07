package com.xufang.myutils.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by xufang on 2017/6/16.
 */

public class PartTransparentView extends RelativeLayout {
    private Context mContext;

    private Rect mTransparentRect;
    private Paint mSolidPaint;
    private Paint mStrokePaint;
    private PorterDuffXfermode mPorterDuffXfermode;
    private int mSolidColor = 0x00ffffff;
    private int mStrokeColor = 0xff928425;
    private boolean mShowStroke = true;

    public PartTransparentView(Context context) {
        super(context);
        init(context);
    }

    public PartTransparentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PartTransparentView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mTransparentRect = new Rect(0, 0, 0, 0);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    public void setTransparentRect(final Rect rect) {
        this.mTransparentRect = rect;
    }

    public void setColor(final int color) {
        mSolidColor = color;
    }

    public void showStroke(boolean showStroke) {
        mShowStroke = showStroke;
    }

    public void setStrokeColor(final int color) {
        mStrokeColor = color;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        setLayerType(LAYER_TYPE_HARDWARE, null);

        drawStroke(canvas);

        if (mSolidPaint == null) {
            mSolidPaint = new Paint();
        }
        mSolidPaint.setFilterBitmap(false);
        mSolidPaint.setStyle(Paint.Style.FILL);

        if (mPorterDuffXfermode == null) {
            mPorterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
        }

        mSolidPaint.setXfermode(mPorterDuffXfermode);
        canvas.drawRect(mTransparentRect, mSolidPaint);
        mSolidPaint.setXfermode(null);

        setLayerType(LAYER_TYPE_NONE, null);
    }

    private void drawStroke(Canvas canvas) {
        if (!mShowStroke) {
            return;
        }

        if (mStrokePaint == null) {
            mStrokePaint = new Paint();
        }
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setColor(mStrokeColor);
        mStrokePaint.setStrokeWidth(DimensUtils.dip2pixel(mContext, 1.5f));
        canvas.drawRect(mTransparentRect, mStrokePaint);
    }
}
