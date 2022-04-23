package app.simple.inure.ui.launcher

import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.AppOpsManagerCompat
import androidx.lifecycle.lifecycleScope
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayout
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.switchview.SwitchView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.preferences.MainPreferences
import app.simple.inure.ui.preferences.subscreens.AccentColor
import app.simple.inure.ui.preferences.subscreens.AppearanceTypeFace
import app.simple.inure.util.ColorUtils.resolveAttrColor
import app.simple.inure.util.FragmentHelper
import app.simple.inure.util.NullSafety.isNull
import app.simple.inure.util.PermissionUtils.arePermissionsGranted
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

    private lateinit var appStorageAccessResult: ActivityResultLauncher<Intent>

    private val flags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
            Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
            Intent.FLAG_GRANT_PREFIX_URI_PERMISSION

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

        appStorageAccessResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION

                    result.data?.data?.normalizeScheme().also {
                        requireActivity().contentResolver.takePersistableUriPermission(it!!, takeFlags)
                        MainPreferences.setStoragePermissionUri(it)
                        setStorageStatus(it)
                    }

                    showStartAppButton()
                }
                Activity.RESULT_CANCELED -> {
                    showStartAppButton()
                    setStorageStatus(null)
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
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }

        storageAccess.setOnClickListener {
            openDirectory()
        }

        startApp.setOnClickListener {
            if (checkForPermission() && !requireActivity().contentResolver.persistedUriPermissions.isNullOrEmpty()) {
                FragmentHelper.openFragment(
                        requireActivity().supportFragmentManager,
                        SplashScreen.newInstance(false), view.findViewById(R.id.imageView3))
            } else {
                Toast.makeText(requireContext(), R.string.ss_please_grant_storage_permission, Toast.LENGTH_SHORT).show()
            }
        }

        skip.setOnClickListener {
            FragmentHelper.openFragment(
                    requireActivity().supportFragmentManager,
                    SplashScreen.newInstance(true), view.findViewById(R.id.imageView3))
        }

        accent.setOnClickListener {
            FragmentHelper.openFragment(parentFragmentManager, AccentColor.newInstance(), "accent_color")
        }

        typeface.setOnClickListener {
            FragmentHelper.openFragment(parentFragmentManager, AppearanceTypeFace.newInstance(), "typeface")
        }

        rootSwitchView.setOnSwitchCheckedChangeListener {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                if (it && Shell.rootAccess()) {
                    ConfigurationPreferences.setUsingRoot(true)

                    withContext(Dispatchers.Main) {
                        rootSwitchView.setChecked(true)
                    }
                } else {
                    ConfigurationPreferences.setUsingRoot(false)

                    withContext(Dispatchers.Main) {
                        (false).also { rootSwitchView.setChecked(it) }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (checkForPermission()) {
            usageStatus.text = getString(R.string.granted)
            usageAccess.isClickable = false
        } else {
            usageStatus.text = getString(R.string.not_granted)
        }

        kotlin.runCatching {
            setStorageStatus(Uri.parse(MainPreferences.getStoragePermissionUri()!!))
        }.onFailure {
            setStorageStatus(null)
        }

        showStartAppButton()
    }

    private fun showStartAppButton() {
        if (checkForPermission() && requireContext().contentResolver.persistedUriPermissions.isNotEmpty()) {
            startApp.visible(true)
        } else {
            startApp.invisible(true)
        }
    }

    private fun checkForPermission(): Boolean {
        val appOps = requireContext().getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), requireContext().packageName)
        } else {
            @Suppress("Deprecation")
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), requireContext().packageName)
        }

        return mode == AppOpsManagerCompat.MODE_ALLOWED
    }

    private fun setStorageStatus(uri: Uri?) {
        if (requireContext().arePermissionsGranted(uri.toString())) {
            storageStatus.text = getString(R.string.granted)
            storageUri.text = uri.toString()
            storageUri.visible(false)
            storageStatus.setTextColor(requireContext().resolveAttrColor(R.attr.colorAppAccent))
            storageAccess.isClickable = false
        } else {
            storageStatus.text = getString(R.string.not_granted)
            storageUri.gone()
            storageAccess.isClickable = true
        }
    }

    private fun openDirectory() {
        // read uriString from shared preferences
        val uriString = MainPreferences.getStoragePermissionUri()
        when {
            uriString.isNull() -> {
                askPermission()
            }
            requireContext().arePermissionsGranted(uriString!!) -> {
                setStorageStatus(Uri.parse(uriString))
            }
            else -> {
                askPermission()
            }
        }
    }

    /**
     * Choose a directory using the system's file picker.
     */
    private fun askPermission() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent.addFlags(flags)
        appStorageAccessResult.launch(intent)
    }

    companion object {
        fun newInstance(): Setup {
            val args = Bundle()
            val fragment = Setup()
            fragment.arguments = args
            return fragment
        }
    }
}