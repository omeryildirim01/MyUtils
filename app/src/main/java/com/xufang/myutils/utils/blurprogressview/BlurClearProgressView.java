package com.xufang.myutils.utils.blurprogressview;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.xufang.myutils.R;

/**
 * Created by xufang on 2017/11/24.
 */

public class BlurClearProgressView extends FrameLayout {
    private static final String TAG = "BlurClearProgressView";

    private static final int STYLE_CENTER_TO_EDGE = 1;
    private static final int STYLE_SIDE_TO_SIDE = 2;

    private static final int DEFAULT_BLUR_RADIUS = 40;

    private ImageView mClearImageView;
    private ProgressTransparentView mProgressTransparentView;

    private Context mContext;

    private int mStyle;
    private boolean mDetached;

    private String mImageUrl;

    private int mRadius = DEFAULT_BLUR_RADIUS;

    private Handler mMainHandler;
    private Runnable mLoadBlurCoverRunnable;

    public BlurClearProgressView(Context context) {
        this(context, null);
    }

    public BlurClearProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BlurClearProgressView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mContext = context;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BlurClearProgressView);
        mStyle = a.getColor(R.styleable.BlurClearProgressView_draw_clear_style, STYLE_CENTER_TO_EDGE);
        a.recycle();

        addImageView();

        mDetached = false;
        mMainHandler = new Handler(Looper.getMainLooper());
        mLoadBlurCoverRunnable = new Runnable() {
            @Override
            public void run() {
                if (!mDetached && (mImageUrl != null && !mImageUrl.isEmpty())) {
                    // TODO: 2017/12/7 load blur image on mProgressTransparentView by mImageUrl
//                    ImageLoader.Builder.obtain(mProgressTransparentView, mImageUrl).setTransform(new YYBlurBitmapTransformation(mRadius)).load();
                }
            }
        };
    }

    public void setUrl(String url) {
        mImageUrl = url;
    }

    public void setProgress(final long progress, final long max) {
        if ((mProgressTransparentView).getWidth() > 0 && (mProgressTransparentView).getHeight() > 0) {
            mProgressTransparentView.setProgress(progress, max);
        } else {
            (mProgressTransparentView).addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (mProgressTransparentView.equals(v) && (right - left) > 0 && (bottom - top) > 0) {
                        (mProgressTransparentView).removeOnLayoutChangeListener(this);
                        mProgressTransparentView.setProgress(progress, max);
                    }
                }
            });
        }
    }

    public void loadClearImage() {
        if (mImageUrl == null || mImageUrl.isEmpty()) {
            return;
        }

        showClearView();
        hideBlurView();
        // TODO: 2017/12/7 load image on mClearImageView by mImageUrl
//        ImageLoader.loadImage(mClearImageView, mImageUrl);
    }

    public void loadBlurImage() {
        if (mImageUrl == null || mImageUrl.isEmpty()) {
            return;
        }

        showBlurView();
        loadBlurImage(DEFAULT_BLUR_RADIUS);
    }

    public void loadBlurImage(int radius) {
        if (mImageUrl == null || mImageUrl.isEmpty()) {
            return;
        }

        mRadius = radius;
        showBlurView();
        loadBlurImage(radius, false);
    }

    public void loadBlurImage(int redius, boolean needToLoadClearImageFirst) {
        if (mImageUrl == null || mImageUrl.isEmpty()) {
            return;
        }

        mRadius = redius;
        showBlurView();
        if (needToLoadClearImageFirst) {
            // TODO: 2017/12/7 load image on mClearImageView by mImageUrl
//            ImageLoader.loadImage(mClearImageView, mImageUrl, -1, -1, new ImageLoader.ImageLoadListener() {
//                @Override
//                public void onLoadFailed(Exception e) {
//                    MLog.error(TAG, "loadBlurImage mCoverImageView onLoadFailed", e);
//                }
//
//                @Override
//                public void onResourceReady(Object resource, boolean isFromMemoryCache) {
//                    if (!mDetached) {
//                        mMainHandler.post(mLoadBlurCoverRunnable);
//                    }
//                }
//            });
        } else {
            // TODO: 2017/12/7 load blur image on mProgressTransparentView by mImageUrl
//            ImageLoader.Builder.obtain(mProgressTransparentView, mImageUrl).setTransform(new YYBlurBitmapTransformation(mRadius)).load();
        }
    }

    public void showClearView() {
        hideBlurView();
        mClearImageView.setVisibility(VISIBLE);
    }

    public void showBlurView() {
        mProgressTransparentView.setVisibility(VISIBLE);
    }

    public void hideBlurView() {
        mProgressTransparentView.setVisibility(GONE);
    }

    private void addImageView() {
        removeAllViews();
        mClearImageView = new ImageView(mContext);
        addView(mClearImageView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        initProgressTransparentView();
        addView(mProgressTransparentView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void initProgressTransparentView() {
        switch (mStyle) {
            case STYLE_CENTER_TO_EDGE:
                mProgressTransparentView = new CenterToEdgeTransparentView(mContext);
                break;
            case STYLE_SIDE_TO_SIDE:
                mProgressTransparentView = new SideToSideTransparentView(mContext);
                break;
            default:
                mProgressTransparentView = new CenterToEdgeTransparentView(mContext);
                break;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        mDetached = true;
        mMainHandler.removeCallbacks(mLoadBlurCoverRunnable);
        super.onDetachedFromWindow();
    }
}
