package app.simple.inure.ui.preferences.mainscreens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.switchview.SwitchView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.preferences.TrackersPreferences

class TrackersScreen : ScopedFragment() {

    private lateinit var showAllClassesSwitch: SwitchView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.preferences_trackers, container, false)

        showAllClassesSwitch = view.findViewById(R.id.show_all_classes_switch)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        showAllClassesSwitch.setChecked(TrackersPreferences.isFullClassesList())

        showAllClassesSwitch.setOnSwitchCheckedChangeListener {
            TrackersPreferences.setFullClassesList(!TrackersPreferences.isFullClassesList())
        }
    }

    companion object {
        fun newInstance(): TrackersScreen {
            val args = Bundle()
            val fragment = TrackersScreen()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "TrackersScreen"
    }
}