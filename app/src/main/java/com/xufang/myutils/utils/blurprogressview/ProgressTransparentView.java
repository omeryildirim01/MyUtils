package com.xufang.myutils.utils.blurprogressview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by xufang on 2017/11/24.
 */

public abstract class ProgressTransparentView extends ImageView {

    public ProgressTransparentView(Context context) {
        super(context);
    }

    public ProgressTransparentView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ProgressTransparentView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    abstract void setProgress(long progress, long max);

    boolean progressValid(long progress, long max) {
        return progress >= 0 && max > 0;
    }
}
