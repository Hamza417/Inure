package app.simple.inure.ui.launcher

import android.Manifest
import android.content.ActivityNotFoundException
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
import androidx.core.net.toUri
import app.simple.inure.BuildConfig
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayout
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.toggles.CheckBox
import app.simple.inure.decorations.toggles.Switch
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.fragments.ShizukuStateFragment
import app.simple.inure.helpers.RootStateHelper.setRootState
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.preferences.SetupPreferences
import app.simple.inure.ui.preferences.subscreens.AccentColor
import app.simple.inure.ui.preferences.subscreens.AppearanceTypeFace
import app.simple.inure.util.PermissionUtils.checkForUsageAccessPermission
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.invisible
import app.simple.inure.util.ViewUtils.visible

class Setup : ShizukuStateFragment() {

    private lateinit var usageAccess: DynamicRippleLinearLayout
    private lateinit var storageAccess: DynamicRippleLinearLayout
    private lateinit var typeface: DynamicRippleLinearLayout
    private lateinit var accent: DynamicRippleLinearLayout
    private lateinit var usageStatus: TypeFaceTextView
    private lateinit var storageStatus: TypeFaceTextView
    private lateinit var storageUri: TypeFaceTextView
    private lateinit var rootSwitchView: Switch
    private lateinit var startApp: DynamicRippleTextView
    private lateinit var skip: DynamicRippleTextView
    private lateinit var dontShowAgainCheckBox: CheckBox
    private lateinit var dontShowAgain: DynamicRippleTextView

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
                }
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        rootSwitchView.isChecked = ConfigurationPreferences.isUsingRoot()
        dontShowAgainCheckBox.isChecked = SetupPreferences.isDontShowAgain()

        rootSwitchView.setRootState(viewLifecycleOwner = viewLifecycleOwner)

        usageAccess.setOnClickListener {
            try {
                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                intent.data = Uri.fromParts("package", requireContext().packageName, null)
                kotlin.runCatching {
                    startActivity(intent)
                }.onFailure {
                    intent.data = null
                    startActivity(intent)
                }
            } catch (e: ActivityNotFoundException) {
                showWarning("ERROR: No Activity found to handle this intent", goBack = false)
            }
        }

        storageAccess.setOnClickListener {
            openDirectory()
        }

        startApp.setOnClickListener {
            if (requireArguments().getBoolean(BundleConstants.GO_BACK)) {
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
            openFragmentSlide(AccentColor.newInstance(), AccentColor.TAG)
        }

        typeface.setOnClickListener {
            openFragmentSlide(AppearanceTypeFace.newInstance(), AppearanceTypeFace.TAG)
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
            val uri = "package:${BuildConfig.APPLICATION_ID}".toUri()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                startActivity(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri))
            } else {
                requestPermissionLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            }
        } catch (e: ActivityNotFoundException) {
            showWarning("ERROR: No Activity found to handle this intent", goBack = false)
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
            args.putBoolean(BundleConstants.GO_BACK, goBack)
            val fragment = Setup()
            fragment.arguments = args
            return fragment
        }

        private const val TAG = "Setup"
    }
}
