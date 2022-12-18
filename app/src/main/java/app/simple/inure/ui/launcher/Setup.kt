package app.simple.inure.ui.launcher

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
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
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayout
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.switchview.SwitchView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.ui.preferences.subscreens.AccentColor
import app.simple.inure.ui.preferences.subscreens.AppearanceTypeFace
import app.simple.inure.util.PermissionUtils.checkForUsageAccessPermission
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.invisible
import app.simple.inure.util.ViewUtils.visible
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Setup : ScopedFragment() {

    private lateinit var usageAccess: DynamicRippleLinearLayout
    private lateinit var storageAccess: DynamicRippleLinearLayout
    private lateinit var typeface: DynamicRippleLinearLayout
    private lateinit var accent: DynamicRippleLinearLayout
    private lateinit var usageStatus: TypeFaceTextView
    private lateinit var storageStatus: TypeFaceTextView
    private lateinit var storageUri: TypeFaceTextView
    private lateinit var rootSwitchView: SwitchView
    private lateinit var startApp: DynamicRippleTextView
    private lateinit var skip: DynamicRippleTextView

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

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
        startApp = view.findViewById(R.id.start_app_now)
        skip = view.findViewById(R.id.skip_setup)

        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.forEach {
                when (it.key) {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                        if (it.value) {
                            setStorageStatus()
                            showStartAppButton()
                        }
                    }
                }
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        rootSwitchView.setChecked(ConfigurationPreferences.isUsingRoot())

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

                    Shell.getShell()

                    if (Shell.isAppGrantedRoot() == true) {
                        ConfigurationPreferences.setUsingRoot(true)
                    } else {
                        ConfigurationPreferences.setUsingRoot(false)
                    }
                }
            } else {
                ConfigurationPreferences.setUsingRoot(false)
            }
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
        val uri = Uri.parse("package:${BuildConfig.APPLICATION_ID}")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startActivity(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri))
        } else {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE))
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

    companion object {
        fun newInstance(goBack: Boolean = false): Setup {
            val args = Bundle()
            args.putBoolean(BundleConstants.goBack, goBack)
            val fragment = Setup()
            fragment.arguments = args
            return fragment
        }
    }
}