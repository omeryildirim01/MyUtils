package com.xufang.myutils.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.yy.mobile.memoryrecycle.views.YYLinearLayout;
import com.yy.mobile.util.log.MLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xufang on 2017/6/3.
 */
public class SelectItemLayout extends YYLinearLayout {
    private static final String TAG = "SelectItemLayout";

    private List<Item> mSubItems;
    private Map<View, Integer> mSubLayoutIndexMap;

    private int mSelectIndex;

    private boolean mHasInit = false;

    private ItemClickListener mItemClickListener;

    public SelectItemLayout(Context context) {
        super(context);
    }

    public SelectItemLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SelectItemLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        try {
            int itemsCount = getChildCount();
            mSubItems = new ArrayList<>(itemsCount);
            mSubLayoutIndexMap = new HashMap<>();
            for (int i = 0; i < itemsCount; i++) {
                final View subLayout = getChildAt(i);

                Item item = new Item();
                if ((subLayout instanceof ViewGroup)) {
                    for (int j = 0; j < ((ViewGroup) subLayout).getChildCount(); j++) {
                        View child = ((ViewGroup) subLayout).getChildAt(j);
                        item.addView(child);
                    }
                } else {
                    item.addView(subLayout);
                }

                mSubItems.add(item);
                mSubLayoutIndexMap.put(subLayout, i);
                subLayout.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setSelect(subLayout);
                        if (mItemClickListener != null) {
                            mItemClickListener.onItemClicked(mSubLayoutIndexMap.get(subLayout));
                        }
                    }
                });
            }
            mHasInit = true;
        } catch (Exception e) {
            mHasInit = false;
            MLog.error(TAG, e.toString());
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHasInit = false;
    }

    public void setItemClickListener(ItemClickListener listener) {
        mItemClickListener = listener;
    }

    public void setSelect(View subLayout) {
        if (!mHasInit) {
            MLog.info(TAG, "mHasInit is false");
            return;
        }

        Integer index = mSubLayoutIndexMap.get(subLayout);
        if (index == null) {
            MLog.error(TAG, "setSelected subLayout index is invalid");
            return;
        }

        mSelectIndex = index;
        for (int i = 0; i < mSubItems.size(); i++) {
            Item item = mSubItems.get(i);
            if (i == index) {
                for (int k = 0; k < item.views.size(); k++) {
                    item.views.get(k).setSelected(true);
                }
            } else {
                for (int k = 0; k < item.views.size(); k++) {
                    item.views.get(k).setSelected(false);
                }
            }
        }
    }

    public void setSelect(int index) {
        if (!mHasInit) {
            MLog.info(TAG, "mHasInit is false");
            return;
        }

        if (index < 0 || index >= mSubItems.size()) {
            MLog.error(TAG, "setSelect invalid index, index:%d, mItemsCount:%d", index, mSubItems.size());
            return;
        }

        mSelectIndex = index;
        for (int i = 0; i < mSubItems.size(); i++) {
            Item item = mSubItems.get(i);
            if (i == index) {
                for (int k = 0; k < item.views.size(); k++) {
                    item.views.get(k).setSelected(true);
                }
            } else {
                for (int k = 0; k < item.views.size(); k++) {
                    item.views.get(k).setSelected(false);
                }
            }
        }
    }

    public int getSelectIndex() {
        return mSelectIndex;
    }

    public ViewGroup getSelectedSubLayout() {
        try {
            return (ViewGroup) getChildAt(mSelectIndex);
        } catch (Exception e) {
            MLog.error(TAG, e.toString());
        }

        return null;
    }

    public interface ItemClickListener {
        void onItemClicked(int index);
    }

    private class Item {
        ArrayList<View> views;

        public Item() {
            views = new ArrayList<>();
        }

        public void addView(View view) {
            if (view != null) {
                views.add(view);
            }
        }
    }
}
