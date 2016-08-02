package com.wangxiandeng.captainpulltorefresh;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private PullToRefreshRecyclerView mPullRecyclerView;
    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPullRecyclerView = (PullToRefreshRecyclerView) findViewById(R.id.recyclerview);
        mRecyclerView = mPullRecyclerView.getRefreshRecyclerView();
        //设置布局管理器
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            list.add("" + i);
        }
        mAdapter = new MyAdapter(this, list);
        mRecyclerView.setAdapter(mAdapter);
        mPullRecyclerView.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void refreshing() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPullRecyclerView.finsihRefreshing();
                    }
                }, 1000);
            }
        });
    }
}
