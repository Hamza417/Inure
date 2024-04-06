package app.simple.inure.dialogs.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.toggles.Switch
import app.simple.inure.dialogs.search.SearchSort.Companion.showSearchSort
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.preferences.SearchPreferences

class SearchMenu : ScopedBottomSheetFragment() {

    private lateinit var openAppsSettings: DynamicRippleTextView
    private lateinit var ignoreCase: Switch
    private lateinit var deepSearch: Switch
    private lateinit var filter: DynamicRippleImageButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_menu_search, container, false)

        openAppsSettings = view.findViewById(R.id.dialog_open_apps_settings)
        ignoreCase = view.findViewById(R.id.ignore_case)
        deepSearch = view.findViewById(R.id.deep_search)
        filter = view.findViewById(R.id.filter)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ignoreCase.isChecked = SearchPreferences.isCasingIgnored()
        deepSearch.isChecked = SearchPreferences.isDeepSearchEnabled()

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

        filter.setOnClickListener {
            parentFragmentManager.showSearchSort()
            dismiss()
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
