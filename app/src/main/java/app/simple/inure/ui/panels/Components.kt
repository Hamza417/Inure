package app.simple.inure.ui.panels

import android.content.pm.PackageInfo
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.viewpager2.widget.ViewPager2
import app.simple.inure.R
import app.simple.inure.adapters.ui.AdapterComponents
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.tablayout.SmartTabLayout
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.themes.manager.ThemeManager
import app.simple.inure.util.NullSafety.isNotNull

class Components : ScopedFragment() {

    private lateinit var tabLayout: SmartTabLayout
    private lateinit var viewPager: ViewPager2

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_components, container, false)

        tabLayout = view.findViewById(R.id.tabLayout)
        viewPager = view.findViewById(R.id.viewPager)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        tabLayout.apply {
            setDefaultTabTextColor(ColorStateList.valueOf(ThemeManager.theme.textViewTheme.secondaryTextColor))
            setSelectedIndicatorColors(ThemeManager.theme.viewGroupTheme.selectedBackground)
        }

        val titles = arrayListOf(
                getString(R.string.permissions),
                getString(R.string.activities),
                getString(R.string.services),
                getString(R.string.receivers),
                getString(R.string.providers),
                getString(R.string.certificate),
        )

        val adapter = AdapterComponents(this, packageInfo, titles.toTypedArray())
        viewPager.adapter = adapter
        tabLayout.setViewPager2(viewPager)

        viewPager.post {
            viewPager.children.forEach {
                // Turn off layout transitions for all children
                if (it is ViewGroup) {
                    if (it.layoutTransition.isNotNull()) {
                        it.layoutTransition.setAnimateParentHierarchy(false)
                    }
                }
            }
        }
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo): Components {
            val args = Bundle()
            val fragment = Components()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            fragment.arguments = args
            return fragment
        }

        const val TAG = "components"
    }
}
