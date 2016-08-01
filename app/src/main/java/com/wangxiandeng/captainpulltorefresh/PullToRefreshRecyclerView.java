package com.wangxiandeng.captainpulltorefresh;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Created by xingzhu on 16/8/1.
 */

public class PullToRefreshRecyclerView extends LinearLayout implements IPullToRefresh {

    private static final int NORMAL = 0;
    private static final int PULL_TO_REFRESH = 1;
    private static final int RELEASE_TO_REFRESH = 2;
    private static final int REFRESHING = 3;

    private static final int REFRESH_LIMIT_HEIGHT = 100;

    private int mLastX;
    private int mLastY;

    private int mCurrentMode;

    private HeaderView mHeaderView;
    private RecyclerView mRecyclerView;
    private IRefreshListener mRefreshListener;


    public PullToRefreshRecyclerView(Context context) {
        super(context);
        init(context, null);
    }

    public PullToRefreshRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public PullToRefreshRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setOrientation(VERTICAL);
        createHeaderView(context);
        createRecyclerView(context, attrs);
        addView(mHeaderView);
        addView(mRecyclerView);
        mCurrentMode = NORMAL;
        postDelayed(new Runnable() {
            @Override
            public void run() {
                LinearLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                params.topMargin = params.topMargin-120;
                setLayoutParams(params);
            }
        },100);
    }

    @Override
    public RecyclerView getRefreshRecyclerView() {
        return mRecyclerView;
    }

    @Override
    public void setRefreshListener(IRefreshListener listener) {
        mRefreshListener = listener;
    }

    private void createRecyclerView(Context context, AttributeSet attrs) {
        mRecyclerView = new RecyclerView(context, attrs);
    }

    private void createHeaderView(Context context) {
        mHeaderView = new HeaderView(context);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
//        if (mCurrentMode != NORMAL) {
//            return true;
//        }
//        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = (int) event.getX();
                mLastY = (int) event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                int x = (int) event.getX();
                int y = (int) event.getY();
                if (Math.abs(x - mLastX) < Math.abs(y - mLastY)) {
                    if (y - mLastY > 0) {
                        if (y - mLastY < REFRESH_LIMIT_HEIGHT) {
                            mCurrentMode = PULL_TO_REFRESH;
                            mHeaderView.setHeaderText("pull to refresh");
                        } else {
                            mCurrentMode = REFRESH_LIMIT_HEIGHT;
                            mHeaderView.setHeaderText("release to refresh");
                        }
                        startPull(y - mLastY);
                    } else {
                        if (((LinearLayout.LayoutParams) getLayoutParams()).topMargin > (-mHeaderView.getHeight())) {
                            startPull(y - mLastY);
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    private void startPull(int ds) {
        LinearLayout.LayoutParams params = (LayoutParams) getLayoutParams();
        params.topMargin = params.topMargin + ds;
        setLayoutParams(params);
    }

    private boolean isChildOnTop() {
        return ((RecyclerView.LayoutParams) getLayoutParams()).topMargin <= (-mHeaderView.getHeight()) ? true : false;
    }
}
