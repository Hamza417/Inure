package app.simple.inure.decorations.views

import android.content.Context
import android.util.AttributeSet
import androidx.viewpager2.widget.ViewPager2
import app.simple.inure.adapters.installer.AdapterTabBar
import app.simple.inure.adapters.installer.AdapterTabBar.Companion.TabBarCallback
import app.simple.inure.decorations.overscroll.CustomHorizontalRecyclerView

class TabBar(context: Context, attrs: AttributeSet?) : CustomHorizontalRecyclerView(context, attrs) {

    private var adapterTabBar: AdapterTabBar? = null

    fun initWithViewPager(viewPager2: ViewPager2, titles: ArrayList<String>) {
        adapterTabBar = AdapterTabBar(titles)
        adapterTabBar?.setOnTabBarClickListener(object : TabBarCallback {
            override fun onTabClicked(position: Int) {
                viewPager2.setCurrentItem(position, true)
            }
        })

        adapter = adapterTabBar

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                adapterTabBar?.selectedPosition = position
                scrollToPosition(position)
            }
        })
    }
}