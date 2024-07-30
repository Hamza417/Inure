package app.simple.inure.dialogs.search

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.transition.TransitionManager
import app.simple.inure.R
import app.simple.inure.decorations.corners.DynamicCornerLinearLayout
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.theme.ThemeDivider
import app.simple.inure.decorations.toggles.Switch
import app.simple.inure.decorations.views.Chip
import app.simple.inure.dialogs.search.SearchSort.Companion.showSearchSort
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.preferences.SearchPreferences
import app.simple.inure.util.ViewUtils.visibility

class SearchMenu : ScopedBottomSheetFragment() {

    private lateinit var dialogContainer: DynamicCornerLinearLayout
    private lateinit var openAppsSettings: DynamicRippleTextView
    private lateinit var ignoreCase: Switch
    private lateinit var deepSearch: Switch
    private lateinit var keywordDatabaseContainer: ConstraintLayout
    private lateinit var keywordDatabaseDivider: ThemeDivider
    private lateinit var permissionsChip: Chip
    private lateinit var trackersChip: Chip
    private lateinit var filter: DynamicRippleImageButton

    private lateinit var searchMenuCallback: SearchMenuCallback

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_menu_search, container, false)

        dialogContainer = view.findViewById(R.id.dialog_container)
        openAppsSettings = view.findViewById(R.id.dialog_open_apps_settings)
        ignoreCase = view.findViewById(R.id.ignore_case)
        deepSearch = view.findViewById(R.id.deep_search)
        keywordDatabaseContainer = view.findViewById(R.id.keyword_database_container)
        keywordDatabaseDivider = view.findViewById(R.id.themeDivider3)
        permissionsChip = view.findViewById(R.id.permissions)
        trackersChip = view.findViewById(R.id.trackers)
        filter = view.findViewById(R.id.filter)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // dialogContainer.applyBottomSheetContainerAnimationFix()
        ignoreCase.isChecked = SearchPreferences.isCasingIgnored()
        deepSearch.isChecked = SearchPreferences.isDeepSearchEnabled()
        setKeywordDatabaseContainerVisibility()
        permissionsChip.isCheckable = false
        trackersChip.isCheckable = false

        openAppsSettings.setOnClickListener {
            openSettings()
        }

        ignoreCase.setOnSwitchCheckedChangeListener {
            SearchPreferences.setIgnoreCasing(it)
        }

        deepSearch.setOnSwitchCheckedChangeListener {
            if (it) {
                if (fullVersionCheck(goBack = false)) {
                    SearchPreferences.setDeepSearch(true)
                }
            } else {
                SearchPreferences.setDeepSearch(false)
            }
        }

        permissionsChip.setOnClickListener {
            searchMenuCallback.onPermission().also {
                dismiss()
            }
        }

        trackersChip.setOnClickListener {
            searchMenuCallback.onTrackers().also {
                dismiss()
            }
        }

        filter.setOnClickListener {
            parentFragmentManager.showSearchSort()
            dismiss()
        }
    }

    fun setSearchMenuCallback(searchMenuCallback: SearchMenuCallback) {
        this.searchMenuCallback = searchMenuCallback
    }

    private fun setKeywordDatabaseContainerVisibility() {
        if (SearchPreferences.isDeepSearchEnabled()) {
            keywordDatabaseDivider.animate()
                .scaleY(1f)
                .setStartDelay(400)
                .setDuration(250)
                .setInterpolator(LinearOutSlowInInterpolator())
                .start()
        } else {
            keywordDatabaseDivider.animate()
                .scaleY(0f)
                .setDuration(250)
                .setInterpolator(AccelerateInterpolator())
                .start()
        }

        // Prevents jumping issue on BottomSheetDialogFragment
        // Usually happens when the visibility of a view is changed
        TransitionManager.beginDelayedTransition(requireView().parent as ViewGroup)
        keywordDatabaseContainer.visibility(SearchPreferences.isDeepSearchEnabled())
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)

        when (key) {
            SearchPreferences.DEEP_SEARCH -> {
                setKeywordDatabaseContainerVisibility()
            }
        }
    }

    companion object {
        fun newInstance(): SearchMenu {
            val args = Bundle()
            val fragment = SearchMenu()
            fragment.arguments = args
            return fragment
        }

        fun Fragment.showSearchMenu(): SearchMenu {
            val dialog = newInstance()
            dialog.show(parentFragmentManager, TAG)
            return dialog
        }

        interface SearchMenuCallback {
            fun onPermission()
            fun onTrackers()
        }

        const val TAG = "SearchMenu"
    }
}
