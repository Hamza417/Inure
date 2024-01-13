package app.simple.inure.ui.viewers

import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import app.simple.inure.R
import app.simple.inure.adapters.details.AdapterPermissions
import app.simple.inure.apk.utils.PackageUtils.isPackageInstalled
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.dialogs.action.PermissionStatus
import app.simple.inure.dialogs.action.PermissionStatus.Companion.showPermissionStatus
import app.simple.inure.dialogs.menus.PermissionsMenu
import app.simple.inure.extensions.fragments.SearchBarScopedFragment
import app.simple.inure.factories.panels.PackageInfoFactory
import app.simple.inure.models.PermissionInfo
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.preferences.PermissionPreferences
import app.simple.inure.shizuku.ShizukuUtils
import app.simple.inure.viewmodels.viewers.PermissionsViewModel
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

        permissionsViewModel.getPermissions().observe(viewLifecycleOwner) { permissionInfos ->
            adapterPermissions = AdapterPermissions(permissionInfos, searchBox.text.toString().trim(), isPackageInstalled)
            recyclerView.adapter = adapterPermissions

            adapterPermissions.setOnPermissionCallbacksListener(object : AdapterPermissions.Companion.PermissionCallbacks {
                override fun onPermissionClicked(container: View, permissionInfo: PermissionInfo, position: Int) {
                    childFragmentManager.showPermissionStatus(packageInfo, permissionInfo)
                        .setOnPermissionStatusCallbackListener(object : PermissionStatus.Companion.PermissionStatusCallbacks {
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
                                    if (it.isSuccess) {
                                        withContext(Dispatchers.Main) {
                                            adapterPermissions.permissionStatusChanged(position, if (permissionInfo.isGranted == 1) 0 else 1)
                                        }
                                    } else {
                                        withContext(Dispatchers.Main) {
                                            showWarning("ERR: failed to $mode permission", goBack = false)
                                            adapterPermissions.permissionStatusChanged(position, permissionInfo.isGranted)
                                        }
                                    }
                                }
                            }.getOrElse {
                                withContext(Dispatchers.Main) {
                                    showWarning("ERR: failed to acquire root", goBack = false)
                                    adapterPermissions.permissionStatusChanged(position, permissionInfo.isGranted)
                                }
                            }
                        } else if (ConfigurationPreferences.isUsingShizuku()) {
                            kotlin.runCatching {
                                if (Shizuku.pingBinder()) {
                                    ShizukuUtils.execInternal(app.simple.inure.shizuku.Shell.Command(
                                            "pm $mode ${packageInfo.packageName} ${permissionInfo.name}"), null).let {
                                        if (it.isSuccess) {
                                            withContext(Dispatchers.Main) {
                                                adapterPermissions.permissionStatusChanged(position, if (permissionInfo.isGranted == 1) 0 else 1)
                                            }
                                        } else {
                                            withContext(Dispatchers.Main) {
                                                showWarning("ERR: failed to $mode permission", goBack = false)
                                                adapterPermissions.permissionStatusChanged(position, permissionInfo.isGranted)
                                            }
                                        }
                                    }
                                } else {
                                    withContext(Dispatchers.Main) {
                                        showWarning("ERR: failed to acquire Shizuku", goBack = false)
                                        adapterPermissions.permissionStatusChanged(position, permissionInfo.isGranted)
                                    }
                                }
                            }.getOrElse {
                                withContext(Dispatchers.Main) {
                                    showWarning("ERR: failed to acquire Shizuku", goBack = false)
                                    adapterPermissions.permissionStatusChanged(position, permissionInfo.isGranted)
                                }
                            }
                        }
                    }
                }
            })

            searchBox.doOnTextChanged { text, _, _, _ ->
                if (searchBox.isFocused) {
                    permissionsViewModel.loadPermissionData(text.toString().trim())
                }
            }
        }

        permissionsViewModel.getError().observe(viewLifecycleOwner) {
            showError(it)
        }

        permissionsViewModel.getWarning().observe(viewLifecycleOwner) {
            showWarning(it)
        }

        options.setOnClickListener {
            PermissionsMenu.newInstance()
                .show(childFragmentManager, "permission_menu")
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
            PermissionPreferences.permissionSearch -> {
                searchBoxState(true, PermissionPreferences.isSearchVisible())
            }

            PermissionPreferences.labelType -> {
                adapterPermissions.update()
            }
        }
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo, keywords: String? = null): Permissions {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            args.putString(BundleConstants.keywords, keywords)
            val fragment = Permissions()
            fragment.arguments = args
            return fragment
        }
    }
}