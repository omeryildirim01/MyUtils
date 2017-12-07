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

    private SurfaceHolder mSurfaceHolder;
    private DrawThread mDrawThread;
    private Context mContext;
    private LikeViewListener mLikeViewListener;
    private final AtomicBoolean mIsSurfaceValid = new AtomicBoolean(false);

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
        initSurfaceHolder();
    }

    public void addLike(int x, int y) {
        if (mDrawThread != null) {
            mDrawThread.onAddLike(x, y);
        }
    }

    public boolean isLiking() {
        return mDrawThread != null && !mDrawThread.mIsLikeListEmpty;
    }

    public void clear() {
        if (mDrawThread != null) {
            mDrawThread.clearAnimations();
        }
    }

    public void setLikeViewListener(LikeViewListener likeViewListener) {
        mLikeViewListener = likeViewListener;
    }

    private void initSurfaceHolder() {
        setZOrderOnTop(true);
        mSurfaceHolder = getHolder();
        mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                mIsSurfaceValid.set(true);
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
                        mDrawThread.stopThread();
                        mDrawThread = null;
                    }
                }
            }
        });
    }

    private static class DrawThread extends HandlerThread {
        private static final String TAG = "DrawThread";

        //DrawThread thread msg
        private static final int MSG_ADD_LIKE = 1;
        private static final int MSG_STOP = 2;
        private static final int MSG_CLEAR = 3;

        //Main thread msg
        private static final int MSG_PLAY_OVER = 101;

        private List<LikeItem> mLikeItems;
        private Bitmap mBitmap;
        private int mBitmapWidth, mBitmapHeight;
        private Handler mDrawThreadHandler;
        private Handler mMainHandler;
        private ValueAnimator mUpdateAnimator;
        private RecyclerPool<LikeItem> mLikeItemRecyclerPool;

        private volatile boolean mIsLikeListEmpty;
        private final byte[] mWakeUpLock = new byte[0];

        private WeakReference<LikeView> mHost;

        DrawThread(String name, LikeView host) {
            super(name);
            mLikeItems = new ArrayList<>();
            mIsLikeListEmpty = true;
            mLikeItemRecyclerPool = new RecyclerPool<>();
            mHost = new WeakReference<>(host);
            initUpdateAnimator();
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

        private void initUpdateAnimator() {
            mUpdateAnimator = ValueAnimator.ofFloat(0, 1);
            mUpdateAnimator.setDuration(1000);
            mUpdateAnimator.setRepeatCount(ValueAnimator.INFINITE);
            mUpdateAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    if (mHost.get() != null && mHost.get().mIsSurfaceValid.get()) {
                        Surface surface = mHost.get().mSurfaceHolder.getSurface();
                        if (surface == null || !surface.isValid()) {
                            return;
                        }

                        tryToWait();
                        synchronized (mHost.get().mIsSurfaceValid) {
                            if (mHost.get().mIsSurfaceValid.get()) {
                                infiniteLoop();
                            }
                        }
                    }
                }
            });
        }

        private void tryToWait() {
            synchronized (mWakeUpLock) {
                while (mIsLikeListEmpty) {
                    try {
                        mWakeUpLock.wait();
                    } catch (InterruptedException e) {

                    }
                }
            }
        }

        private void infiniteLoop() {
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

        private void ensureBitmap() {
            if (mHost.get() != null && mBitmapWidth == 0 && mBitmapHeight == 0) {
                mBitmapWidth = DimensUtils.dip2pixel(mHost.get().mContext, 150);
                mBitmapHeight = DimensUtils.dip2pixel(mHost.get().mContext, 150);
            }
            if (mHost.get() != null && mBitmap == null && mBitmapWidth > 0 && mBitmapHeight > 0) {
                mBitmap = BitmapFactory.decodeResource(mHost.get().mContext.getResources(), R.drawable.small_video_like);
                mBitmap = Bitmap.createScaledBitmap(mBitmap, mBitmapWidth, mBitmapHeight, false);
            }
        }

        //main thread
        private void onAddLike(int x, int y) {
            wakeUp();
            sendMessage(MSG_ADD_LIKE, x, y);
        }

        //main thread
        private void wakeUp() {
            synchronized (mWakeUpLock) {
                mIsLikeListEmpty = false;
                mWakeUpLock.notify();
            }
        }

        //main thread
        private void clearAnimations() {
            if (!mIsLikeListEmpty) {
                sendMessage(MSG_CLEAR, null);
            }
        }

        //main thread
        private void onLikeViewPlayOver() {
            if (mHost.get() != null && mHost.get().mLikeViewListener != null) {
                mHost.get().mLikeViewListener.onLikeViewPlayOver();
            }
        }

        private void stopThread() {
            wakeUp();
            sendMessage(MSG_STOP, null);
        }

        @Override
        protected void onLooperPrepared() {
            super.onLooperPrepared();
            initHandler();
            mUpdateAnimator.start();
        }

        private void initHandler() {
            mDrawThreadHandler = new Handler(getLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    switch (msg.what) {
                        case MSG_ADD_LIKE:
                            addLikeItemToList(msg);
                            break;
                        case MSG_STOP:
                            stopSelf();
                            break;
                        case MSG_CLEAR:
                            clear();
                            break;
                        default:
                            break;
                    }
                }
            };
        }

        private void addLikeItemToList(Message message) {
            LikeItem likeItem = getLikeItem();
            if (likeItem != null && message != null) {
                likeItem.setTouchPoint(message.arg1 - mBitmapWidth / 2, message.arg2 - mBitmapHeight / 2);
                likeItem.start();
                mLikeItems.add(likeItem);
                mIsLikeListEmpty = false;
            }
        }

        private LikeItem getLikeItem() {
            LikeItem likeItem = mLikeItemRecyclerPool.obtain();
            if (likeItem == null) {
                if (mHost.get() != null) {
                    ensureBitmap();
                    likeItem = new LikeItem(mHost.get().mContext, mBitmap);
                }
            } else {
                likeItem.reset();
            }

            return likeItem;
        }

        private void stopSelf() {
            for (LikeItem item : mLikeItems) {
                item.stop();
            }
            mLikeItems.clear();
            mUpdateAnimator.cancel();
            mLikeItemRecyclerPool.clear();
            quit();
        }

        private void clear() {
            if (mIsLikeListEmpty) {
                return;
            }

            for (LikeItem item : mLikeItems) {
                item.stop();
            }
        }

        private void updateFrame(Canvas canvas) {
            Iterator<LikeItem> iterator = mLikeItems.iterator();
            while (iterator.hasNext()) {
                LikeItem item = iterator.next();
                if (item.isAnimating()) {
                    item.updateFrame(canvas);
                } else {
                    mLikeItemRecyclerPool.discard(item);
                    iterator.remove();
                    mIsLikeListEmpty = mLikeItems.isEmpty();
                    if (mIsLikeListEmpty) {
                        mMainHandler.sendEmptyMessage(MSG_PLAY_OVER);
                    }
                }
            }
        }

        private void sendMessage(int msgType, Object o) {
            if (mDrawThreadHandler != null) {
                Message msg = Message.obtain();
                msg.what = msgType;
                msg.obj = o;
                mDrawThreadHandler.sendMessage(msg);
            }
        }

        private void sendMessage(int msgType, int arg1, int arg2) {
            if (mDrawThreadHandler != null) {
                Message msg = Message.obtain();
                msg.what = msgType;
                msg.arg1 = arg1;
                msg.arg2 = arg2;
                mDrawThreadHandler.sendMessage(msg);
            }
        }
    }
}
