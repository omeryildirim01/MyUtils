package com.xufang.myutils.utils.like;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import static com.xufang.myutils.utils.like.LikeEventClickHelper.ClickMode.MODE_DOUBLE_CLICK;
import static com.xufang.myutils.utils.like.LikeEventClickHelper.ClickMode.MODE_SINGLE_CLICK;

/**
 * Created by xufang on 2017/10/13.
 * 用于双击启动点赞动画，启动后响应单击事件播放点赞动画，改善用户体验。然而产品不同意这样做，该类暂时无人引用。
 */

public class LikeEventClickHelper {
    private static final String TAG = "LikeEventClickHelper";

    private ClickMode mClickMode;
    private Context mAppContext;
    private LikeClickListener mLikeClickListener;
    private GestureDetectorCompat mGestureDetectorCompat;

    public enum ClickMode {
        MODE_SINGLE_CLICK,
        MODE_DOUBLE_CLICK
    }

    public LikeEventClickHelper(View clickView) {
        if (clickView != null && clickView.getContext() != null) {
            mAppContext = clickView.getContext().getApplicationContext();
            mGestureDetectorCompat = getGestureDetectorCompat();
            clickView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        if (MODE_SINGLE_CLICK.equals(mClickMode)) {
                            if (mLikeClickListener != null) {
                                mLikeClickListener.onTriggerLike(MODE_SINGLE_CLICK, event);
                            }
                        }
                    }
                    mGestureDetectorCompat.onTouchEvent(event);
                    return true;
                }
            });
        }
        mClickMode = MODE_DOUBLE_CLICK;
    }

    public void setLikeClickListener(LikeClickListener likeClickListener) {
        mLikeClickListener = likeClickListener;
    }

    public void setClickMode(ClickMode clickMode) {
        mClickMode = clickMode;
    }

    private GestureDetectorCompat getGestureDetectorCompat() {
        return new GestureDetectorCompat(mAppContext, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (MODE_DOUBLE_CLICK.equals(mClickMode)) {
                    if (mLikeClickListener != null) {
                        mLikeClickListener.onTriggerLike(MODE_DOUBLE_CLICK, e);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    public interface LikeClickListener {
        void onTriggerLike(ClickMode clickMode, MotionEvent event);
    }
}
