package com.stone.persistent.library

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2

/**
 * 内层的RecyclerView
 */
class PersistentRecyclerView: RecyclerView {

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet?): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        connectToParent()
    }

    private fun connectToParent() {
        var viewPager: ViewPager? = null
        var viewPager2: ViewPager2? = null
        var lastTraverseView: View = this

        var parentView = this.parent as? View
        while (parentView != null) {
            if (parentView.parent is ViewPager2) {
                // 将ChildRecyclerView设置到FrameLayout的tag中
                lastTraverseView.setTag(R.id.tag_saved_child_recycler_view, this)
            } else if (parentView is ViewPager) {
                // 将ChildRecyclerView保存到ViewPager最直接的子View中
                if (lastTraverseView != this) {
                    lastTraverseView.setTag(R.id.tag_saved_child_recycler_view, this)
                }
                viewPager = parentView
            } else if (parentView is ViewPager2) {
                viewPager2 = parentView
            } else if (parentView is PersistentCoordinatorLayout) {
                parentView.setInnerViewPager(viewPager)
                parentView.setInnerViewPager2(viewPager2)
                return
            }

            lastTraverseView = parentView
            parentView = parentView.parent as? View ?: break
        }
    }
}