package app.simple.inure.dialogs

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import app.simple.inure.R
import app.simple.inure.activities.PreferenceActivity
import app.simple.inure.decorations.animatedbackground.AnimatedBackgroundTextView
import app.simple.inure.decorations.corners.DynamicCornerLinearLayout
import app.simple.inure.decorations.fragments.ScopedBottomSheetFragment
import app.simple.inure.decorations.popup.PopupMenuCallback
import app.simple.inure.decorations.transitions.DetailsTransitionArc
import app.simple.inure.decorations.transitions.TransitionManager
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.popups.dialogs.AppCategoryPopup
import app.simple.inure.popups.dialogs.SortingStylePopup
import app.simple.inure.preferences.MainPreferences
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences
import app.simple.inure.ui.preferences.MainPreferencesScreen
import app.simple.inure.util.Sort

class AppsListConfiguration : ScopedBottomSheetFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var appsCategory: TypeFaceTextView
    private lateinit var sortingStyle: TypeFaceTextView
    private lateinit var openAppsSettings: AnimatedBackgroundTextView

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
                layoutInflater.inflate(R.layout.popup_sorting_style, DynamicCornerLinearLayout(requireContext(), null), true),
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
                layoutInflater.inflate(R.layout.popup_apps_category, DynamicCornerLinearLayout(requireContext(), null), true),
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
            val fragment = requireActivity().supportFragmentManager.findFragmentByTag("main_preferences_screen") ?: MainPreferencesScreen.newInstance()

            requireActivity().supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.dialog_in, R.anim.dialog_out)
                    .replace(R.id.app_container, fragment, "main_preferences_screen")
                    .addToBackStack(tag)
                    .commit()
        }

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        getSharedPreferences().registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this)
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

    companion object {
        fun newInstance(): AppsListConfiguration {
            val args = Bundle()
            val fragment = AppsListConfiguration()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            MainPreferences.sortStyle -> setSortingStyle()
            MainPreferences.listAppsCategory -> setListCategory()
        }
    }
}
