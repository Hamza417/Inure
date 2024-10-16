package app.simple.inure.ui.installer

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import app.simple.inure.R
import app.simple.inure.adapters.viewers.AdapterPermissions
import app.simple.inure.apk.utils.PackageUtils.isPackageInstalled
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.dialogs.action.PermissionStatus
import app.simple.inure.dialogs.action.PermissionStatus.Companion.showPermissionStatus
import app.simple.inure.extensions.fragments.InstallerLoaderScopedFragment
import app.simple.inure.factories.installer.InstallerViewModelFactory
import app.simple.inure.helpers.ShizukuServiceHelper
import app.simple.inure.models.PermissionInfo
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.viewmodels.installer.InstallerPermissionViewModel
import com.anggrayudi.storage.extension.postToUi
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class Permissions : InstallerLoaderScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView

    private var file: File? = null
    private lateinit var installerPermissionViewModel: InstallerPermissionViewModel

    private var isPackageInstalled = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.installer_fragment_permissions, container, false)

        recyclerView = view.findViewById(R.id.recycler_view)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            file = requireArguments().getSerializable(BundleConstants.file, File::class.java)
        } else {
            @Suppress("DEPRECATION")
            file = requireArguments().getSerializable(BundleConstants.file) as File
        }

        val installerViewModelFactory = InstallerViewModelFactory(null, file)
        installerPermissionViewModel = ViewModelProvider(this, installerViewModelFactory)[InstallerPermissionViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //        installerPermissionViewModel.getPermissionsFile().observe(viewLifecycleOwner) { permissions ->
        //            // (parentFragment as InstallerCallbacks).onLoadingFinished()
        //            // recyclerView.adapter = AdapterInstallerPermissions(permissions)
        //        }

        installerPermissionViewModel.getPermissionsInfo().observe(viewLifecycleOwner) { it ->
            onLoadingFinished()
            packageInfo = it.second
            isPackageInstalled = requirePackageManager().isPackageInstalled(packageInfo.packageName)
            val adapterPermissions = AdapterPermissions(it.first, "", isPackageInstalled)

            adapterPermissions.setOnPermissionCallbacksListener(object : AdapterPermissions.Companion.PermissionCallbacks {
                override fun onPermissionClicked(container: View, permissionInfo: PermissionInfo, position: Int) {
                    childFragmentManager.showPermissionStatus(packageInfo, permissionInfo).setOnPermissionStatusCallbackListener(object : PermissionStatus.Companion.PermissionStatusCallbacks {
                        override fun onSuccess(grantedStatus: Boolean) {
                            adapterPermissions.permissionStatusChanged(position, if (grantedStatus) 1 else 0)
                        }
                    })
                }

                override fun onPermissionSwitchClicked(checked: Boolean, permissionInfo: PermissionInfo, position: Int) {
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                        val mode = if (checked) "grant" else "revoke"

                        if (ConfigurationPreferences.isUsingRoot()) {
                            kotlin.runCatching {
                                Shell.cmd("pm $mode ${packageInfo.packageName} ${permissionInfo.name}").exec().let {
                                    postToUi {
                                        if (it.isSuccess) {
                                            adapterPermissions.permissionStatusChanged(position, if (permissionInfo.isGranted == 1) 0 else 1)
                                        } else {
                                            showWarning("ERR: failed to $mode permission", goBack = false)
                                        }
                                    }
                                }
                            }.getOrElse {
                                postToUi {
                                    showWarning("ERR: failed to acquire root", goBack = false)
                                }
                            }
                        } else if (ConfigurationPreferences.isUsingShizuku()) {
                            kotlin.runCatching {
                                ShizukuServiceHelper.getInstance().getBoundService { shizukuService ->
                                    shizukuService.simpleExecute("pm $mode ${packageInfo.packageName} ${permissionInfo.name}").let {
                                        postToUi {
                                            if (it.isSuccess) {
                                                adapterPermissions.permissionStatusChanged(position, if (permissionInfo.isGranted == 1) 0 else 1)
                                            } else {
                                                showWarning("ERR: failed to $mode permission", goBack = false)
                                                adapterPermissions.permissionStatusChanged(position, permissionInfo.isGranted)
                                            }
                                        }
                                    }
                                }
                            }.getOrElse {
                                postToUi {
                                    showWarning("ERR: failed to acquire Shizuku", goBack = false)
                                }
                            }
                        }
                    }
                }
            })

            recyclerView.adapter = adapterPermissions
        }
    }

    fun setPackageInstalled(packageInstalled: Boolean) {
        isPackageInstalled = packageInstalled
        if (packageInstalled) {
            installerPermissionViewModel.loadPermissionData()
        }
    }

    override fun setupBackPressedDispatcher() {
        /* no-op */
    }

    override fun setupBackPressedCallback(view: ViewGroup) {
        /* no-op */
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
