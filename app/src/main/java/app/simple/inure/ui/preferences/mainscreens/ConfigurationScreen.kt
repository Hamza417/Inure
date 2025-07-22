package app.simple.inure.ui.preferences.mainscreens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleConstraintLayout
import app.simple.inure.decorations.ripple.DynamicRippleRelativeLayout
import app.simple.inure.decorations.toggles.Switch
import app.simple.inure.dialogs.configuration.AppPath.Companion.showAppPathDialog
import app.simple.inure.dialogs.configuration.VirusTotalAPI.Companion.showVirusTotalAPI
import app.simple.inure.dialogs.miscellaneous.StoragePermission
import app.simple.inure.dialogs.miscellaneous.StoragePermission.Companion.showStoragePermissionDialog
import app.simple.inure.extensions.fragments.ShizukuStateFragment
import app.simple.inure.helpers.RootStateHelper.setRootState
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.ui.preferences.subscreens.ComponentManager
import app.simple.inure.ui.preferences.subscreens.Language
import app.simple.inure.ui.preferences.subscreens.Shortcuts
import app.simple.inure.util.AppUtils
import app.simple.inure.util.PermissionUtils.checkStoragePermission

class ConfigurationScreen : ShizukuStateFragment() {

    private lateinit var keepScreenOnSwitchView: Switch
    private lateinit var shortcuts: DynamicRippleRelativeLayout
    private lateinit var components: DynamicRippleRelativeLayout
    private lateinit var language: DynamicRippleRelativeLayout
    private lateinit var path: DynamicRippleConstraintLayout
    private lateinit var virustotalAPI: DynamicRippleConstraintLayout
    private lateinit var rootSwitchView: Switch

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.preferences_configuration, container, false)

        keepScreenOnSwitchView = view.findViewById(R.id.configuration_switch_keep_screen_on)
        shortcuts = view.findViewById(R.id.configuration_shortcuts)
        components = view.findViewById(R.id.configuration_component_manager)
        language = view.findViewById(R.id.configuration_language)
        path = view.findViewById(R.id.configuration_path)
        virustotalAPI = view.findViewById(R.id.vt_api)
        rootSwitchView = view.findViewById(R.id.configuration_root_switch_view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        keepScreenOnSwitchView.isChecked = ConfigurationPreferences.isKeepScreenOn()
        rootSwitchView.isChecked = ConfigurationPreferences.isUsingRoot()

        if (AppUtils.isPlayFlavor()) {
            virustotalAPI.visibility = View.GONE
        } else {
            virustotalAPI.setOnClickListener {
                childFragmentManager.showVirusTotalAPI()
            }
        }

        rootSwitchView.setRootState(viewLifecycleOwner)

        keepScreenOnSwitchView.setOnSwitchCheckedChangeListener { isChecked ->
            ConfigurationPreferences.setKeepScreenOn(isChecked)

            if (isChecked) {
                requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }

        shortcuts.setOnClickListener {
            openFragmentSlide(Shortcuts.newInstance(), Shortcuts.TAG)
        }

        components.setOnClickListener {
            openFragmentSlide(ComponentManager.newInstance(), ComponentManager.TAG)
        }

        language.setOnClickListener {
            openFragmentSlide(Language.newInstance(), Language.TAG)
        }

        path.setOnClickListener {
            if (requireContext().checkStoragePermission()) {
                childFragmentManager.showAppPathDialog()
            } else {
                childFragmentManager.showStoragePermissionDialog().setStoragePermissionCallbacks(object : StoragePermission.Companion.StoragePermissionCallbacks {
                    override fun onStoragePermissionGranted() {
                        childFragmentManager.showAppPathDialog()
                    }
                })
            }
        }
    }

    companion object {
        fun newInstance(): ConfigurationScreen {
            val args = Bundle()
            val fragment = ConfigurationScreen()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "ConfigurationScreen"
    }
}
