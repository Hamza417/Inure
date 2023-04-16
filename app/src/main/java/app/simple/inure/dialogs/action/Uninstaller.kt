package app.simple.inure.dialogs.action

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils.isSystemApp
import app.simple.inure.apk.utils.PackageUtils.uninstallThisPackage
import app.simple.inure.constants.BundleConstants
import app.simple.inure.constants.IntentConstants
import app.simple.inure.extensions.fragments.ScopedActionDialogBottomFragment
import app.simple.inure.factories.actions.UninstallerViewModelFactory
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.viewmodels.dialogs.UninstallerViewModel
import app.simple.inure.viewmodels.panels.*
import rikka.shizuku.Shizuku

class Uninstaller : ScopedActionDialogBottomFragment() {

    lateinit var appUninstallObserver: ActivityResultLauncher<Intent>

    var listener: (() -> Unit)? = null

    private var isAppDataLoaded = false
    private var isBatchLoaded = false
    private var isUsageDataLoaded = false
    private var isSearchLoaded = false
    private var isUninstalledPackagesLoaded = false
    private var isDisabledPackagesLoaded = false
    private var isRecentlyInstalledLoaded = false
    private var isRecentlyUpdatedLoaded = false
    private var isFrequentlyUsedLoaded = false
    private var isBatteryOptimizationLoaded = false
    private var isBootManagerLoaded = false

    override fun getLayoutViewId(): Int {
        return R.layout.dialog_hide
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (ConfigurationPreferences.isUsingRoot()) {
            with(ViewModelProvider(this, UninstallerViewModelFactory(packageInfo))[UninstallerViewModel::class.java]) {
                getError().observe(viewLifecycleOwner) {
                    showError(it)
                }

                getSuccessStatus().observe(viewLifecycleOwner) {
                    when (it) {
                        "Done" -> {
                            loader.loaded()
                            status.setText(R.string.done)
                            listener?.invoke()
                        }
                        "Failed" -> {
                            loader.error()
                            status.setText(R.string.failed)
                        }
                    }
                }


                getWarning().observe(viewLifecycleOwner) {
                    showWarning(it)
                }
            }
        } else if (ConfigurationPreferences.isUsingShizuku()) { // This block could be merged with the above block
            if (Shizuku.pingBinder()) {
                with(ViewModelProvider(this, UninstallerViewModelFactory(packageInfo))[UninstallerViewModel::class.java]) {
                    getError().observe(viewLifecycleOwner) {
                        showError(it)
                    }

                    getSuccessStatus().observe(viewLifecycleOwner) {
                        when (it) {
                            "Done" -> {
                                loader.loaded()
                                status.setText(R.string.done)
                                listener?.invoke()
                            }
                            "Failed" -> {
                                loader.error()
                                status.setText(R.string.failed)
                            }
                        }
                    }
                }
            } else {
                if (packageInfo.isSystemApp()) {
                    loader.error()
                    status.setText(R.string.failed)
                } else {
                    useAPIUninstaller()
                }
            }
        } else {
            useAPIUninstaller()
        }
    }

    private fun useAPIUninstaller() {
        kotlin.runCatching {
            method.setText(R.string.package_manager)
            status.setText(R.string.waiting)

            appUninstallObserver = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                when (result.resultCode) {
                    Activity.RESULT_OK -> {
                        loader.loaded()
                        status.setText(R.string.done)
                        listener?.invoke()
                    }
                    Activity.RESULT_CANCELED -> {
                        loader.error()
                        status.setText(R.string.cancelled)
                    }
                }
            }

            packageInfo.uninstallThisPackage(appUninstallObserver)
        }.onFailure {
            loader.error()
            status.setText(R.string.failed)
        }
    }

    private fun sendUninstalledBroadcast() {
        val intent = Intent(IntentConstants.ACTION_APP_UNINSTALLED)
        intent.data = Uri.parse("package:${packageInfo.packageName}")
        requireContext().sendBroadcast(intent)
    }

    private fun refreshAppData() {
        status.setText(R.string.refetching_data)
        status.append("…")

        val appsViewModel = ViewModelProvider(requireActivity())[AppsViewModel::class.java]
        val usageStatsData = ViewModelProvider(requireActivity())[UsageStatsViewModel::class.java]
        val searchViewModel = ViewModelProvider(requireActivity())[SearchViewModel::class.java]
        val homeViewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]
        val batchViewModel = ViewModelProvider(requireActivity())[BatchViewModel::class.java]

        val batteryOptimizationViewModel = if (ConfigurationPreferences.isUsingRoot()) {
            ViewModelProvider(requireActivity())[BatteryOptimizationViewModel::class.java]
        } else {
            isBatteryOptimizationLoaded = true
            null
        }

        val bootManagerViewModel = if (ConfigurationPreferences.isUsingRoot()) {
            ViewModelProvider(requireActivity())[BootManagerViewModel::class.java]
        } else {
            isBootManagerLoaded = true
            null
        }

        appsViewModel.onAppUninstalled(packageInfo.packageName)
        usageStatsData.onAppUninstalled(packageInfo.packageName)
        searchViewModel.onAppUninstalled(packageInfo.packageName)
        homeViewModel.onAppUninstalled(packageInfo.packageName)
        batchViewModel.onAppUninstalled(packageInfo.packageName)
        batteryOptimizationViewModel?.onAppUninstalled(packageInfo.packageName)
        bootManagerViewModel?.onAppUninstalled(packageInfo.packageName)

        appsViewModel.getAppData().observe(viewLifecycleOwner) {
            isAppDataLoaded = true
            dismissUninstaller()
        }

        batchViewModel.getBatchData().observe(viewLifecycleOwner) {
            isBatchLoaded = true
            dismissUninstaller()
        }

        usageStatsData.usageData.observe(viewLifecycleOwner) {
            isUsageDataLoaded = true
            dismissUninstaller()
        }

        searchViewModel.getSearchData().observe(viewLifecycleOwner) {
            isSearchLoaded = true
            dismissUninstaller()
        }

        searchViewModel.getDeepSearchData().observe(viewLifecycleOwner) {
            isSearchLoaded = true
            dismissUninstaller()
        }

        homeViewModel.getRecentlyInstalled().observe(viewLifecycleOwner) {
            isRecentlyInstalledLoaded = true
            dismissUninstaller()
        }

        homeViewModel.getRecentlyUpdated().observe(viewLifecycleOwner) {
            isRecentlyUpdatedLoaded = true
            dismissUninstaller()
        }

        homeViewModel.getMostUsed().observe(viewLifecycleOwner) {
            isFrequentlyUsedLoaded = true
            dismissUninstaller()
        }

        homeViewModel.getDisabledApps().observe(viewLifecycleOwner) {
            isDisabledPackagesLoaded = true
            dismissUninstaller()
        }

        homeViewModel.getUninstalledPackages().observe(viewLifecycleOwner) {
            isUninstalledPackagesLoaded = true
            dismissUninstaller()
        }

        batteryOptimizationViewModel?.getBatteryOptimizationData()?.observe(viewLifecycleOwner) {
            isBatteryOptimizationLoaded = true
            dismissUninstaller()
        }

        bootManagerViewModel?.getBootComponentData()?.observe(viewLifecycleOwner) {
            isBootManagerLoaded = true
            dismissUninstaller()
        }
    }

    private fun isEverythingLoaded(): Boolean {
        return isAppDataLoaded &&
                isUsageDataLoaded &&
                isSearchLoaded &&
                isUninstalledPackagesLoaded &&
                isDisabledPackagesLoaded &&
                isFrequentlyUsedLoaded &&
                isRecentlyUpdatedLoaded &&
                isRecentlyInstalledLoaded &&
                isBatteryOptimizationLoaded &&
                isBatchLoaded &&
                isBootManagerLoaded
    }

    private fun dismissUninstaller() {
        if (isEverythingLoaded()) {
            listener?.invoke()
        }
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo): Uninstaller {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            val fragment = Uninstaller()
            fragment.arguments = args
            return fragment
        }
    }
}