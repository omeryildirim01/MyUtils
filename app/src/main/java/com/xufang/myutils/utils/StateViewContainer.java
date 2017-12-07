package com.xufang.myutils.utils;

import android.content.Context;
import android.support.v4.util.SparseArrayCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewStub;
import android.widget.FrameLayout;

import java.util.List;

/**
 * Created by xufang on 2017/11/18.
 * 通用的根据状态显示View的控件，使用ViewStub实现懒加载，调用者只需传入state以及layoutId，
 * 需要显示状态时调用{@link #showState(int state)}即可。
 */

public class StateViewContainer extends FrameLayout {
    private static final String TAG = "StateViewContainer";

    private Context mContext;

    private boolean mHasInitStateItem;
    private int mCurrentShowState = Integer.MIN_VALUE;
    private StateItem mInitStateItem;
    private SparseArrayCompat<StateItem> mStateMap;
    private SparseArrayCompat<ViewStub> mViewStubMap;
    private SparseArrayCompat<View> mViewMap;

    public StateViewContainer(Context context) {
        this(context, null);
    }

    public StateViewContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StateViewContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        mStateMap = new SparseArrayCompat<>();
        mViewStubMap = new SparseArrayCompat<>();
        mViewMap = new SparseArrayCompat<>();
    }

    public View getStateView(int state) {
        return mViewMap.get(state);
    }

    public void showState(int state) {
        if (state == mCurrentShowState) {
            return;
        }

        for (int i = 0; i < mViewMap.size(); i++) {
            View view = mViewMap.valueAt(i);
            if (view != null) {
                view.setVisibility(INVISIBLE);
            }
        }

        View view = mViewMap.get(state);
        if (view == null) {
            inflateStateView(state);
        }
        if (view != null) {
            view.setVisibility(VISIBLE);
        }
        mCurrentShowState = state;
    }

    public void setStates(List<StateItem> stateItems) {
        if (stateItems == null || stateItems.isEmpty()) {
            return;
        }

        clearAllMap();
        removeAllViews();

        for (StateItem item : stateItems) {
            addState(item);
        }

        checkInitStateItem(stateItems);
        inflateStateView(mInitStateItem.mState);
    }

    private void inflateStateView(int state) {
        ViewStub viewStub = mViewStubMap.get(state);
        if (viewStub != null) {
            try {
                View view = viewStub.inflate();
                if (view != null) {
                    mViewMap.put(state, view);
                }
            } catch (Throwable t) {

            }
        }
    }

    private void checkInitStateItem(List<StateItem> stateItems) {
        if (!mHasInitStateItem) {
            mInitStateItem = stateItems.get(0);
            mInitStateItem.mIsInitState = true;
            mHasInitStateItem = true;
        }
    }

    private void addState(StateItem stateItem) {
        if (stateItem == null) {
            return;
        }

        if (stateItem.mIsInitState) {
            if (mInitStateItem == null) {
                mInitStateItem = stateItem;
            } else {
                stateItem.mIsInitState = false;
            }
            mHasInitStateItem = true;
        }
        mStateMap.put(stateItem.mState, stateItem);

        ViewStub viewStub = new ViewStub(mContext, stateItem.mStateLayoutResId);
        addView(viewStub);
        mViewStubMap.put(stateItem.mState, viewStub);
    }

    private void clearAllMap() {
        mStateMap.clear();
        mViewStubMap.clear();
        mViewMap.clear();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        clearAllMap();
    }

    public static class StateItem {
        boolean mIsInitState;
        int mState;
        int mStateLayoutResId;

        public StateItem(int mState, int mStateLayoutResId) {
            this.mState = mState;
            this.mStateLayoutResId = mStateLayoutResId;
        }

        public StateItem(boolean mIsInitState, int mState, int mStateLayoutResId) {
            this.mIsInitState = mIsInitState;
            this.mState = mState;
            this.mStateLayoutResId = mStateLayoutResId;
        }
    }
}
