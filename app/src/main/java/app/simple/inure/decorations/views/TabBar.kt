package app.simple.inure.decorations.views

import android.content.Context
import android.util.AttributeSet
import androidx.viewpager2.widget.ViewPager2
import app.simple.inure.adapters.installer.AdapterTabBar
import app.simple.inure.adapters.installer.AdapterTabBar.Companion.TabBarCallback
import app.simple.inure.decorations.overscroll.CustomHorizontalRecyclerView
import app.simple.inure.preferences.TrialPreferences
import app.simple.inure.util.ConditionUtils.isNotZero

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

    fun initWithViewPager(viewPager2: ViewPager2, titles: ArrayList<String>, onPositionChanged: (position: Int) -> Unit) {
        adapterTabBar = AdapterTabBar(titles)
        adapterTabBar?.setOnTabBarClickListener(object : TabBarCallback {
            override fun onTabClicked(position: Int) {
                if (TrialPreferences.isAppFullVersionEnabled()) {
                    viewPager2.setCurrentItem(position, true)
                } else {
                    if (position.isNotZero()) {
                        onPositionChanged(position)
                    }
                }
            }
        })

        adapter = adapterTabBar

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                adapterTabBar?.selectedPosition = position
                scrollToPosition(position)
                if (position.isNotZero()) {
                    onPositionChanged(position)
                }
            }
        })
    }

    fun setCurrentTab(position: Int, pager: ViewPager2) {
        adapterTabBar?.selectedPosition = position
        scrollToPosition(position)
        pager.setCurrentItem(position, true)
    }

    fun shiftToFirstTab(pager: ViewPager2) {
        setCurrentTab(0, pager)
    }
}