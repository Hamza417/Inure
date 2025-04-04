package app.simple.inure.dialogs.installer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.toggles.Switch
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.preferences.InstallerPreferences
import app.simple.inure.ui.preferences.subscreens.InstallerCustomization

class InstallerMenu : ScopedBottomSheetFragment() {

    private lateinit var visibility: DynamicRippleTextView
    private lateinit var openAppSettings: DynamicRippleTextView
    private lateinit var diffStyleSwitch: Switch

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_menu_installer, container, false)

        visibility = view.findViewById(R.id.visibility)
        diffStyleSwitch = view.findViewById(R.id.diff_styled_changes)
        openAppSettings = view.findViewById(R.id.dialog_open_apps_settings)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        diffStyleSwitch.isChecked = InstallerPreferences.isDiffStyleChanges()

        visibility.setOnClickListener {
            openFragmentSlide(InstallerCustomization.newInstance(), InstallerCustomization.TAG)
        }

        diffStyleSwitch.setOnSwitchCheckedChangeListener {
            InstallerPreferences.setDiffStyleChanges(it)
        }

        openAppSettings.setOnClickListener {
            openSettings()
        }
    }

    companion object {
        fun newInstance(): InstallerMenu {
            val args = Bundle()
            val fragment = InstallerMenu()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showInstallerMenu(): InstallerMenu {
            val fragment = newInstance()
            fragment.show(this, TAG)
            return fragment
        }

        const val TAG = "installer_menu"
    }
}
