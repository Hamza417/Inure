package app.simple.inure.decorations.views

import android.content.Context
import android.util.AttributeSet
import androidx.viewpager2.widget.ViewPager2
import app.simple.inure.adapters.installer.AdapterInstallerTabBar
import app.simple.inure.adapters.installer.AdapterInstallerTabBar.Companion.TabBarCallback
import app.simple.inure.decorations.overscroll.CustomHorizontalRecyclerView

class TabBar(context: Context, attrs: AttributeSet?) : CustomHorizontalRecyclerView(context, attrs) {

    private var adapterInstallerTabBar: AdapterInstallerTabBar? = null

    fun initWithViewPager(viewPager2: ViewPager2, titles: ArrayList<String>) {
        adapterInstallerTabBar = AdapterInstallerTabBar(titles)
        adapterInstallerTabBar?.setOnTabBarClickListener(object : TabBarCallback {
            override fun onTabClicked(position: Int) {
                viewPager2.setCurrentItem(position, true)
            }
        })
        adapter = adapterInstallerTabBar

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                adapterInstallerTabBar?.selectedPosition = position
                scrollToPosition(position)
            }
        })
    }
}