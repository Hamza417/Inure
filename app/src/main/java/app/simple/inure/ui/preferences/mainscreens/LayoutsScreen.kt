package app.simple.inure.ui.preferences.mainscreens

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleRelativeLayout
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.popups.home.PopupMenuLayout
import app.simple.inure.preferences.HomePreferences
import app.simple.inure.ui.preferences.subscreens.HomeCustomization
import app.simple.inure.ui.preferences.subscreens.InformationCustomization
import app.simple.inure.ui.preferences.subscreens.InstallerCustomization

class LayoutsScreen : ScopedFragment() {

    private lateinit var homeMenuLayout: DynamicRippleTextView
    private lateinit var homeLayoutCustomization: DynamicRippleRelativeLayout
    private lateinit var installerVisibilityCustomization: DynamicRippleRelativeLayout
    private lateinit var infoVisibilityCustomization: DynamicRippleRelativeLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.preferences_layouts, container, false)

        homeMenuLayout = view.findViewById(R.id.home_menu_popup)
        homeLayoutCustomization = view.findViewById(R.id.home_visibility_customization)
        installerVisibilityCustomization = view.findViewById(R.id.installer_visibility_customization)
        infoVisibilityCustomization = view.findViewById(R.id.info_visibility_customization)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()
        updateLayout()

        homeMenuLayout.setOnClickListener {
            PopupMenuLayout(it)
        }

        homeLayoutCustomization.setOnClickListener {
            openFragmentSlide(HomeCustomization.newInstance(), "home_customization")
        }

        installerVisibilityCustomization.setOnClickListener {
            openFragmentSlide(InstallerCustomization.newInstance(), "installer_visibility")
        }

        infoVisibilityCustomization.setOnClickListener {
            openFragmentSlide(InformationCustomization.newInstance(), "info_visibility")
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            HomePreferences.homeMenuLayout -> {
                updateLayout()
            }
        }
    }

    private fun updateLayout() {
        when (HomePreferences.getMenuLayout()) {
            PopupMenuLayout.VERTICAL -> homeMenuLayout.text = getString(R.string.vertical)
            PopupMenuLayout.GRID -> homeMenuLayout.text = getString(R.string.grid)
        }
    }

    companion object {
        fun newInstance(): LayoutsScreen {
            val args = Bundle()
            val fragment = LayoutsScreen()
            fragment.arguments = args
            return fragment
        }
    }
}