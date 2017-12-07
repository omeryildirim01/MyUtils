package com.xufang.myutils.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xufang on 2017/6/3.
 */
public class SelectItemLayout extends LinearLayout {
    private static final String TAG = "SelectItemLayout";

    private List<Item> mSubItems;
    private Map<View, Integer> mSubLayoutIndexMap;

    private int mSelectLayoutId;

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
                item.layoutId = subLayout.getId();

                mSubItems.add(item);
                mSubLayoutIndexMap.put(subLayout, subLayout.getId());
                subLayout.setOnClickListener(new View.OnClickListener() {
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
            return;
        }

        Integer layoutId = mSubLayoutIndexMap.get(subLayout);
        if (layoutId == null || layoutId == View.NO_ID) {
            return;
        }

        mSelectLayoutId = layoutId;
        for (int i = 0; i < mSubItems.size(); i++) {
            Item item = mSubItems.get(i);
            if (item.layoutId == layoutId) {
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

    public void setSelect(int layoutId) {
        if (!mHasInit) {
            return;
        }

        if (layoutId == View.NO_ID) {
            return;
        }

        mSelectLayoutId = layoutId;
        for (int i = 0; i < mSubItems.size(); i++) {
            Item item = mSubItems.get(i);
            if (item.layoutId == layoutId) {
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
        return mSelectLayoutId;
    }

    public interface ItemClickListener {
        void onItemClicked(int id);
    }

    private class Item {
        int layoutId;
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
