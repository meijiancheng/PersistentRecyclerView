package com.stone.persistent.library

import android.content.Context
import android.view.ViewConfiguration
import android.widget.OverScroller

/**
 * 这是注入到Behavior的scroller
 */
class HookedScroller(context: Context): OverScroller(context) {

    private val minFlingVelocity = ViewConfiguration.get(context).scaledMinimumFlingVelocity
    private var isFlingActive = false

    override fun fling(
        startX: Int,
        startY: Int,
        velocityX: Int,
        velocityY: Int,
        minX: Int,
        maxX: Int,
        minY: Int,
        maxY: Int
    ) {
        super.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY)
        isFlingActive = velocityY < -minFlingVelocity
    }

    /**
     * AppBar 完全收起时调用，取出当前剩余速度用于传导给子列表。
     * 只有 fling 触发的场景才会返回有效速度，手指拖动返回 0。
     */
    fun consumeFlingVelocity(): Int {
        if (!isFlingActive) return 0
        isFlingActive = false
        val v = currVelocity.toInt()
        return if (v > minFlingVelocity) v else 0
    }

    fun clearPendingFling() {
        isFlingActive = false
    }
}