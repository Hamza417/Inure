package app.simple.inure.ui.launcher

import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.AppOpsManagerCompat
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayout
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.dialogs.appearance.AccentColor
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.ui.preferences.subscreens.AppearanceTypeFace
import app.simple.inure.util.ColorUtils.resolveAttrColor
import app.simple.inure.util.FragmentHelper
import app.simple.inure.util.ViewUtils.makeInvisible
import app.simple.inure.util.ViewUtils.makeVisible

class Setup : ScopedFragment() {

    private lateinit var usageAccess: DynamicRippleLinearLayout
    private lateinit var storageAccess: DynamicRippleLinearLayout
    private lateinit var typeface: DynamicRippleLinearLayout
    private lateinit var accent: DynamicRippleLinearLayout
    private lateinit var usageStatus: TypeFaceTextView
    private lateinit var storageStatus: TypeFaceTextView
    private lateinit var startApp: DynamicRippleTextView
    private lateinit var skip: DynamicRippleTextView

    lateinit var appStorageAccessResult: ActivityResultLauncher<Intent>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_setup, container, false)

        usageAccess = view.findViewById(R.id.grant_usage_access)
        storageAccess = view.findViewById(R.id.grant_storage_access)
        typeface = view.findViewById(R.id.setup_typeface)
        accent = view.findViewById(R.id.setup_accent_color)
        usageStatus = view.findViewById(R.id.status_usage_access)
        storageStatus = view.findViewById(R.id.status_storage_access)
        startApp = view.findViewById(R.id.start_app_now)
        skip = view.findViewById(R.id.skip_setup)

        appStorageAccessResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION

                    result.data.also {
                        requireActivity().contentResolver.takePersistableUriPermission(it!!.data?.normalizeScheme()!!, takeFlags)
                    }

                    showStartAppButton()
                    setStorageStatus()
                }
                Activity.RESULT_CANCELED -> {
                    storageStatus.text = getString(R.string.rejected_contextual)
                    storageStatus.setTextColor(Color.RED)

                    showStartAppButton()
                    setStorageStatus()
                }
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startPostponedEnterTransition()

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
            }
        }

        skip.setOnClickListener {
            FragmentHelper.openFragment(
                requireActivity().supportFragmentManager,
                SplashScreen.newInstance(true), view.findViewById(R.id.imageView3))
        }

        accent.setOnClickListener {
            AccentColor.newInstance().show(childFragmentManager, "accent_color")
        }

        typeface.setOnClickListener {
            FragmentHelper.openFragment(parentFragmentManager, AppearanceTypeFace.newInstance(), "typeface")
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

        showStartAppButton()
        setStorageStatus()
    }

    private fun requestStoragePermission() {
        requireActivity().requestPermissions(
            arrayOf(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ), 1729) // Taxi Cab number or Hardy-Ramanujan number
    }

    private fun showStartAppButton() {
        if (checkForPermission() && !requireActivity().contentResolver.persistedUriPermissions.isNullOrEmpty()) {
            startApp.makeVisible()
        } else {
            startApp.makeInvisible()
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

    private fun setStorageStatus() {
        if (requireActivity().contentResolver.persistedUriPermissions.isNotEmpty()) {
            storageStatus.text = getString(R.string.granted)
            storageStatus.setTextColor(requireContext().resolveAttrColor(R.attr.colorAppAccent))
            storageAccess.isClickable = false
        } else {
            storageStatus.text = getString(R.string.not_granted)
        }
    }

    private fun openDirectory() {
        // Choose a directory using the system's file picker.
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent.addFlags(
            Intent.FLAG_GRANT_READ_URI_PERMISSION
                    or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                    or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
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