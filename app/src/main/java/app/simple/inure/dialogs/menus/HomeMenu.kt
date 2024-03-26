package app.simple.inure.dialogs.menus

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.popups.home.PopupMenuLayout
import app.simple.inure.preferences.HomePreferences
import app.simple.inure.ui.preferences.subscreens.HomeCustomization

class HomeMenu : ScopedBottomSheetFragment() {

    private lateinit var layout: DynamicRippleTextView
    private lateinit var visibility: DynamicRippleTextView
    private lateinit var openAppSettings: DynamicRippleTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_menu_home, container, false)

        layout = view.findViewById(R.id.popup_menu_layout)
        visibility = view.findViewById(R.id.visibility)
        openAppSettings = view.findViewById(R.id.dialog_open_apps_settings)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateLayout()

        layout.setOnClickListener {
            PopupMenuLayout(layout)
        }

        visibility.setOnClickListener {
            openFragmentSlide(HomeCustomization.newInstance(), HomeCustomization.TAG)
        }

        openAppSettings.setOnClickListener {
            openSettings()
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
            PopupMenuLayout.VERTICAL -> layout.text = getString(R.string.vertical)
            PopupMenuLayout.GRID -> layout.text = getString(R.string.grid)
        }
    }

    companion object {
        fun newInstance(): HomeMenu {
            val args = Bundle()
            val fragment = HomeMenu()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showHomeMenu(): HomeMenu {
            val fragment = newInstance()
            fragment.show(this, fragment.tag)
            return fragment
        }
    }
}
