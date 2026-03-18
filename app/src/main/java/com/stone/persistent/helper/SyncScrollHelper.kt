package com.stone.persistent.helper

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.appbar.AppBarLayout
import com.stone.persistent.R
import com.stone.persistent.MainActivity
import com.stone.persistent.extensions.dp2px
import com.stone.persistent.extensions.getScreenWidth
import com.stone.persistent.extensions.getStatusBarHeight

/**
 * 首页滑动帮助类
 */
class SyncScrollHelper(mainActivity: MainActivity) {

    private val statusBarHeight = mainActivity.getStatusBarHeight()
    private val toolbarHeight = mainActivity.dp2px(50f)
    private var screenWidth = mainActivity.getScreenWidth()
    private var searchBarHeight = mainActivity.dp2px(46f)

    private val activity = mainActivity
    private val toolBarLayout = mainActivity.findViewById<ConstraintLayout>(R.id.main_toolbar)
    private val searchBarLayout = mainActivity.findViewById<ConstraintLayout>(R.id.main_search_layout)
    private val backIv1 = mainActivity.findViewById<android.widget.ImageView>(R.id.main_back_img1)
    private val backIv2 = mainActivity.findViewById<android.widget.ImageView>(R.id.main_back_img2)
    private val logoImageView = mainActivity.findViewById<android.widget.ImageView>(R.id.main_top_logo)

    companion object {
        private const val BACK_DIMENSION_RATIO2 = 0.992647f
        private const val BACK_DIMENSION_RATIO1 = 1.8125f
    }

    fun initLayout() {
        val toolbarParams = toolBarLayout.layoutParams as ConstraintLayout.LayoutParams
        toolbarParams.setMargins(0, statusBarHeight, 0, 0)
        toolBarLayout.layoutParams = toolbarParams

        val backImgHeight1 = screenWidth / BACK_DIMENSION_RATIO1
        val translationY1 = backImgHeight1 - statusBarHeight - toolbarHeight - searchBarHeight
        backIv1.translationY = -translationY1

        val backImgHeight2 = screenWidth / Companion.BACK_DIMENSION_RATIO2
        val translationY2 = backImgHeight2 - backImgHeight1
        backIv2.translationY = -translationY2

        searchBarLayout.translationY = statusBarHeight + toolbarHeight
    }

    /**
     * 列表滚动时，一些View位置变动；同时同步控制下拉刷新的可用状态
     */
    fun syncListScroll(appBarLayout: AppBarLayout, refreshLayout: SwipeRefreshLayout) {
        var appBarFullyExpanded = true

        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, offset ->
            appBarFullyExpanded = offset == 0

            val minTranslationY = statusBarHeight + activity.dp2px(9f)
            val maxTranslationY = statusBarHeight + toolbarHeight
            val targetTranslationY = maxTranslationY + offset / 2

            // 1. logo的alpha处理
            var alpha = 1 + offset / 2 / (maxTranslationY - minTranslationY)
            if (alpha < 0) {
                alpha = 0f
            }
            logoImageView.alpha = alpha

            // 2. 搜索框位移调整
            searchBarLayout.translationY = if (targetTranslationY < minTranslationY) {
                minTranslationY
            } else {
                targetTranslationY
            }

            // 3. 搜索框大小调整
            val maxMarginRight = activity.dp2px(92f)
            var progress = (1 - alpha) * 2f
            if (progress > 1) {
                progress = 1.0f
            }
            val layoutParams = searchBarLayout.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.setMargins(0, 0, (maxMarginRight * progress).toInt(),0)
            searchBarLayout.layoutParams = layoutParams
        })

        // 4. AppBar 未完全展开时，视为子视图仍可上滑，阻止触发下拉刷新
        //    loading 中 isRefreshing=true，SwipeRefreshLayout 不依赖此回调，indicator 正常显示
        refreshLayout.setOnChildScrollUpCallback { _, _ -> !appBarFullyExpanded }
    }
}