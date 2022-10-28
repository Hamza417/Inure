package app.simple.inure.ui.installer

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.details.AdapterPermissions
import app.simple.inure.adapters.installer.AdapterInstallerPermissions
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.dialogs.action.PermissionStatus
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.extensions.popup.PopupMenuCallback
import app.simple.inure.factories.installer.InstallerViewModelFactory
import app.simple.inure.models.PermissionInfo
import app.simple.inure.popups.viewers.PopupPermissions
import app.simple.inure.viewmodels.installer.InstallerPermissionViewModel
import java.io.File

class Permissions : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView

    private var file: File? = null
    private lateinit var installerPermissionViewModel: InstallerPermissionViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.installer_fragment_permissions, container, false)

        recyclerView = view.findViewById(R.id.permissions_recycler_view)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            file = requireArguments().getSerializable(BundleConstants.file, File::class.java)
        } else {
            @Suppress("DEPRECATION")
            file = requireArguments().getSerializable(BundleConstants.file) as File
        }

        val installerViewModelFactory = InstallerViewModelFactory(null, file)
        installerPermissionViewModel = ViewModelProvider(requireActivity(), installerViewModelFactory)[InstallerPermissionViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        installerPermissionViewModel.getPermissionsFile().observe(viewLifecycleOwner) { permissions ->
            recyclerView.adapter = AdapterInstallerPermissions(permissions)
        }

        installerPermissionViewModel.getPermissionsInfo().observe(viewLifecycleOwner) {
            val adapterPermissions = AdapterPermissions(it, "")

            adapterPermissions.setOnPermissionCallbacksListener(object : AdapterPermissions.Companion.PermissionCallbacks {
                override fun onPermissionClicked(container: View, permissionInfo: PermissionInfo, position: Int) {

                    val popup = PopupPermissions(container, permissionInfo)

                    popup.setOnMenuClickListener(object : PopupMenuCallback {
                        override fun onMenuItemClicked(source: String) {
                            when (source) {
                                getString(R.string.revoke) -> {
                                    val p = PermissionStatus.newInstance(packageInfo, permissionInfo, getString(R.string.revoke))
                                    p.show(childFragmentManager, "permission_status")
                                    p.setOnPermissionStatusCallbackListener(object : PermissionStatus.Companion.PermissionStatusCallbacks {
                                        override fun onSuccess(grantedStatus: Boolean) {
                                            adapterPermissions.permissionStatusChanged(position, if (grantedStatus) 1 else 0)
                                        }
                                    })
                                }
                                getString(R.string.grant) -> {
                                    val p = PermissionStatus.newInstance(packageInfo, permissionInfo, getString(R.string.grant))
                                    p.show(childFragmentManager, "permission_status")
                                    p.setOnPermissionStatusCallbackListener(object : PermissionStatus.Companion.PermissionStatusCallbacks {
                                        override fun onSuccess(grantedStatus: Boolean) {
                                            adapterPermissions.permissionStatusChanged(position, if (grantedStatus) 1 else 0)
                                        }
                                    })
                                }
                            }
                        }
                    })
                }
            })

            recyclerView.adapter = adapterPermissions
        }
    }

    companion object {
        fun newInstance(file: File): Permissions {
            val args = Bundle()
            args.putSerializable(BundleConstants.file, file)
            val fragment = Permissions()
            fragment.arguments = args
            return fragment
        }
    }
}