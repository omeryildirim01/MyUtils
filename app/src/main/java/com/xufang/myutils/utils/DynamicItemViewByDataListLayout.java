package com.xufang.myutils.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xufang on 2017/8/24.
 */

public class DynamicItemViewByDataListLayout<DATA> {
    private static final String TAG = "DynamicItemViewByDataListLayout";

    private static final int DEFAULT_CAPACITY = 5;

    private int mItemLayoutId;
    private LinearLayout mContainerLayout;
    private List<ItemView> mItemViewList;
    private BindViewListener<DATA> mBindListener;

    private List<DATA> mDataList;

    public DynamicItemViewByDataListLayout(LinearLayout layout) {
        this(DEFAULT_CAPACITY, layout);
    }

    public DynamicItemViewByDataListLayout(int capacity, LinearLayout layout) {
        mDataList = new ArrayList<>(capacity);
        mItemViewList = new ArrayList<>(capacity);
        mContainerLayout = layout;
    }

    public void setItemViewLayoutId(int layoutId) {
        mItemLayoutId = layoutId;
    }

    public void setBindListener(BindViewListener<DATA> bindListener) {
        mBindListener = bindListener;
    }

    public void setDataList(List<DATA> dataList) {

        if (dataList == null || dataList.isEmpty()) {
            removeAllItemView();
            return;
        }

        if (mDataList.equals(dataList)) {
            return;
        }

        removeAllItemView();

        mDataList.addAll(dataList);

        for (int i = 0; i < mDataList.size(); i++) {
            View view = safeInflateView();
            if (view == null) {
                return;
            }
            addItemView(i, view);
        }

        if (mItemViewList == null || mItemViewList.isEmpty() || mDataList.size() != mItemViewList.size()) {
            return;
        }

        for (ItemView itemView : mItemViewList) {
            if (mBindListener != null) {
                mBindListener.onBindView(itemView.mIndex, itemView.mView, mDataList.get(itemView.mIndex));
            }
        }
    }

    private View safeInflateView() {
        View view = null;
        try {
            view = LayoutInflater.from(mContainerLayout.getContext()).inflate(mItemLayoutId, null);
        } catch (Exception e) {

        }
        return view;
    }

    private void removeAllItemView() {
        mDataList.clear();
        mItemViewList.clear();
        if (mContainerLayout != null) {
            mContainerLayout.removeAllViews();
        }
    }

    private void addItemView(int index, View view) {
        ItemView itemView = new ItemView(index, view);
        mItemViewList.add(itemView);

        if (mContainerLayout != null) {
            mContainerLayout.addView(view);
        }
    }

    public interface BindViewListener<DATA> {
        void onBindView(int index, View view, DATA data);
    }

    private static class ItemView {
        int mIndex;
        View mView;

        ItemView(int index, View view) {
            this.mIndex = index;
            this.mView = view;
        }
    }
}
