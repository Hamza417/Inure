package app.simple.inure.ui.viewers

import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import app.simple.inure.R
import app.simple.inure.adapters.viewers.AdapterPermissions
import app.simple.inure.apk.utils.PackageUtils.isPackageInstalled
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.dialogs.action.PermissionStatus
import app.simple.inure.dialogs.action.PermissionStatus.Companion.showPermissionStatus
import app.simple.inure.dialogs.permissions.PermissionsMenu
import app.simple.inure.extensions.fragments.SearchBarScopedFragment
import app.simple.inure.factories.panels.PackageInfoFactory
import app.simple.inure.helpers.ShizukuServiceHelper
import app.simple.inure.models.PermissionInfo
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.preferences.PermissionPreferences
import app.simple.inure.viewmodels.viewers.PermissionsViewModel
import com.anggrayudi.storage.extension.postToUi
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku

class Permissions : SearchBarScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var options: DynamicRippleImageButton
    private lateinit var permissionsViewModel: PermissionsViewModel
    private lateinit var packageInfoFactory: PackageInfoFactory
    private lateinit var adapterPermissions: AdapterPermissions

    private var isPackageInstalled = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_permissions, container, false)

        recyclerView = view.findViewById(R.id.permissions_recycler_view)
        options = view.findViewById(R.id.permissions_option_btn)
        search = view.findViewById(R.id.permissions_search_btn)
        searchBox = view.findViewById(R.id.permissions_search)
        title = view.findViewById(R.id.permission_title)

        packageInfoFactory = PackageInfoFactory(packageInfo)
        permissionsViewModel = ViewModelProvider(this, packageInfoFactory)[PermissionsViewModel::class.java]
        isPackageInstalled = requirePackageManager().isPackageInstalled(packageInfo.packageName)

        searchBoxState(false, PermissionPreferences.isSearchVisible())
        startPostponedEnterTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        permissionsViewModel.permissions.observe(viewLifecycleOwner) { permissionInfos ->
            if (recyclerView.adapter == null) {
                Log.d("Permissions", "Setting up new adapter")
                adapterPermissions = AdapterPermissions(permissionInfos, searchBox.text.toString().trim(), isPackageInstalled)

                adapterPermissions.setOnPermissionCallbacksListener(object : AdapterPermissions.Companion.PermissionCallbacks {
                    override fun onPermissionClicked(container: View, permissionInfo: PermissionInfo, position: Int) {
                        childFragmentManager.showPermissionStatus(packageInfo, permissionInfo)
                            .setOnPermissionStatusCallbackListener(object : PermissionStatus.Companion.PermissionStatusCallbacks {
                                override fun onSuccess(grantedStatus: Boolean) {
                                    // Record the expected change
                                    val expectedStatus = if (grantedStatus) 1 else 0
                                    permissionsViewModel.recordPermissionChangeRequest(permissionInfo.name, position, expectedStatus)

                                    // Optimistically update UI
                                    adapterPermissions.permissionStatusChanged(position, expectedStatus)

                                    // Schedule a delayed refresh to verify the change
                                    viewLifecycleOwner.lifecycleScope.launch {
                                        permissionsViewModel.refreshPermissionStatus(permissionInfo.name, position)
                                    }
                                }
                            })
                    }

                    override fun onPermissionSwitchClicked(checked: Boolean, permissionInfo: PermissionInfo, position: Int) {
                        val expectedStatus = if (checked) 1 else 0

                        // Record the expected change
                        permissionsViewModel.recordPermissionChangeRequest(permissionInfo.name, position, expectedStatus)

                        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                            val mode = if (checked) "grant" else "revoke"

                            if (ConfigurationPreferences.isUsingRoot()) {
                                kotlin.runCatching {
                                    Shell.cmd("pm $mode ${packageInfo.packageName} ${permissionInfo.name}").exec().let {
                                        // Refresh to get actual status
                                        permissionsViewModel.refreshPermissionStatus(permissionInfo.name, position)
                                    }
                                }.getOrElse {
                                    withContext(Dispatchers.Main) {
                                        showWarning("failed to acquire root", goBack = false)
                                        adapterPermissions.permissionStatusChanged(position, permissionInfo.isGranted)
                                    }
                                }
                            } else if (ConfigurationPreferences.isUsingShizuku()) {
                                kotlin.runCatching {
                                    if (Shizuku.pingBinder()) {
                                        ShizukuServiceHelper.getInstance().getBoundService { shizukuService ->
                                            shizukuService.simpleExecute("pm $mode ${packageInfo.packageName} ${permissionInfo.name}").let {
                                                // Wait a bit for the system to process
                                                Thread.sleep(500)

                                                // Refresh to get actual status
                                                permissionsViewModel.refreshPermissionStatus(permissionInfo.name, position)
                                            }
                                        }
                                    } else {
                                        postToUi {
                                            showWarning("failed to acquire Shizuku", goBack = false)
                                            adapterPermissions.permissionStatusChanged(position, permissionInfo.isGranted)
                                        }
                                    }
                                }.getOrElse {
                                    postToUi {
                                        showWarning("failed to acquire Shizuku", goBack = false)
                                        adapterPermissions.permissionStatusChanged(position, permissionInfo.isGranted)
                                    }
                                }
                            }
                        }
                    }
                })

                recyclerView.setExclusiveAdapter(adapterPermissions)
            } else {
                // Update existing adapter with new data
                Log.d("Permissions", "Updating existing adapter data")
                adapterPermissions.updateData(permissionInfos, searchBox.text.toString().trim())
            }

            setCount(permissionInfos.size)
        }

        // Collect permission change results
        viewLifecycleOwner.lifecycleScope.launch {
            permissionsViewModel.permissionChangeResult.collect { result ->
                result?.let {
                    if (!it.success) {
                        val expectedStatusText = if (permissionsViewModel.lastPermissionChangeRequest.value?.expectedStatus == 1) "granted" else "revoked"
                        val actualStatusText = if (it.actualStatus == 1) "granted" else "revoked"
                        // Permission change failed - show warning
                        showWarning("Failed to change permission state. Expected: $expectedStatusText, Actual: " +
                                            "$actualStatusText. The system maybe disallowing permission change for this app.", goBack = false)
                    }
                }
            }
        }

        permissionsViewModel.getError().observe(viewLifecycleOwner) {
            showError(it)
        }

        permissionsViewModel.getWarning().observe(viewLifecycleOwner) {
            showWarning(it)
        }

        searchBox.doOnTextChanged { text, _, _, _ ->
            if (searchBox.isFocused) {
                permissionsViewModel.loadPermissionData(text.toString().trim())
            }
        }

        options.setOnClickListener {
            PermissionsMenu.newInstance()
                .show(childFragmentManager, PermissionsMenu.TAG)
        }

        search.setOnClickListener {
            if (searchBox.text.isNullOrEmpty()) {
                PermissionPreferences.setSearchVisibility(!PermissionPreferences.isSearchVisible())
            } else {
                searchBox.text?.clear()
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            PermissionPreferences.PERMISSION_SEARCH -> {
                searchBoxState(true, PermissionPreferences.isSearchVisible())
            }

            PermissionPreferences.LABEL_TYPE -> {
                adapterPermissions.update()
            }
        }
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo, keywords: String? = null): Permissions {
            val args = Bundle()
            args.putParcelable(BundleConstants.PACKAGE_INFO, packageInfo)
            args.putString(BundleConstants.KEYWORDS, keywords)
            val fragment = Permissions()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "permissions"
    }
}
