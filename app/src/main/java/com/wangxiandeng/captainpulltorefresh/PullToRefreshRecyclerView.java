package com.wangxiandeng.captainpulltorefresh;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Created by xingzhu on 16/8/1.
 */

public class PullToRefreshRecyclerView extends LinearLayout implements IPullToRefresh {

    private static final int NORMAL = 0;//正常状态
    private static final int PULL_TO_REFRESH = 1;//开始下拉
    private static final int RELEASE_TO_REFRESH = 2;//超过边界,释放刷新
    private static final int REFRESHING = 3;//刷新状态

    private int REFRESH_LIMIT_HEIGHT = 0;//下拉刷新边界

    private int mLastX;
    private int mLastY;

    private int mCurrentMode;//当前状态

    private HeaderView mHeaderView;
    private RecyclerView mRecyclerView;
    private OnRefreshListener mRefreshListener;
    private int mDs = 0;
    private float mScale = 1f;
    private boolean mIsFromTop;
    private ValueAnimator mReleaseAnimator;
    private ValueAnimator mFinishRefreshAnimator;


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
        post(new Runnable() {
            @Override
            public void run() {
                LinearLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                params.topMargin = params.topMargin - dip2px(60);
                setLayoutParams(params);
            }
        });
        REFRESH_LIMIT_HEIGHT = dip2px(60);
    }

    @Override
    public RecyclerView getRefreshRecyclerView() {
        return mRecyclerView;
    }

    @Override
    public void setOnRefreshListener(OnRefreshListener listener) {
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
        if (mCurrentMode != NORMAL) {
            if (mCurrentMode == REFRESHING) {
                return super.onInterceptTouchEvent(ev);
            }
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = (int) event.getX();
                mLastY = (int) event.getY();
                if (isChildOnTop()) {
                    mIsFromTop = true;
                } else {
                    mIsFromTop = false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (!mIsFromTop) {
                    return super.dispatchTouchEvent(event);
                }
                int x = (int) event.getX();
                int y = (int) event.getY();
                if (Math.abs(x - mLastX) < Math.abs(y - mLastY)) {
                    if (!isChildOnTop()) {
                        mIsFromTop = false;
                        return super.dispatchTouchEvent(event);
                    }
                    if ((y - mLastY > 0) && isChildOnTop()) {
                        if (mCurrentMode == REFRESHING) {
                            return super.dispatchTouchEvent(event);
                        }
                        if ((-mDs) < REFRESH_LIMIT_HEIGHT) {
                            mCurrentMode = PULL_TO_REFRESH;
                            mHeaderView.setHeaderText("pull to refresh");
                        } else {
                            mCurrentMode = RELEASE_TO_REFRESH;
                            mHeaderView.setHeaderText("release to refresh");
                        }
                        startPull(y - mLastY);
                    } else {
                        if (mCurrentMode != NORMAL) {
                            startPull(y - mLastY);
                            if ((-mDs) >= REFRESH_LIMIT_HEIGHT) {
                                mCurrentMode = RELEASE_TO_REFRESH;
                                mHeaderView.setHeaderText("release to refresh");
                            } else if ((-mDs) < REFRESH_LIMIT_HEIGHT && (-mDs) > 0) {
                                mCurrentMode = PULL_TO_REFRESH;
                                mHeaderView.setHeaderText("pull to refresh");

                            } else {
                                mDs = 0;
                                mCurrentMode = NORMAL;
                            }
                        }
                    }
                }
                Log.e("Mode", "" + mCurrentMode);
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_UP:
                release();
                break;
        }

        return super.dispatchTouchEvent(event);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mCurrentMode != NORMAL) {
            if (mCurrentMode == REFRESHING) {
                return super.onTouchEvent(event);
            }
            return true;
        }
        return super.onTouchEvent(event);
    }

    private void startPull(int ds) {//手动下拉
        mDs = (int) (mDs - ds * mScale);
        if (mDs > 0) {
            mDs = 0;
        }
        scrollTo(0, mDs);
        mScale = 1 - (((float) (-mDs) / (float) (dip2px(150))));
    }

    private boolean isChildOnTop() {//判断recyclerView是否拉到顶
        View child = mRecyclerView.getChildAt(0);
        return null == child ? true : child.getTop() >= 0;
    }

    public int dip2px(float dip) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dip, getResources().getDisplayMetrics()
        );
    }

    private void release() {//手指松开时
        mReleaseAnimator = ValueAnimator.ofInt(0, 1).setDuration(500);
        mReleaseAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = animation.getAnimatedFraction();
                if (mCurrentMode == PULL_TO_REFRESH) {
                    mDs = (int) (mDs * (1 - fraction));
                    scrollTo(0, mDs);
                    if (mDs == 0) {
                        mCurrentMode = NORMAL;
                    }
                } else if (mCurrentMode == RELEASE_TO_REFRESH) {
                    if ((-mDs) > REFRESH_LIMIT_HEIGHT) {
                        mDs = (int) (mDs * (1 - fraction));
                        if ((-mDs) < REFRESH_LIMIT_HEIGHT) {
                            mDs = -REFRESH_LIMIT_HEIGHT;
                        }
                        scrollTo(0, mDs);
                    } else {
                        refreshing();
                    }
                }
            }
        });
        mReleaseAnimator.start();
    }

    private void refreshing() {//执行刷新
        mCurrentMode = REFRESHING;
        mHeaderView.setHeaderText("refreshing....");
        if (mRefreshListener != null) {
            mRefreshListener.refreshing();
        }
    }

    public void finsihRefreshing() {//结束刷新
        mFinishRefreshAnimator = ValueAnimator.ofInt(0, 1).setDuration(500);
        mFinishRefreshAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = animation.getAnimatedFraction();
                if (mCurrentMode == REFRESHING) {
                    mDs = (int) (mDs * (1 - fraction));
                    scrollTo(0, mDs);
                    if (mDs == 0) {
                        mCurrentMode = NORMAL;
                    }
                }
            }
        });
        mFinishRefreshAnimator.start();
    }
}
