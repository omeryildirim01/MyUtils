package com.xufang.myutils.utils.like;

import java.util.LinkedList;

/**
 * Created by xufang on 2017/10/12.
 */

public class RecyclerPool<T> {
    private static final String TAG = "RecyclerPool";

    private static final int DEFAULT_CAPACITY = 10;

    private int mCapacity;
    private LinkedList<T> mPool;

    public RecyclerPool() {
        this(DEFAULT_CAPACITY);
    }

    public RecyclerPool(int capacity) {
        if (capacity > 0) {
            mCapacity = capacity;
        } else {
            mCapacity = DEFAULT_CAPACITY;
        }
        mPool = new LinkedList<>();
    }

    public void clear() {
        mPool.clear();
    }

    public boolean discard(T object) {
        if (mPool.size() >= mCapacity) {
            return false;
        }
        return mPool.offer(object);
    }

    public T obtain() {
        T target = mPool.poll();
        return target;
    }
}
