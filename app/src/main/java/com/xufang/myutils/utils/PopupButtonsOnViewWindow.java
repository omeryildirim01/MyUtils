package com.xufang.myutils.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by xufang on 2017/6/6.
 */

public abstract class PopupButtonsOnViewWindow extends PopupWindow {
    private static final String TAG = "PopupButtonsOnViewWindow";
    protected List<String> mTextList;
    private Context mContext;
    private LinearLayout mButtonsContainerLayout;
    private DynamicItemViewByDataListLayout<String> dynamicLayout;

    public PopupButtonsOnViewWindow(Context context) {
        mContext = context;
        mTextList = new ArrayList<>();
        View contentView = LayoutInflater.from(context).inflate(getContentViewLayoutId(), null);
        setContentView(contentView);
        initConfig();
        initView(contentView);
    }

    abstract int getContentViewLayoutId();

    abstract int getButtonViewLayoutId();

    abstract LinearLayout getButtonsContainerLayout(View contentView);

    private void initConfig() {
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setFocusable(true);
        this.setBackgroundDrawable(new BitmapDrawable());
        this.setOutsideTouchable(true);
        this.update();
    }

    private void initView(View contentView) {
        mButtonsContainerLayout = getButtonsContainerLayout(contentView);
    }

    public void setTextList(String... text) {
        if (text.length == 0) {
            return;
        }
        List<String> textList = new ArrayList<>();
        textList.addAll(Arrays.asList(text));
        setTextList(textList);
    }

    public void setTextList(List<String> textList) {
        if (textList == null || textList.isEmpty()) {
            return;
        }

        if (textList.equals(mTextList)) {
            return;
        }

        if (mButtonsContainerLayout == null) {
            return;
        }

        mTextList.clear();
        mTextList.addAll(textList);

        if (dynamicLayout == null) {
            dynamicLayout = new DynamicItemViewByDataListLayout<>(mButtonsContainerLayout);
            dynamicLayout.setItemViewLayoutId(getButtonViewLayoutId());
            dynamicLayout.setBindListener(new DynamicItemViewByDataListLayout.BindViewListener<String>() {
                @Override
                public void onBindView(int index, View view, String s) {
                    onBindItemView(index, view, s);
                }
            });
        }
        dynamicLayout.setDataList(textList);
    }

    protected void onBindItemView(int index, View view, String str) {

    }

    public void show(View parentView, View touchView) {
        initConfig();
        if (!this.isShowing()) {
            /*测量popupwindow宽高*/
            this.getContentView().measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int height = this.getContentView().getMeasuredHeight();
            int width = this.getContentView().getMeasuredWidth();
            WindowManager wm = ((Activity) mContext).getWindowManager();
            int x = (wm.getDefaultDisplay().getWidth() - width) / 2;
            int[] location = new int[2];
            touchView.getLocationInWindow(location);
            int y = location[1] - 35;
            this.showAtLocation(parentView, Gravity.NO_GRAVITY, x, y);
        } else {
            this.dismiss();
        }
    }
}
