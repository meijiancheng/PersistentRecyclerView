package com.stone.persistent

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.AppBarLayout
import com.stone.persistent.adapter.FeedsPagerAdapter
import com.stone.persistent.adapter.HomeTopContentAdapter
import com.stone.persistent.helper.HomeIndicatorHelper
import com.stone.persistent.helper.SyncScrollHelper
import com.stone.persistent.extensions.immerseStatusBar

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. 调整状态栏
        immerseStatusBar()

        // 2. 列表滑动及下拉刷新，View状态同步
        val syncScrollHelper = SyncScrollHelper(this)
        syncScrollHelper.initLayout()
        val refreshLayout = findViewById<SwipeRefreshLayout>(R.id.main_refresh_layout)
        syncScrollHelper.syncListScroll(findViewById(R.id.main_appbar_layout), refreshLayout)

        // 3. 商品流，ViewPager绑定Adapter
        val feedsViewPager = findViewById<ViewPager2>(R.id.main_feeds_viewpager)
        feedsViewPager.adapter = FeedsPagerAdapter(this)
        val feedsIndicator = HomeIndicatorHelper(this)
        feedsIndicator.setViewPager(feedsViewPager)

        // 4. 顶部内容区，RecyclerView
        findViewById<RecyclerView>(R.id.home_top_content_rv).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = HomeTopContentAdapter(this@MainActivity)
        }

        // 6. 下拉刷新初始配置（AppBar 默认完全展开，故初始允许刷新）
        refreshLayout.apply {
            isEnabled = true
            isRefreshing = false
            setOnRefreshListener(null)
        }
    }
}
