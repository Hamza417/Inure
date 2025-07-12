package app.simple.inure.ui.panels

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager2.widget.ViewPager2
import app.simple.inure.R
import app.simple.inure.activities.app.MainActivity
import app.simple.inure.activities.association.ApkInstallerActivity
import app.simple.inure.adapters.installer.AdapterInstallerInfoPanels
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.apk.utils.PackageUtils.getPackageInfo
import app.simple.inure.apk.utils.PackageUtils.isPackageInstalled
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.constants.BundleConstants
import app.simple.inure.constants.ServiceConstants
import app.simple.inure.constants.Warnings
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.AppIconImageView
import app.simple.inure.decorations.views.CustomProgressBar
import app.simple.inure.decorations.views.TabBar
import app.simple.inure.dialogs.action.Uninstaller.Companion.uninstallPackage
import app.simple.inure.dialogs.app.Sure
import app.simple.inure.dialogs.configuration.VirusTotalAPI
import app.simple.inure.dialogs.configuration.VirusTotalAPI.Companion.showVirusTotalAPI
import app.simple.inure.dialogs.installer.Downgrade.Companion.showDowngradeDialog
import app.simple.inure.dialogs.installer.InstallAnyway.Companion.showInstallAnyway
import app.simple.inure.dialogs.installer.InstallerMenu.Companion.showInstallerMenu
import app.simple.inure.dialogs.installer.Users
import app.simple.inure.dialogs.installer.Users.Companion.showUsers
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.factories.installer.InstallerViewModelFactory
import app.simple.inure.glide.util.ImageLoader.loadAppIcon
import app.simple.inure.interfaces.fragments.InstallerCallbacks
import app.simple.inure.interfaces.fragments.SureCallbacks
import app.simple.inure.models.User
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.preferences.DevelopmentPreferences
import app.simple.inure.preferences.InstallerPreferences
import app.simple.inure.preferences.VirusTotalPreferences
import app.simple.inure.ui.viewers.VirusTotal
import app.simple.inure.util.AppUtils
import app.simple.inure.util.ConditionUtils.isNotZero
import app.simple.inure.util.ParcelUtils.parcelable
import app.simple.inure.util.ParcelUtils.serializable
import app.simple.inure.util.TextViewUtils.setDrawableLeft
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.installer.InstallerViewModel
import java.io.File

class Installer : ScopedFragment(), InstallerCallbacks {

    private lateinit var installerViewModel: InstallerViewModel

    private lateinit var icon: AppIconImageView
    private lateinit var name: TypeFaceTextView
    private lateinit var packageName: TypeFaceTextView
    private lateinit var version: TypeFaceTextView
    private lateinit var virusTotal: DynamicRippleImageButton
    private lateinit var settings: DynamicRippleImageButton
    private lateinit var install: DynamicRippleTextView
    private lateinit var cancel: DynamicRippleTextView
    private lateinit var launch: DynamicRippleTextView
    private lateinit var update: DynamicRippleTextView
    private lateinit var uninstall: DynamicRippleTextView
    private lateinit var loader: CustomProgressBar
    private lateinit var viewPager: ViewPager2
    private lateinit var tabBar: TabBar

    private lateinit var broadcastReceiver: BroadcastReceiver
    private val intentFilter = IntentFilter()
    private var user: User? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_installer, container, false)

        icon = view.findViewById(R.id.icon)
        name = view.findViewById(R.id.name)
        packageName = view.findViewById(R.id.package_id)
        version = view.findViewById(R.id.version)
        virusTotal = view.findViewById(R.id.virustotal)
        settings = view.findViewById(R.id.settings)
        install = view.findViewById(R.id.install)
        cancel = view.findViewById(R.id.cancel)
        launch = view.findViewById(R.id.launch)
        update = view.findViewById(R.id.update)
        uninstall = view.findViewById(R.id.uninstall)
        viewPager = view.findViewById(R.id.viewPager)
        tabBar = view.findViewById(R.id.tab_bar)
        loader = view.findViewById(R.id.loader)

        kotlin.runCatching {
            icon.transitionName = requireArguments()
                .getString(BundleConstants.transitionName,
                           requireArguments().parcelable<Uri>(BundleConstants.uri)!!.toString())
        }.onFailure {
            icon.transitionName = requireArguments().serializable<File>(BundleConstants.file)!!.absolutePath
        }

        if (AppUtils.isPlayFlavor()) {
            virusTotal.gone(animate = false)
        }

        postponeEnterTransition()

        val factory = InstallerViewModelFactory(requireArguments().parcelable(BundleConstants.uri), requireArguments().serializable(BundleConstants.file))
        installerViewModel = ViewModelProvider(this, factory)[InstallerViewModel::class.java]

        intentFilter.addAction(ServiceConstants.actionSessionStatus)
        viewPager.offscreenPageLimit = if (DevelopmentPreferences.get(DevelopmentPreferences.LOAD_ALL_INSTALLER_PAGES)) {
            5
        } else {
            ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
        }

        postponeEnterTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (requireActivity() is MainActivity) {
            showLoader(manualOverride = true)
        }

        postponeEnterTransition()

        fullVersionCheck()
        loader.visible(animate = true)

        broadcastReceiver = object : BroadcastReceiver() {
            @SuppressLint("UnsafeIntentLaunch")
            override fun onReceive(context: Context, intent: Intent) {
                // Sanitize the intent
                // TODO : Check if this is required

                when (intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -999)) {
                    PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                        val confirmationIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(Intent.EXTRA_INTENT)
                        }

                        if (confirmationIntent != null) {
                            confirmationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            try {
                                context.startActivity(confirmationIntent)
                            } catch (ignored: Exception) {
                            }
                        }
                    }

                    PackageInstaller.STATUS_SUCCESS -> {
                        success()
                    }

                    PackageInstaller.STATUS_FAILURE_ABORTED -> {
                        showWarning(intent.extras!!.getString(PackageInstaller.EXTRA_STATUS_MESSAGE)!!)
                    }

                    PackageInstaller.STATUS_FAILURE_BLOCKED,
                    PackageInstaller.STATUS_FAILURE_CONFLICT -> {
                        showWarning(intent.extras!!.getString(PackageInstaller.EXTRA_STATUS_MESSAGE)!! +
                                            " -> " +
                                            intent.extras!!.getString(PackageInstaller.EXTRA_PACKAGE_NAME)!!)
                    }

                    PackageInstaller.STATUS_FAILURE_INCOMPATIBLE,
                    PackageInstaller.STATUS_FAILURE_INVALID,
                    PackageInstaller.STATUS_FAILURE_TIMEOUT,
                    PackageInstaller.STATUS_FAILURE,
                    PackageInstaller.STATUS_FAILURE_STORAGE -> {
                        showWarning(intent.extras?.getString(PackageInstaller.EXTRA_STATUS_MESSAGE) ?: Warnings.UNIDENTIFIED_ERROR)
                    }
                }
            }
        }

        installerViewModel.getSignatureStatus().observe(viewLifecycleOwner) {
            if (it) {
                version.setDrawableLeft(R.drawable.ic_close_12dp)
            } else {
                version.setDrawableLeft(R.drawable.ic_check_12dp)
            }
        }

        installerViewModel.getPackageInfo().observe(viewLifecycleOwner) {
            loader.gone()
            kotlin.runCatching {
                packageInfo = it

                name.text = packageInfo.safeApplicationInfo.name
                packageName.text = packageInfo.packageName
                version.text = buildString {
                    if (requirePackageManager().isPackageInstalled(packageInfo.packageName)) {
                        append(requirePackageManager().getPackageInfo(packageName.text.toString())?.versionName ?: "")
                        append(" → ")
                        append(packageInfo.versionName)
                    } else {
                        append(packageInfo.versionName)
                    }
                }

                checkLaunchStatus()

                if (requirePackageManager().isPackageInstalled(packageInfo.packageName)) {
                    install.gone()
                    update.visible(true)
                    uninstall.visible(true)
                } else {
                    install.visible(true)
                    update.gone()
                    uninstall.gone()
                }

                install.setOnClickListener {
                    if (InstallerPreferences.isShowUsersList()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            childFragmentManager.showUsers().setUsersCallback(object : Users.Companion.UsersCallback {
                                override fun onUserSelected(user: User) {
                                    this@Installer.user = user
                                    loader.visible(true)
                                    install.gone()
                                    update.gone()
                                    uninstall.gone()
                                    launch.gone()
                                    installerViewModel.install(user)
                                }
                            })
                        } else {
                            loader.visible(true)
                            install.gone()
                            update.gone()
                            uninstall.gone()
                            launch.gone()
                            installerViewModel.install()
                        }
                    } else {
                        loader.visible(true)
                        install.gone()
                        update.gone()
                        uninstall.gone()
                        launch.gone()
                        installerViewModel.install()
                    }
                }

                update.setOnClickListener {
                    install.callOnClick()
                }

                uninstall.setOnClickListener {
                    val sure = Sure.newInstance()
                    sure.setOnSureCallbackListener(object : SureCallbacks {
                        override fun onSure() {
                            childFragmentManager.uninstallPackage(packageInfo) {
                                if (!requirePackageManager().isPackageInstalled(packageInfo.packageName)) {
                                    uninstall.gone(animate = false)
                                    update.gone(animate = false)
                                    install.visible(animate = false)
                                }

                                checkLaunchStatus()
                            }
                        }
                    })

                    sure.show(childFragmentManager, "sure")
                }
            }.onFailure {
                showWarning(it.localizedMessage!!)
            }
        }

        installerViewModel.getFile().observe(viewLifecycleOwner) { file ->
            kotlin.runCatching {
                icon.loadAppIcon(file)

                (view.parent as? ViewGroup)?.doOnPreDraw {
                    startPostponedEnterTransition()
                    hideLoader()
                }

                val titles = arrayListOf<String>().apply {
                    if (InstallerPreferences.getPanelVisibility(InstallerPreferences.IS_INFO_VISIBLE)) add(getString(R.string.information))
                    if (InstallerPreferences.getPanelVisibility(InstallerPreferences.IS_CHANGES_VISIBLE)) add(getString(R.string.changes))
                    if (InstallerPreferences.getPanelVisibility(InstallerPreferences.IS_PERMISSIONS_VISIBLE)) add(getString(R.string.permissions))
                    if (InstallerPreferences.getPanelVisibility(InstallerPreferences.IS_MANIFEST_VISIBLE)) add(getString(R.string.manifest))
                    if (InstallerPreferences.getPanelVisibility(InstallerPreferences.IS_CERTIFICATE_VISIBLE)) add(getString(R.string.certificate))
                    if (InstallerPreferences.getPanelVisibility(InstallerPreferences.IS_TRACKERS_VISIBLE)) add(getString(R.string.trackers))
                }

                viewPager.adapter = AdapterInstallerInfoPanels(this, file, titles.toTypedArray(), packageInfo)
                tabBar.initWithViewPager(viewPager, titles)
            }.onFailure {
                showError(it)
            }
        }

        installerViewModel.getError().observe(viewLifecycleOwner) {
            showError(it)
        }

        installerViewModel.getWarning().observe(viewLifecycleOwner) {
            when {
                /**
                 * This error is thrown when the app is installed on a device with
                 * a lower SDK version than the app's minimum SDK version. For root
                 * users, we can install the app anyway if they want to. The error
                 * is bypassed already for Shizuku users in [app.simple.inure.shizuku.PackageInstaller.createSession].
                 *
                 * @see [app.simple.inure.shizuku.PackageInstaller.createSession]
                 */
                it.contains("INSTALL_FAILED_DEPRECATED_SDK_VERSION") -> {
                    when {
                        ConfigurationPreferences.isUsingRoot() -> {
                            parentFragmentManager.showInstallAnyway(it).setInstallAnywayCallback {
                                installerViewModel.installAnyway()
                            }
                        }
                        else -> {
                            showWarning(it)
                        }
                    }
                }
                it.contains("INSTALL_FAILED_VERSION_DOWNGRADE") -> {
                    childFragmentManager.showDowngradeDialog(it).setUninstallCallbacks {
                        childFragmentManager.uninstallPackage(packageInfo) {
                            if (!requirePackageManager().isPackageInstalled(packageInfo.packageName)) {
                                loader.visible(true)
                                install.gone()
                                update.gone()
                                uninstall.gone()
                                launch.gone()
                                installerViewModel.install(user)
                            }
                        }
                    }
                }
                else -> {
                    showWarning(it)
                }
            }
        }

        installerViewModel.getSuccess().observe(viewLifecycleOwner) {
            if (it.isNotZero()) {
                success()
            }
        }

        cancel.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        settings.setOnClickListener {
            childFragmentManager.showInstallerMenu()
        }

        virusTotal.setOnClickListener {
            try {
                if (VirusTotalPreferences.hasValidAPI()) {
                    openFragmentArc(VirusTotal.newInstance(packageInfo), virusTotal, VirusTotal.TAG)
                } else {
                    childFragmentManager.showVirusTotalAPI()
                        .setOnVirusTotalAPIListener(object : VirusTotalAPI.Companion.onVirusTotalAPIListener {
                            override fun onVirusTotalAPI() {
                                openFragmentArc(VirusTotal.newInstance(packageInfo), virusTotal, VirusTotal.TAG)
                            }
                        })
                }
            } catch (e: UninitializedPropertyAccessException) {
                e.printStackTrace()
                showWarning(Warnings.WAIT_FOR_LOADING, false)
            } catch (e: IllegalStateException) {
                Log.e(TAG, "onViewCreated: " + e.message)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(broadcastReceiver, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(broadcastReceiver)
        } catch (e: UninitializedPropertyAccessException) {
            e.printStackTrace()
        }
    }

    private fun success() {
        update.gone()
        install.gone()
        loader.gone()
        checkLaunchStatus()
        uninstall.visible(false)
        cancel.setText(R.string.close)
    }

    private fun checkLaunchStatus() {
        try {
            if (requirePackageManager().isPackageInstalled(packageInfo.packageName) &&
                    PackageUtils.isAppLaunchable(requireContext(), packageInfo.packageName)) {
                launch.visible(animate = false)

                launch.setOnClickListener {
                    kotlin.runCatching {
                        Log.d("LaunchApp", "Attempting to launch app") // Log before attempting to launch the app
                        PackageUtils.launchThisPackage(requireActivity().applicationContext, packageInfo.packageName)
                        if (requireActivity() is ApkInstallerActivity) {
                            requireActivity().finishAfterTransition()
                        }
                    }.onFailure {
                        it.printStackTrace()
                        Log.e("LaunchApp", "Failed to launch app", it) // Log the error if the app fails to launch
                        showError(it.stackTraceToString())
                    }
                }
            } else {
                launch.gone(animate = false)
            }
        } catch (e: UninitializedPropertyAccessException) {
            e.printStackTrace()
            launch.gone(animate = false)
            showWarning(e.message ?: "Err: 0x000788")
        } catch (e: IllegalStateException) {
            Log.e(TAG, "checkLaunchStatus: " + e.message)
        }
    }

    override fun onLoadingStarted() {
        // loader.visible(true)
    }

    override fun onLoadingFinished() {
        // loader.gone(animate = true)
    }

    companion object {
        fun newInstance(uri: Uri, transitionName: String? = null): Installer {
            val args = Bundle()
            args.putParcelable(BundleConstants.uri, uri)
            args.putString(BundleConstants.transitionName, transitionName)
            val fragment = Installer()
            fragment.arguments = args
            return fragment
        }

        fun newInstance(file: File): Installer {
            val args = Bundle()
            args.putSerializable(BundleConstants.file, file)
            val fragment = Installer()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "Installer"
    }
}
