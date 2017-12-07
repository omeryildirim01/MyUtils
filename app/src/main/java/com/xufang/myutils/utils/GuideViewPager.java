package com.xufang.myutils.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xufang.myutils.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xufang on 2017/5/28.
 */

public class GuideViewPager extends ViewPager {
    private int mPageResId1;
    private int mPageResId2;
    private int mPageResId3;
    private int mPageResId4;
    private int mAckViewId;

    private Context mContext;

    private List<View> mPageList;

    private AckClickListener mAckClickListener;

    public void setAckClickListener(AckClickListener ackClickListener) {
        this.mAckClickListener = ackClickListener;
    }

    public GuideViewPager(Context context) {
        this(context, null);
    }

    public GuideViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        mPageList = new ArrayList<>(4);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.GuideViewPager);
        mPageResId1 = ta.getResourceId(R.styleable.GuideViewPager_gvp_page1, 0);
        mPageResId2 = ta.getResourceId(R.styleable.GuideViewPager_gvp_page2, 0);
        mPageResId3 = ta.getResourceId(R.styleable.GuideViewPager_gvp_page3, 0);
        mPageResId4 = ta.getResourceId(R.styleable.GuideViewPager_gvp_page4, 0);
        mAckViewId = ta.getResourceId(R.styleable.GuideViewPager_gvp_ack_view_id, 0);
        ta.recycle();

        setPageList();
        setAckClickEvent();
        setAdapter(new GuidePagerAdapter());
    }

    @Override
    protected void onDetachedFromWindow() {
        mPageList.clear();
        super.onDetachedFromWindow();
    }

    private void setAckClickEvent() {
        if (mPageList.isEmpty()) {
            return;
        }
        View lastPage = mPageList.get(mPageList.size() - 1);
        View ackView = lastPage.findViewById(mAckViewId);
        if (ackView != null) {
            ackView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mAckClickListener != null) {
                        mAckClickListener.onClickAck();
                    }
                }
            });
        }
    }

    private void setPageList() {
        if (mPageResId1 != 0) {
            View view = LayoutInflater.from(mContext).inflate(mPageResId1, null);
            mPageList.add(view);
        }
        if (mPageResId2 != 0) {
            View view = LayoutInflater.from(mContext).inflate(mPageResId2, null);
            mPageList.add(view);
        }
        if (mPageResId3 != 0) {
            View view = LayoutInflater.from(mContext).inflate(mPageResId3, null);
            mPageList.add(view);
        }
        if (mPageResId4 != 0) {
            View view = LayoutInflater.from(mContext).inflate(mPageResId4, null);
            mPageList.add(view);
        }
    }

    private class GuidePagerAdapter extends PagerAdapter {
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = mPageList.get(position);
            container.addView(view, position);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mPageList.get(position));
        }

        @Override
        public int getCount() {
            return mPageList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

    public interface AckClickListener {
        void onClickAck();
    }
}
