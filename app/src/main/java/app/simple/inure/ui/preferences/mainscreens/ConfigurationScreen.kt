package app.simple.inure.ui.preferences.mainscreens

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import app.simple.inure.R
import app.simple.inure.constants.Warnings
import app.simple.inure.decorations.ripple.DynamicRippleConstraintLayout
import app.simple.inure.decorations.ripple.DynamicRippleRelativeLayout
import app.simple.inure.decorations.toggles.Switch
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.dialogs.configuration.AppPath.Companion.showAppPathDialog
import app.simple.inure.dialogs.miscellaneous.StoragePermission
import app.simple.inure.dialogs.miscellaneous.StoragePermission.Companion.showStoragePermissionDialog
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.root.RootStateHelper.setRootState
import app.simple.inure.ui.preferences.subscreens.ComponentManager
import app.simple.inure.ui.preferences.subscreens.Language
import app.simple.inure.ui.preferences.subscreens.Shortcuts
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.PermissionUtils.checkStoragePermission
import app.simple.inure.util.StringUtils.appendFlag
import app.simple.inure.util.ViewUtils.gone
import rikka.shizuku.Shizuku

class ConfigurationScreen : ScopedFragment() {

    private lateinit var keepScreenOnSwitchView: Switch
    private lateinit var shortcuts: DynamicRippleRelativeLayout
    private lateinit var components: DynamicRippleRelativeLayout
    private lateinit var language: DynamicRippleRelativeLayout
    private lateinit var path: DynamicRippleConstraintLayout
    private lateinit var showUsersSwitch: Switch
    private lateinit var rootSwitchView: Switch
    private lateinit var shizukuSwitchView: Switch
    private lateinit var shizukuPermissionState: TypeFaceTextView

    private val requestCode = 100
    private var isBinderReceived = false

    private val requestPermissionResultListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        onRequestPermissionsResult(requestCode, grantResult)
    }

    private val onBinderReceivedListener = Shizuku.OnBinderReceivedListener {
        Log.d(TAG, "Shizuku binder received")
        isBinderReceived = true
        setShizukuPermissionState()
    }

    private val onBinderDeadListener = Shizuku.OnBinderDeadListener {
        Log.d(TAG, "Shizuku binder dead")
        isBinderReceived = false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.preferences_configuration, container, false)

        keepScreenOnSwitchView = view.findViewById(R.id.configuration_switch_keep_screen_on)
        shortcuts = view.findViewById(R.id.configuration_shortcuts)
        components = view.findViewById(R.id.configuration_component_manager)
        language = view.findViewById(R.id.configuration_language)
        path = view.findViewById(R.id.configuration_path)
        showUsersSwitch = view.findViewById(R.id.configuration_show_user_list_switch)
        rootSwitchView = view.findViewById(R.id.configuration_root_switch_view)
        shizukuSwitchView = view.findViewById(R.id.configuration_shizuku_switch_view)
        shizukuPermissionState = view.findViewById(R.id.shizuku_permission_state)

        runCatching {
            Shizuku.addRequestPermissionResultListener(requestPermissionResultListener)
            Shizuku.addBinderReceivedListenerSticky(onBinderReceivedListener)
            Shizuku.addBinderDeadListener(onBinderDeadListener)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        keepScreenOnSwitchView.isChecked = ConfigurationPreferences.isKeepScreenOn()
        showUsersSwitch.isChecked = ConfigurationPreferences.isShowUsersList()
        rootSwitchView.isChecked = ConfigurationPreferences.isUsingRoot()
        shizukuSwitchView.isChecked = ConfigurationPreferences.isUsingShizuku()

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

        showUsersSwitch.setOnSwitchCheckedChangeListener {
            ConfigurationPreferences.setShowUsersList(it)
        }

        shizukuSwitchView.setOnSwitchCheckedChangeListener {
            if (isBinderReceived) {
                if (it) {
                    if (checkPermission()) {
                        ConfigurationPreferences.setUsingShizuku(true)
                    }
                } else {
                    ConfigurationPreferences.setUsingShizuku(false)
                }

                setShizukuPermissionState()
            } else {
                shizukuSwitchView.uncheck(true)
                showWarning(Warnings.SHIZUKU_BINDER_NOT_READY, false)
            }
        }
    }

    private fun isShizukuPermissionGranted(): Boolean {
        return if (Shizuku.isPreV11() || Shizuku.getVersion() < 11) {
            false
        } else {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        runCatching {
            Shizuku.removeRequestPermissionResultListener(requestPermissionResultListener)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun checkPermission(): Boolean {
        if (Shizuku.isPreV11()) {
            // Pre-v11 is unsupported
            shizukuPermissionState.text = "Pre-v11 is unsupported"
            return false
        }

        return when {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED -> {
                // Granted
                shizukuSwitchView.check(true)
                Log.d(TAG, "checkPermission: granted")
                true
            }
            Shizuku.shouldShowRequestPermissionRationale() -> {
                // Users choose "Deny and don't ask again"
                shizukuSwitchView.uncheck(false)
                shizukuSwitchView.gone()
                Log.d(TAG, "checkPermission: shouldShowRequestPermissionRationale")
                shizukuPermissionState.text = Warnings.SHIZUKU_PERMISSION_DENIED
                false
            }
            else -> {
                // Request the permission
                Shizuku.requestPermission(requestCode)
                Log.d("ConfigurationScreen", "checkPermission: requestPermission")
                false
            }
        }
    }

    private fun setShizukuPermissionState() {
        handler.post {
            try {
                shizukuPermissionState.text = buildString {
                    if (Shizuku.isPreV11().invert()) {
                        if (Shizuku.shouldShowRequestPermissionRationale()) {
                            appendFlag(Warnings.SHIZUKU_PERMISSION_DENIED)
                            shizukuSwitchView.gone(animate = false)
                        } else {
                            if (isShizukuPermissionGranted()) {
                                appendFlag(getString(R.string.granted))
                            } else {
                                appendFlag(getString(R.string.rejected))
                            }

                            if (ConfigurationPreferences.isUsingShizuku()) {
                                appendFlag(getString(R.string.enabled))
                            } else {
                                appendFlag(getString(R.string.disabled))
                            }
                        }
                    } else {
                        appendFlag("Pre-v11 is unsupported")
                    }
                }
            } catch (e: IllegalStateException) {
                // Since the fragment is destroyed, the view is not available
                // So, we catch the exception and ignore it
                Log.e(TAG, "setShizukuPermissionState: $e")
            }
        }
    }

    private fun onRequestPermissionsResult(requestCode: Int, grantResult: Int) {
        val granted: Boolean = grantResult == PackageManager.PERMISSION_GRANTED
        Log.d(TAG, "onRequestPermissionsResult: $granted with requestCode: $requestCode")
        ConfigurationPreferences.setUsingShizuku(granted)
        setShizukuPermissionState()

        if (granted) {
            shizukuSwitchView.check(true)
        } else {
            shizukuSwitchView.uncheck(true)
            if (Shizuku.shouldShowRequestPermissionRationale()) {
                shizukuPermissionState.text = Warnings.SHIZUKU_PERMISSION_DENIED
                shizukuSwitchView.gone(animate = false)
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
