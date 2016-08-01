package com.wangxiandeng.captainpulltorefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by xingzhu on 16/8/1.
 */

public class HeaderView extends LinearLayout {
    private TextView mTextHead;

    public HeaderView(Context context) {
        super(context);
        init(context);
    }

    public HeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public HeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_header, this, true);
        mTextHead = (TextView) findViewById(R.id.text_header);
    }

    public void setHeaderText(String text) {
        mTextHead.setText(text);
    }
}