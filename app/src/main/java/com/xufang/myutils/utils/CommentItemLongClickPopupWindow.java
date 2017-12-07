package com.xufang.myutils.utils;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xufang.myutils.R;

/**
 * Created by xufang on 2017/8/25.
 */

public class CommentItemLongClickPopupWindow extends PopupButtonsOnViewWindow {
    private static final String TAG = "CommentItemLongClickPopupWindow";

    private OnItemClickListener mOnItemClickListener;

    public CommentItemLongClickPopupWindow(Context context) {
        super(context);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @Override
    int getContentViewLayoutId() {
        return R.layout.comment_item_popup_window;
    }

    @Override
    int getButtonViewLayoutId() {
        return R.layout.comment_item_popup_item_view;
    }

    @Override
    LinearLayout getButtonsContainerLayout(View contentView) {
        return (LinearLayout) contentView.findViewById(R.id.ll_button_container);
    }

    @Override
    protected void onBindItemView(final int index, final View view, final String str) {
        super.onBindItemView(index, view, str);
        setDividerVisible(index, view);

        TextView textView = (TextView) view.findViewById(R.id.tv_button);
        if (textView != null && str != null && !str.isEmpty()) {
            textView.setText(str);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onClick(view, index, str);
                    }
                }
            });
        }
    }

    private void setDividerVisible(int index, View view) {
        if (mTextList == null || mTextList.isEmpty()) {
            return;
        }

        View dividerRight = view.findViewById(R.id.divider_right);
        if (dividerRight != null && index < mTextList.size() - 1) {
            dividerRight.setVisibility(View.VISIBLE);
        }
    }

    public interface OnItemClickListener {
        void onClick(View view, int index, String text);
    }
}
