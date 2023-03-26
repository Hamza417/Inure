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

class HomeScreen : ScopedFragment() {

    private lateinit var menuLayout: DynamicRippleTextView
    private lateinit var layoutCustomization: DynamicRippleRelativeLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.preferences_home, container, false)

        menuLayout = view.findViewById(R.id.home_menu_popup)
        layoutCustomization = view.findViewById(R.id.home_visibility_customization)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()
        updateLayout()

        menuLayout.setOnClickListener {
            PopupMenuLayout(it)
        }

        layoutCustomization.setOnClickListener {

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
            PopupMenuLayout.VERTICAL -> menuLayout.text = getString(R.string.vertical)
            PopupMenuLayout.GRID -> menuLayout.text = getString(R.string.grid)
        }
    }

    companion object {
        fun newInstance(): HomeScreen {
            val args = Bundle()
            val fragment = HomeScreen()
            fragment.arguments = args
            return fragment
        }
    }
}