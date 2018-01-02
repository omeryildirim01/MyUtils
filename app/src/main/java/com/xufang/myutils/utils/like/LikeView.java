package com.xufang.myutils.utils.like;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.animation.LinearInterpolator;

import com.xufang.myutils.R;
import com.xufang.myutils.utils.DimensUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by xufang on 2017/9/27.
 */

public class LikeView extends SurfaceView {
    private static final String TAG = "LikeView";

    private Context mContext;

    private ValueAnimator mUpdateAnimator;

    //shared collection
    private List<LikeItem> mLikeItems;
    private RecyclerPool<LikeItem> mLikeItemRecyclerPool;

    private Bitmap mBitmap;
    private int mBitmapWidth, mBitmapHeight;

    private DrawThread mDrawThread;
    private SurfaceHolder mSurfaceHolder;
    private final AtomicBoolean mIsSurfaceValid = new AtomicBoolean(false);

    private LikeViewListener mLikeViewListener;

    public LikeView(Context context) {
        super(context);
        init(context);
    }

    public LikeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LikeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mLikeItems = new ArrayList<>();
        mLikeItemRecyclerPool = new RecyclerPool<>();

        mBitmapWidth = DimensUtils.dip2pixel(mContext, 150);
        mBitmapHeight = DimensUtils.dip2pixel(mContext, 150);

        initLikeBitmap();
        initSurfaceHolder();
        initUpdateAnimator();
    }

    public void addLike(int x, int y) {
        if (mDrawThread != null) {
            synchronized (mDrawThread.mLock) {
                LikeItem likeItem = getLikeItem();
                if (likeItem == null) {
                    return;
                }
                likeItem.setTouchPoint(x - mBitmapWidth / 2, y - mBitmapHeight / 2);
                likeItem.start();
                mLikeItems.add(likeItem);
                if (!mUpdateAnimator.isRunning()) {
                    mUpdateAnimator.start();
                }
            }
        }
    }

    public void clearLikeView() {
        if (mDrawThread == null || mDrawThread.mIsLikeListEmpty) {
            return;
        }

        synchronized (mDrawThread.mLock) {
            final List<LikeItem> likeItems = mLikeItems;
            for (LikeItem item : likeItems) {
                item.stop();
            }
        }
    }

    private LikeItem getLikeItem() {
        LikeItem likeItem = mLikeItemRecyclerPool.obtain();
        if (likeItem == null) {
            if (mBitmap != null) {
                likeItem = new LikeItem(mContext, mBitmap);
            }
        } else {
            likeItem.reset();
        }

        return likeItem;
    }

    public boolean isLiking() {
        return mDrawThread != null && !mDrawThread.mIsLikeListEmpty;
    }

    private void stopThread() {
        synchronized (mDrawThread.mLock) {
            final List<LikeItem> likeItems = mLikeItems;
            for (LikeItem item : likeItems) {
                item.stop();
            }
            likeItems.clear();
            mLikeItemRecyclerPool.clear();
        }

        mUpdateAnimator.cancel();
        mDrawThread.quit();
        mDrawThread = null;
    }

    public void setLikeViewListener(LikeViewListener likeViewListener) {
        mLikeViewListener = likeViewListener;
    }

    private void onPlayOver() {
        if (mLikeViewListener != null) {
            mLikeViewListener.onLikeViewPlayOver();
        }

        mUpdateAnimator.cancel();
    }

    private void initLikeBitmap() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mBitmap == null && mBitmapWidth > 0 && mBitmapHeight > 0) {
                    try {
                        mBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.small_video_like);
                        mBitmap = Bitmap.createScaledBitmap(mBitmap, mBitmapWidth, mBitmapHeight, false);
                    } catch (Throwable throwable) {

                    }
                }
            }
        }).start();
    }

    private void initUpdateAnimator() {
        mUpdateAnimator = ValueAnimator.ofFloat(0, 1);
        mUpdateAnimator.setInterpolator(new LinearInterpolator());
        mUpdateAnimator.setDuration(1000);
        mUpdateAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mUpdateAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (mIsSurfaceValid.get()) {
                    Surface surface = mSurfaceHolder.getSurface();
                    if (surface == null || !surface.isValid()) {
                        return;
                    }

                    synchronized (mIsSurfaceValid) {
                        if (mIsSurfaceValid.get() && mDrawThread != null) {
                            mDrawThread.updateFrameLoop();
                        }
                    }
                }
            }
        });
    }

    private void initSurfaceHolder() {
        setZOrderOnTop(true);
        mSurfaceHolder = getHolder();
        mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                synchronized (mIsSurfaceValid) {
                    mIsSurfaceValid.set(true);
                }
                if (mDrawThread == null) {
                    mDrawThread = new DrawThread("YY_IN_LIKEVIEW", LikeView.this);
                    mDrawThread.start();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                synchronized (mIsSurfaceValid) {
                    mIsSurfaceValid.set(false);
                    if (mDrawThread != null) {
                        stopThread();
                    }
                }
            }
        });
    }

    private static class DrawThread extends HandlerThread {
        private static final String TAG = "DrawThread";

        //DrawThread thread msg
        private static final int MSG_UPDATE_FRAME_LOOP = 4;

        //Main thread msg
        private static final int MSG_PLAY_OVER = 101;

        private Handler mDrawThreadHandler;
        private Handler mMainHandler;

        private volatile boolean mIsLikeListEmpty;
        private final byte[] mLock = new byte[0];

        private WeakReference<LikeView> mHost;

        DrawThread(String name, LikeView host) {
            super(name);
            mIsLikeListEmpty = true;
            mHost = new WeakReference<>(host);
            mMainHandler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    switch (msg.what) {
                        case MSG_PLAY_OVER:
                            onLikeViewPlayOver();
                            break;
                        default:
                            break;
                    }
                }
            };
        }

        //main thread
        private void updateFrameLoop() {
            sendMessage(MSG_UPDATE_FRAME_LOOP);
        }

        //main thread
        private void onLikeViewPlayOver() {
            if (mHost.get() != null) {
                mHost.get().onPlayOver();
            }
        }

        @Override
        protected void onLooperPrepared() {
            super.onLooperPrepared();
            initHandler();
        }

        private void initHandler() {
            mDrawThreadHandler = new Handler(getLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    switch (msg.what) {
                        case MSG_UPDATE_FRAME_LOOP:
                            infiniteLoop();
                            break;
                        default:
                            break;
                    }
                }
            };
        }

        private void infiniteLoop() {
            if (mHost.get() == null) {
                return;
            }
            if (!mHost.get().mIsSurfaceValid.get()) {
                return;
            }
            synchronized (mHost.get().mIsSurfaceValid) {
                if (mHost.get().mIsSurfaceValid.get()) {
                    Canvas canvas = null;
                    try {
                        if (mHost.get() != null) {
                            canvas = mHost.get().mSurfaceHolder.lockCanvas();
                        }
                        if (canvas != null) {
                            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                            updateFrame(canvas);
                        }
                    } catch (Exception ex) {
                        Log.e(TAG, "draw thread run ex:", ex);
                    } finally {
                        if (canvas != null && mHost.get() != null) {
                            mHost.get().mSurfaceHolder.unlockCanvasAndPost(canvas);
                        }
                    }
                }
            }
        }

        private void updateFrame(Canvas canvas) {
            if (mHost.get() == null) {
                return;
            }
            synchronized (mLock) {
                if (mHost.get() != null) {
                    final List<LikeItem> list = mHost.get().mLikeItems;
                    final RecyclerPool<LikeItem> pool = mHost.get().mLikeItemRecyclerPool;

                    Iterator<LikeItem> iterator = list.iterator();
                    while (iterator.hasNext()) {
                        LikeItem item = iterator.next();
                        if (item.isAnimating()) {
                            item.updateFrame(canvas);
                        } else {
                            pool.discard(item);
                            iterator.remove();
                            mIsLikeListEmpty = list.isEmpty();
                            if (mIsLikeListEmpty) {
                                mMainHandler.sendEmptyMessage(MSG_PLAY_OVER);
                            }
                        }
                    }
                }
            }
        }

        private void sendMessage(int msgType) {
            if (mDrawThreadHandler != null) {
                Message msg = Message.obtain();
                msg.what = msgType;
                mDrawThreadHandler.sendMessage(msg);
            }
        }
    }
}
