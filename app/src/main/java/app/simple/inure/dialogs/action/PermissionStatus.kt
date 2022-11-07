package app.simple.inure.dialogs.action

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.LoaderImageView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.factories.actions.PermissionStatusFactory
import app.simple.inure.models.PermissionInfo
import app.simple.inure.util.ParcelUtils.parcelable
import app.simple.inure.viewmodels.dialogs.PermissionStatusViewModel

class PermissionStatus : ScopedBottomSheetFragment() {

    private lateinit var loader: LoaderImageView
    private lateinit var status: TypeFaceTextView

    private lateinit var permissionInfo: PermissionInfo
    private lateinit var permissionStatusFactory: PermissionStatusFactory
    private lateinit var permissionStatusViewModel: PermissionStatusViewModel
    private var permissionStatusCallbacks: PermissionStatusCallbacks? = null

    private var mode: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_permission_status, container, false)

        loader = view.findViewById(R.id.loader)
        status = view.findViewById(R.id.permission_status_result)

        permissionInfo = requireArguments().parcelable(BundleConstants.permissionInfo)!!
        packageInfo = requireArguments().parcelable(BundleConstants.packageInfo)!!
        mode = requireArguments().getString(BundleConstants.permissionMode)

        permissionStatusFactory = PermissionStatusFactory(packageInfo,
                                                          permissionInfo,
                                                          mode)

        println(mode)

        permissionStatusViewModel = ViewModelProvider(this, permissionStatusFactory)[PermissionStatusViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        permissionStatusViewModel.getSuccessStatus().observe(viewLifecycleOwner) {
            when (it) {
                "Done" -> {
                    loader.loaded()
                    with(mode == getString(R.string.revoke)) {
                        if (this) {
                            status.setText(R.string.rejected)
                        } else {
                            status.setText(R.string.granted)
                        }

                        permissionStatusCallbacks?.onSuccess(!this)
                    }
                }
                "Failed" -> {
                    loader.error()
                    status.setText(R.string.failed)
                }
            }
        }
    }

    fun setOnPermissionStatusCallbackListener(permissionStatusCallbacks: PermissionStatusCallbacks) {
        this.permissionStatusCallbacks = permissionStatusCallbacks
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo, permissionInfo: PermissionInfo, mode: String): PermissionStatus {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            args.putParcelable(BundleConstants.permissionInfo, permissionInfo)
            args.putString(BundleConstants.permissionMode, mode)
            val fragment = PermissionStatus()
            fragment.arguments = args
            return fragment
        }

        interface PermissionStatusCallbacks {
            fun onSuccess(grantedStatus: Boolean)
        }
    }
}