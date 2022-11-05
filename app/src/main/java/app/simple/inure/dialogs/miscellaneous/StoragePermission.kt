package app.simple.inure.dialogs.miscellaneous

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentManager
import app.simple.inure.BuildConfig
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayoutWithFactor
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.util.PermissionUtils.checkForUsageAccessPermission
import app.simple.inure.util.PermissionUtils.checkStoragePermission
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible

class StoragePermission : ScopedBottomSheetFragment() {

    private lateinit var container: DynamicRippleLinearLayoutWithFactor
    private lateinit var grant: DynamicRippleTextView
    private lateinit var close: DynamicRippleTextView
    private lateinit var status: TypeFaceTextView

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private var storagePermissionCallbacks: StoragePermissionCallbacks? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_permission_storage, container, false)

        this.container = view.findViewById(R.id.grant_storage_access)
        this.grant = view.findViewById(R.id.grant)
        this.close = view.findViewById(R.id.close)
        this.status = view.findViewById(R.id.status_storage_access)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.forEach {
                when (it.key) {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                        if (it.value) {
                            setStorageStatus()
                        }
                    }
                }
            }
        }

        container.setOnClickListener {
            openDirectory()
        }

        grant.setOnClickListener {
            container.callOnClick()
        }

        close.setOnClickListener {
            if (requireContext().checkStoragePermission()) {
                storagePermissionCallbacks?.onStoragePermissionGranted()
                dismiss()
            } else {
                dismiss()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setStorageStatus()
    }

    private fun openDirectory() {
        if (requireContext().checkStoragePermission()) {
            setStorageStatus()
        } else {
            askPermission()
        }
    }

    private fun setStorageStatus() {
        if (requireContext().checkStoragePermission()) {
            status.text = getString(R.string.granted)
            container.isClickable = false
            grant.gone()
        } else {
            container.isClickable = true
            grant.visible(true)
            status.text = getString(R.string.not_granted)
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

    fun setStoragePermissionCallbacks(storagePermissionCallbacks: StoragePermissionCallbacks) {
        this.storagePermissionCallbacks = storagePermissionCallbacks
    }

    companion object {
        fun newInstance(): StoragePermission {
            val args = Bundle()
            val fragment = StoragePermission()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.newStoragePermissionInstance(): StoragePermission {
            val args = Bundle()
            val fragment = StoragePermission()
            fragment.arguments = args
            fragment.show(this, "storage_permission")
            return fragment
        }

        interface StoragePermissionCallbacks {
            fun onStoragePermissionGranted()
        }
    }
}