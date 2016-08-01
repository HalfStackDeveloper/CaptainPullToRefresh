package com.wangxiandeng.captainpulltorefresh;

import android.support.v7.widget.RecyclerView;

/**
 * Created by xingzhu on 16/8/1.
 */

public interface IPullToRefresh {
    RecyclerView getRefreshRecyclerView();

    void setRefreshListener(IRefreshListener listener);
}
