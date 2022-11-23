package app.simple.inure.dialogs.appinfo

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.popups.appinfo.PopupMenuLayout
import app.simple.inure.preferences.AppInformationPreferences

class AppInfoMenu : ScopedBottomSheetFragment() {

    private lateinit var menuLayout: DynamicRippleTextView
    private lateinit var openSettings: DynamicRippleTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_menu_app_info, container, false)

        menuLayout = view.findViewById(R.id.popup_menu_layout)
        openSettings = view.findViewById(R.id.dialog_open_apps_settings)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setMenuLayout()

        menuLayout.setOnClickListener {
            PopupMenuLayout(it)
        }

        openSettings.setOnClickListener {
            openSettings()
        }
    }

    private fun setMenuLayout() {
        when (AppInformationPreferences.getMenuLayout()) {
            PopupMenuLayout.HORIZONTAL -> {
                menuLayout.setText(R.string.horizontal)
            }
            PopupMenuLayout.GRID -> {
                menuLayout.setText(R.string.grid)
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            AppInformationPreferences.menuLayout -> {
                setMenuLayout()
            }
        }
    }

    companion object {
        fun newInstance(): AppInfoMenu {
            val args = Bundle()
            val fragment = AppInfoMenu()
            fragment.arguments = args
            return fragment
        }
    }
}