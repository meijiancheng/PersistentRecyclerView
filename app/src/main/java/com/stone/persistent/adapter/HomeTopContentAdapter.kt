package com.stone.persistent.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.stone.persistent.R
import com.stone.persistent.widget.CarouselView
import com.stone.persistent.widget.ViewPagerIndicator

class HomeTopContentAdapter(private val activity: FragmentActivity) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_CAROUSEL = 0
        private const val TYPE_MENU = 1
        private const val TYPE_PROMO_ROW = 2
        private const val TYPE_DIVIDER = 3
        private const val TYPE_BANNER = 4
        private const val CAROUSEL_REAL_COUNT = 5
        private const val MENU_PAGE_COUNT = 2
    }

    sealed class Item {
        object Carousel : Item()
        object Menu : Item()
        object PromoRow : Item()
        data class Divider(val heightDp: Int) : Item()
        data class Banner(@DrawableRes val resId: Int, val ratio: String) : Item()
    }

    private val items = listOf(
        Item.Carousel,
        Item.Menu,
        Item.PromoRow,
        Item.Divider(5),
        Item.Banner(R.mipmap.capture_sec_kill_image, "0.78947"),
        Item.Divider(12),
        Item.Banner(R.mipmap.capture_today_recommend, "1.641337"),
        Item.Divider(12),
        Item.Banner(R.mipmap.capture_new_year_street, "0.65934"),
    )

    override fun getItemViewType(position: Int) = when (items[position]) {
        is Item.Carousel -> TYPE_CAROUSEL
        is Item.Menu -> TYPE_MENU
        is Item.PromoRow -> TYPE_PROMO_ROW
        is Item.Divider -> TYPE_DIVIDER
        is Item.Banner -> TYPE_BANNER
    }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_CAROUSEL -> CarouselViewHolder(inflater.inflate(R.layout.item_top_carousel, parent, false))
            TYPE_MENU -> MenuViewHolder(inflater.inflate(R.layout.item_top_menu, parent, false))
            TYPE_PROMO_ROW -> PromoRowViewHolder(inflater.inflate(R.layout.item_top_promo_row, parent, false))
            TYPE_DIVIDER -> DividerViewHolder(inflater.inflate(R.layout.item_top_divider, parent, false))
            else -> BannerViewHolder(inflater.inflate(R.layout.item_top_banner, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is Item.Carousel -> (holder as CarouselViewHolder).bind()
            is Item.Menu -> (holder as MenuViewHolder).bind()
            is Item.PromoRow -> Unit
            is Item.Divider -> (holder as DividerViewHolder).bind(item)
            is Item.Banner -> (holder as BannerViewHolder).bind(item)
        }
    }

    inner class CarouselViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val carouselView: CarouselView = itemView.findViewById(R.id.home_carousel_view)
        private val indicator: ViewPagerIndicator = itemView.findViewById(R.id.home_carousel_indicator)

        fun bind() {
            val vp2 = carouselView.getViewPager2()
            if (vp2.adapter == null) {
                vp2.adapter = CarouselAdapter(itemView.context)
                indicator.setViewPager2(vp2, CAROUSEL_REAL_COUNT)
            }
        }
    }

    inner class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val viewPager: ViewPager2 = itemView.findViewById(R.id.home_menu_viewpager2)
        private val indicator: ViewPagerIndicator = itemView.findViewById(R.id.home_menu_indicator)

        fun bind() {
            if (viewPager.adapter == null) {
                viewPager.offscreenPageLimit = 2
                viewPager.adapter = MenuViewPagerAdapter(activity)
                indicator.setViewPager2(viewPager, MENU_PAGE_COUNT)
            }
        }
    }

    inner class PromoRowViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    inner class DividerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: Item.Divider) {
            val px = (item.heightDp * itemView.context.resources.displayMetrics.density).toInt()
            val lp = itemView.layoutParams
            lp.height = px
            itemView.layoutParams = lp
        }
    }

    inner class BannerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val bannerImg: ImageView = itemView.findViewById(R.id.top_banner_img)

        fun bind(item: Item.Banner) {
            bannerImg.setImageResource(item.resId)
            val lp = bannerImg.layoutParams as ConstraintLayout.LayoutParams
            lp.dimensionRatio = item.ratio
            bannerImg.layoutParams = lp
        }
    }
}
