package app.simple.inure.dialogs.app

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.popup.PopupLinearLayout
import app.simple.inure.decorations.popup.PopupMenuCallback
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extension.fragments.ScopedBottomSheetFragment
import app.simple.inure.popups.dialogs.AppCategoryPopup
import app.simple.inure.popups.dialogs.SortingStylePopup
import app.simple.inure.preferences.MainPreferences
import app.simple.inure.ui.preferences.mainscreens.MainPreferencesScreen
import app.simple.inure.util.Sort

class AppsListConfiguration : ScopedBottomSheetFragment() {

    private lateinit var appsCategory: DynamicRippleTextView
    private lateinit var sortingStyle: DynamicRippleTextView
    private lateinit var openAppsSettings: DynamicRippleTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_main_settings, container, false)

        appsCategory = view.findViewById(R.id.dialog_apps_category)
        sortingStyle = view.findViewById(R.id.dialog_apps_sorting)
        openAppsSettings = view.findViewById(R.id.dialog_open_apps_settings)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        setSortingStyle()
        setListCategory()

        sortingStyle.setOnClickListener {
            val popup = SortingStylePopup(
                layoutInflater.inflate(R.layout.popup_sorting_style, PopupLinearLayout(requireContext()), true),
                sortingStyle)

            popup.setOnMenuItemClickListener(object : PopupMenuCallback {
                override fun onMenuItemClicked(source: String) {
                    MainPreferences.setSortStyle(source)
                }

                override fun onDismiss() {

                }
            })
        }

        appsCategory.setOnClickListener {
            val popup = AppCategoryPopup(
                layoutInflater.inflate(R.layout.popup_apps_category, PopupLinearLayout(requireContext()), true),
                appsCategory)

            popup.setOnMenuItemClickListener(object : PopupMenuCallback {
                override fun onMenuItemClicked(source: String) {
                    MainPreferences.setListAppCategory(source)
                }

                override fun onDismiss() {

                }
            })
        }

        openAppsSettings.setOnClickListener {
            val fragment = requireActivity().supportFragmentManager.findFragmentByTag("main_preferences_screen")
                ?: MainPreferencesScreen.newInstance()

            requireActivity().supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.dialog_in, R.anim.dialog_out)
                    .replace(R.id.app_container, fragment, "main_preferences_screen")
                    .addToBackStack(tag)
                    .commit()
        }

        super.onViewCreated(view, savedInstanceState)
    }

    private fun setSortingStyle() {
        sortingStyle.text = when (MainPreferences.getSortStyle()) {
            Sort.NAME -> getString(R.string.name)
            Sort.INSTALL_DATE -> getString(R.string.install_date)
            Sort.SIZE -> getString(R.string.app_size)
            Sort.PACKAGE_NAME -> getString(R.string.package_name)
            else -> getString(R.string.unknown)
        }
    }

    private fun setListCategory() {
        appsCategory.text = when (MainPreferences.getListAppCategory()) {
            AppCategoryPopup.SYSTEM -> getString(R.string.system)
            AppCategoryPopup.USER -> getString(R.string.user)
            AppCategoryPopup.BOTH -> getString(R.string.both)
            else -> getString(R.string.unknown)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            MainPreferences.sortStyle -> setSortingStyle()
            MainPreferences.listAppsCategory -> setListCategory()
        }
    }

    companion object {
        fun newInstance(): AppsListConfiguration {
            val args = Bundle()
            val fragment = AppsListConfiguration()
            fragment.arguments = args
            return fragment
        }
    }
}
