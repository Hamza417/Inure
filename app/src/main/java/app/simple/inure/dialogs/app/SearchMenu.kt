package app.simple.inure.dialogs.app

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extension.fragments.ScopedBottomSheetFragment
import app.simple.inure.popups.apps.PopupAppsCategory
import app.simple.inure.popups.search.PopupSortingStyle
import app.simple.inure.preferences.SearchPreferences
import app.simple.inure.ui.preferences.mainscreens.MainPreferencesScreen
import app.simple.inure.util.Sort

class SearchMenu : ScopedBottomSheetFragment() {

    private lateinit var appsCategory: DynamicRippleTextView
    private lateinit var sortingStyle: DynamicRippleTextView
    private lateinit var openAppsSettings: DynamicRippleTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_search_menu, container, false)

        appsCategory = view.findViewById(R.id.dialog_search_apps_category)
        sortingStyle = view.findViewById(R.id.dialog_search_apps_sorting)
        openAppsSettings = view.findViewById(R.id.dialog_open_apps_settings)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setSortingStyle()
        setListCategory()

        sortingStyle.setOnClickListener {
            PopupSortingStyle(it)
        }

        appsCategory.setOnClickListener {
            app.simple.inure.popups.search.PopupAppsCategory(it)
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
    }

    private fun setSortingStyle() {
        sortingStyle.text = when (SearchPreferences.getSortStyle()) {
            Sort.NAME -> getString(R.string.name)
            Sort.INSTALL_DATE -> getString(R.string.install_date)
            Sort.SIZE -> getString(R.string.app_size)
            Sort.PACKAGE_NAME -> getString(R.string.package_name)
            else -> getString(R.string.unknown)
        }
    }

    private fun setListCategory() {
        appsCategory.text = when (SearchPreferences.getAppsCategory()) {
            PopupAppsCategory.SYSTEM -> getString(R.string.system)
            PopupAppsCategory.USER -> getString(R.string.user)
            PopupAppsCategory.BOTH -> getString(R.string.both)
            else -> getString(R.string.unknown)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            SearchPreferences.sortStyle -> setSortingStyle()
            SearchPreferences.listAppsCategory -> setListCategory()
        }
    }

    companion object {
        fun newInstance(): SearchMenu {
            val args = Bundle()
            val fragment = SearchMenu()
            fragment.arguments = args
            return fragment
        }
    }
}