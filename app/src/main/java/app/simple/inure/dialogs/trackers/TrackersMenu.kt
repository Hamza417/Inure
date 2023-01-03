package app.simple.inure.dialogs.trackers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.switchview.SwitchView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.preferences.TrackersPreferences

class TrackersMenu : ScopedBottomSheetFragment() {

    private lateinit var showAllClasses: SwitchView
    private lateinit var openSettings: DynamicRippleTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_trackers_menu, container, false)

        showAllClasses = view.findViewById(R.id.all_classes_switch)
        openSettings = view.findViewById(R.id.dialog_open_apps_settings)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showAllClasses.setChecked(TrackersPreferences.isFullClassesList())

        showAllClasses.setOnSwitchCheckedChangeListener {
            TrackersPreferences.setFullClassesList(!TrackersPreferences.isFullClassesList())
        }

        openSettings.setOnClickListener {
            openSettings()
        }
    }

    companion object {
        fun newInstance(): TrackersMenu {
            val args = Bundle()
            val fragment = TrackersMenu()
            fragment.arguments = args
            return fragment
        }
    }
}