package com.stone.persistent.library

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ViewTreeObserver
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.AppBarLayout

/**
 * 持续滚动 CoordinatorLayout
 * ---appbarLayout
 * ---viewpager/viewpager2
 * -------fragment
 * -----------PersistentRecyclerView
 */
private const val TAG = "PersistentCoordinator"

class PersistentCoordinatorLayout: CoordinatorLayout {

    private lateinit var appBarLayout: AppBarLayout

    private var innerViewPager: ViewPager? = null
    private var innerViewPager2: ViewPager2? = null

    private var overScroller: HookedScroller? = null

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet?): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    override fun onFinishInflate() {
        super.onFinishInflate()
        val firstChild = getChildAt(0)
        if (childCount != 2 || firstChild !is AppBarLayout) {
            throw RuntimeException("PersistentCoordinatorLayout's first child must be AppbarLayout")
        }

        appBarLayout = firstChild
        val observer = appBarLayout.viewTreeObserver
        observer.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                hookScroller()
                if (observer.isAlive) {
                    observer.removeOnGlobalLayoutListener(this)
                }
            }
        })
    }

    /**
     * 反射behavior塞入hookScroller
     */
    private fun hookScroller() {
        val lp = appBarLayout.layoutParams as LayoutParams
        val behavior = lp.behavior as? AppBarLayout.Behavior ?: return
        behavior.setDragCallback(PersistentCallback())

        try {
            val grandParentClass = AppBarLayout.Behavior::class.java.superclass?.superclass
                ?: return
            val scrollerField = grandParentClass.getDeclaredField("scroller")
            scrollerField.isAccessible = true
            if (scrollerField.get(behavior) != null) return

            val hookedScroller = HookedScroller(context)
            scrollerField.set(behavior, hookedScroller)
            overScroller = hookedScroller
            registerOffsetListener(hookedScroller)
        } catch (e: Exception) {
            // 隐藏API访问失败时不崩溃，保持默认行为
            Log.w(TAG, "hookScroller: reflection failed, velocity transfer disabled", e)
        }
    }

    private fun registerOffsetListener(hookedScroller: HookedScroller) {
        appBarLayout.addOnOffsetChangedListener { appBar, verticalOffset ->
            if (verticalOffset == -appBar.totalScrollRange) {
                val velocity = hookedScroller.consumeFlingVelocity()
                if (velocity > 0) {
                    findCurrentChildRecyclerView()?.fling(0, velocity)
                }
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        // 手指按下时，所有scroll动画都需要停止
        if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
            // behavior滑动停止
            overScroller?.clearPendingFling()
            overScroller?.forceFinished(true)

            // 底部的RecyclerView滑动停止
            if (ev.y < appBarLayout.bottom) {
                val currentRecyclerView = findCurrentChildRecyclerView()
                currentRecyclerView?.stopScroll()
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    /**
     * 获取当前的ChildRecyclerView
     */
    private fun findCurrentChildRecyclerView(): PersistentRecyclerView? =
        innerViewPager?.let(::findCurrentChildInViewPager)
            ?: innerViewPager2?.let(::findCurrentChildInViewPager2)

    private fun findCurrentChildInViewPager(viewPager: ViewPager): PersistentRecyclerView? {
        for (i in 0 until viewPager.childCount) {
            val itemChildView = viewPager.getChildAt(i)
            val layoutParams = itemChildView.layoutParams as ViewPager.LayoutParams
            if (!layoutParams.isDecor) {
                val recyclerView = findPersistentRecyclerView(itemChildView)
                if (recyclerView != null) {
                    return recyclerView
                }
            }
        }
        return null
    }

    private fun findCurrentChildInViewPager2(viewPager2: ViewPager2): PersistentRecyclerView? {
        val rv = viewPager2.getChildAt(0) as? RecyclerView ?: return null
        val pageView = rv.findViewHolderForAdapterPosition(viewPager2.currentItem)?.itemView
            ?: return null
        return findPersistentRecyclerView(pageView)
    }

    private fun findPersistentRecyclerView(view: android.view.View): PersistentRecyclerView? {
        if (view is PersistentRecyclerView) {
            return view
        }
        return view.getTag(R.id.tag_saved_child_recycler_view) as? PersistentRecyclerView
    }

    fun setInnerViewPager(viewPager: ViewPager?) {
        this.innerViewPager = viewPager
    }

    fun setInnerViewPager2(viewPager2: ViewPager2?) {
        this.innerViewPager2 = viewPager2
    }

    inner class PersistentCallback: AppBarLayout.Behavior.DragCallback() {
        override fun canDrag(appBarLayout: AppBarLayout) = true
    }
}