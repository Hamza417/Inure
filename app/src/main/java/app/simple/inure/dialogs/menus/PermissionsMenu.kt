package app.simple.inure.dialogs.menus

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extension.fragments.ScopedBottomSheetFragment
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.popups.viewers.PopupLabelType
import app.simple.inure.preferences.PermissionPreferences
import app.simple.inure.ui.panels.Preferences
import app.simple.inure.util.FragmentHelper

class PermissionsMenu : ScopedBottomSheetFragment() {

    private lateinit var labelType: DynamicRippleTextView
    private lateinit var appSettings: DynamicRippleTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_permissions, container, false)

        labelType = view.findViewById(R.id.popup_label_type)
        appSettings = view.findViewById(R.id.dialog_open_apps_settings)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setLabelType()

        labelType.setOnClickListener {
            PopupLabelType(it)
        }

        appSettings.setOnClickListener {
            (parentFragment as ScopedFragment).clearExitTransition()
            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                        Preferences.newInstance(),
                                        "prefs")
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            PermissionPreferences.labelType -> {
                setLabelType()
            }
        }
    }

    private fun setLabelType() {
        labelType.text = if (PermissionPreferences.getLabelType()) {
            getString(R.string.id)
        } else {
            getString(R.string.descriptive)
        }
    }

    companion object {
        fun newInstance(): PermissionsMenu {
            val args = Bundle()
            val fragment = PermissionsMenu()
            fragment.arguments = args
            return fragment
        }
    }
}