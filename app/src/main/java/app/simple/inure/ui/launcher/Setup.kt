package app.simple.inure.ui.launcher

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import app.simple.inure.BuildConfig
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.constants.Warnings
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayout
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.toggles.CheckBox
import app.simple.inure.decorations.toggles.Switch
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.preferences.SetupPreferences
import app.simple.inure.ui.preferences.subscreens.AccentColor
import app.simple.inure.ui.preferences.subscreens.AppearanceTypeFace
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.PermissionUtils.checkForUsageAccessPermission
import app.simple.inure.util.StringUtils.appendFlag
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.invisible
import app.simple.inure.util.ViewUtils.visible
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuProvider

class Setup : ScopedFragment() {

    private lateinit var usageAccess: DynamicRippleLinearLayout
    private lateinit var storageAccess: DynamicRippleLinearLayout
    private lateinit var typeface: DynamicRippleLinearLayout
    private lateinit var accent: DynamicRippleLinearLayout
    private lateinit var usageStatus: TypeFaceTextView
    private lateinit var storageStatus: TypeFaceTextView
    private lateinit var storageUri: TypeFaceTextView
    private lateinit var rootSwitchView: Switch
    private lateinit var shizukuSwitchView: Switch
    private lateinit var shizukuPermissionState: TypeFaceTextView
    private lateinit var startApp: DynamicRippleTextView
    private lateinit var skip: DynamicRippleTextView
    private lateinit var dontShowAgainCheckBox: CheckBox
    private lateinit var dontShowAgain: DynamicRippleTextView

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

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
        val view = inflater.inflate(R.layout.fragment_setup, container, false)

        usageAccess = view.findViewById(R.id.grant_usage_access)
        storageAccess = view.findViewById(R.id.grant_storage_access)
        typeface = view.findViewById(R.id.setup_typeface)
        accent = view.findViewById(R.id.setup_accent_color)
        usageStatus = view.findViewById(R.id.status_usage_access)
        storageStatus = view.findViewById(R.id.status_storage_access)
        storageUri = view.findViewById(R.id.status_storage_uri)
        rootSwitchView = view.findViewById(R.id.configuration_root_switch_view)
        shizukuSwitchView = view.findViewById(R.id.setup_shizuku_switch_view)
        shizukuPermissionState = view.findViewById(R.id.shizuku_permission_state)
        startApp = view.findViewById(R.id.start_app_now)
        skip = view.findViewById(R.id.skip_setup)
        dontShowAgainCheckBox = view.findViewById(R.id.show_again_checkbox)
        dontShowAgain = view.findViewById(R.id.dont_show_again)

        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.forEach {
                when (it.key) {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                        if (it.value) {
                            setStorageStatus()
                            showStartAppButton()
                        }
                    }

                    // Unused
                    ShizukuProvider.PERMISSION -> {
                        if (it.value) {
                            ConfigurationPreferences.setUsingShizuku(true)
                            shizukuSwitchView.isChecked = true
                        } else {
                            ConfigurationPreferences.setUsingShizuku(false)
                            shizukuSwitchView.isChecked = false
                        }
                    }
                }
            }
        }

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

        rootSwitchView.isChecked = ConfigurationPreferences.isUsingRoot()
        dontShowAgainCheckBox.isChecked = SetupPreferences.isDontShowAgain()
        shizukuSwitchView.isChecked = ConfigurationPreferences.isUsingShizuku()

        usageAccess.setOnClickListener {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            intent.data = Uri.fromParts("package", requireContext().packageName, null)
            kotlin.runCatching {
                startActivity(intent)
            }.onFailure {
                intent.data = null
                startActivity(intent)
            }
        }

        storageAccess.setOnClickListener {
            openDirectory()
        }

        startApp.setOnClickListener {
            if (requireArguments().getBoolean(BundleConstants.goBack)) {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            } else {
                if (requireContext().checkForUsageAccessPermission()) {
                    openFragmentSlide(SplashScreen.newInstance(false))
                } else {
                    Toast.makeText(requireContext(), R.string.ss_please_grant_storage_permission, Toast.LENGTH_SHORT).show()
                }
            }
        }

        skip.setOnClickListener {
            openFragmentSlide(SplashScreen.newInstance(true))
        }

        accent.setOnClickListener {
            openFragmentSlide(AccentColor.newInstance(), "accent_color")
        }

        typeface.setOnClickListener {
            openFragmentSlide(AppearanceTypeFace.newInstance(), "app_typeface")
        }

        rootSwitchView.setOnSwitchCheckedChangeListener { it ->
            if (it) {
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    kotlin.runCatching {
                        Shell.enableVerboseLogging = BuildConfig.DEBUG
                        Shell.setDefaultBuilder(
                                Shell.Builder
                                    .create()
                                    .setFlags(Shell.FLAG_REDIRECT_STDERR or Shell.FLAG_MOUNT_MASTER)
                                    .setTimeout(10))
                    }.getOrElse {
                        it.printStackTrace()
                    }

                    Shell.getShell() // Request root access

                    if (Shell.isAppGrantedRoot() == true) {
                        withContext(Dispatchers.Main) {
                            ConfigurationPreferences.setUsingRoot(true)
                            rootSwitchView.isChecked = true
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            ConfigurationPreferences.setUsingRoot(false)
                            rootSwitchView.isChecked = false
                        }
                    }
                }
            } else {
                ConfigurationPreferences.setUsingRoot(false)
                rootSwitchView.isChecked = false
            }
        }

        shizukuSwitchView.setOnSwitchCheckedChangeListener { it ->
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
                showWarning(Warnings.SHIZUKU_BINDER_NOT_READY, goBack = false)
            }
        }

        dontShowAgainCheckBox.setOnCheckedChangeListener {
            SetupPreferences.setDontShowAgain(it)
        }

        dontShowAgain.setOnClickListener {
            dontShowAgainCheckBox.toggle(true)
        }
    }

    override fun onResume() {
        super.onResume()
        if (requireContext().checkForUsageAccessPermission()) {
            usageStatus.text = getString(R.string.granted)
            usageAccess.isClickable = false
        } else {
            usageStatus.text = getString(R.string.not_granted)
        }

        kotlin.runCatching {
            setStorageStatus()
        }.onFailure {
            setStorageStatus()
        }

        showStartAppButton()
    }

    override fun onDestroy() {
        super.onDestroy()
        runCatching {
            // Since this panel will only be opened once, we can skip managing the memory leak if it occurs
            Shizuku.removeRequestPermissionResultListener(requestPermissionResultListener)
        }
    }

    private fun showStartAppButton() {
        if (requireContext().checkForUsageAccessPermission() && checkStoragePermission()) {
            startApp.visible(false)
            skip.gone()
        } else {
            startApp.invisible(false)
            skip.visible(false)
        }
    }

    private fun setStorageStatus() {
        if (checkStoragePermission()) {
            storageStatus.text = getString(R.string.granted)
            storageUri.text = Environment.getExternalStorageDirectory().toString()
            storageUri.visible(false)
            storageAccess.isClickable = false
        } else {
            storageStatus.text = getString(R.string.not_granted)
            storageUri.gone()
            storageAccess.isClickable = true
        }
    }

    private fun openDirectory() {
        if (checkStoragePermission()) {
            setStorageStatus()
        } else {
            askPermission()
        }
    }

    /**
     * Grant storage permission
     */
    private fun askPermission() {
        try {
            val uri = Uri.parse("package:${BuildConfig.APPLICATION_ID}")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                startActivity(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri))
            } else {
                requestPermissionLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            }
        } catch (e: ActivityNotFoundException) {
            showWarning("ERROR: No Activity found to handle this intent", goBack = false)
        }
    }

    private fun isShizukuPermissionGranted(): Boolean {
        return if (Shizuku.isPreV11() || Shizuku.getVersion() < 11) {
            false
        } else {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun setShizukuPermissionState() {
        handler.post {
            try {
                shizukuPermissionState.text = buildString {
                    if (Shizuku.isPreV11().invert()) {
                        if (isShizukuPermissionGranted()) {
                            appendFlag(getString(R.string.granted))
                        } else {
                            appendFlag(getString(R.string.rejected))

                            if (Shizuku.shouldShowRequestPermissionRationale()) {
                                appendFlag(getString(R.string.not_available))
                            }
                        }

                        if (ConfigurationPreferences.isUsingShizuku()) {
                            appendFlag(getString(R.string.enabled))
                        } else {
                            appendFlag(getString(R.string.disabled))
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
                true
            }
            Shizuku.shouldShowRequestPermissionRationale() -> {
                // Users choose "Deny and don't ask again"
                shizukuSwitchView.uncheck(false)
                shizukuSwitchView.gone()
                shizukuPermissionState.text = Warnings.SHIZUKU_PERMISSION_DENIED
                false
            }
            else -> {
                // Request the permission
                Shizuku.requestPermission(requestCode)
                false
            }
        }
    }

    private fun onRequestPermissionsResult(requestCode: Int, grantResult: Int) {
        val granted: Boolean = grantResult == PackageManager.PERMISSION_GRANTED
        Log.d("ConfigurationScreen", "onRequestPermissionsResult: $granted with requestCode: $requestCode")
        ConfigurationPreferences.setUsingShizuku(granted)
        setShizukuPermissionState()

        if (granted) {
            shizukuSwitchView.check(true)
        } else {
            shizukuSwitchView.uncheck(true)
        }
    }

    companion object {
        fun newInstance(goBack: Boolean = false): Setup {
            val args = Bundle()
            args.putBoolean(BundleConstants.goBack, goBack)
            val fragment = Setup()
            fragment.arguments = args
            return fragment
        }

        private const val TAG = "Setup"
    }
}
