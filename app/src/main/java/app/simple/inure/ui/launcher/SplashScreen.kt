package app.simple.inure.ui.launcher

import android.Manifest
import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.os.Process
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.core.app.AppOpsManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.simple.inure.BuildConfig
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils.isPackageInstalled
import app.simple.inure.constants.BundleConstants
import app.simple.inure.crash.CrashReporter
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.LoaderImageView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.preferences.AccessibilityPreferences
import app.simple.inure.preferences.BehaviourPreferences
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.preferences.DevelopmentPreferences
import app.simple.inure.preferences.MainPreferences
import app.simple.inure.preferences.SetupPreferences
import app.simple.inure.preferences.TrialPreferences
import app.simple.inure.services.DataLoaderService
import app.simple.inure.ui.panels.Home
import app.simple.inure.util.AppUtils
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.viewmodels.launcher.LauncherViewModel
import app.simple.inure.viewmodels.panels.ApkBrowserViewModel
import app.simple.inure.viewmodels.panels.AppsViewModel
import app.simple.inure.viewmodels.panels.BatchViewModel
import app.simple.inure.viewmodels.panels.BatteryOptimizationViewModel
import app.simple.inure.viewmodels.panels.BootManagerViewModel
import app.simple.inure.viewmodels.panels.DebloatViewModel
import app.simple.inure.viewmodels.panels.HomeViewModel
import app.simple.inure.viewmodels.panels.NotesViewModel
import app.simple.inure.viewmodels.panels.SearchViewModel
import app.simple.inure.viewmodels.panels.TagsViewModel
import app.simple.inure.viewmodels.panels.UsageStatsViewModel
import app.simple.inure.viewmodels.viewers.SensorsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashScreen : ScopedFragment() {

    private lateinit var icon: ImageView
    private lateinit var loaderImageView: LoaderImageView
    private lateinit var daysLeft: TypeFaceTextView

    private var isAppDataLoaded = false
    private var isBatchLoaded = false
    private var isUsageDataLoaded = false
    private var areSensorsLoaded = false
    private var isSearchLoaded = false
    private var isNotesLoaded = false
    private var isUninstalledPackagesLoaded = false
    private var isDisabledPackagesLoaded = false
    private var isRecentlyInstalledLoaded = false
    private var isRecentlyUpdatedLoaded = false
    private var isFrequentlyUsedLoaded = false
    private var isBatteryOptimizationLoaded = false
    private var isBootManagerLoaded = false
    private var isTagsLoaded = false
    private var isDebloatLoaded = false

    private val launcherViewModel: LauncherViewModel by viewModels()

    private var serviceConnection: ServiceConnection? = null
    private var dataLoaderService: DataLoaderService? = null
    private var broadcastReceiver: BroadcastReceiver? = null

    private var intentFilter: IntentFilter = IntentFilter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_splash_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        intentFilter.addAction(DataLoaderService.APPS_LOADED)
        intentFilter.addAction(DataLoaderService.UNINSTALLED_APPS_LOADED)
        intentFilter.addAction(DataLoaderService.INSTALLED_APPS_LOADED)

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == DataLoaderService.APPS_LOADED) {
                    proceed()
                }
            }
        }

        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                dataLoaderService = (service as DataLoaderService.LoaderBinder).getService()
                if (dataLoaderService?.hasDataLoaded() == true) {
                    proceed()
                } else {
                    dataLoaderService?.startLoading()
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                dataLoaderService = null
            }
        }

        icon = view.findViewById(R.id.imageView)
        loaderImageView = view.findViewById(R.id.loader)
        daysLeft = view.findViewById(R.id.days_left)

        unlockStateChecker()

        if (AccessibilityPreferences.isAnimationReduced().invert()) {
            icon.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.app_icon_animation))
        }

        if (BehaviourPreferences.isSkipLoading()) {
            loaderImageView.alpha = 0F
        }

        // (icon.drawable as AnimatedVectorDrawable).start()

        viewLifecycleOwner.lifecycleScope.launch {
            if (BehaviourPreferences.isSkipLoading()) {
                delay(250L)
            }

            // Initialize native crash handler
            if (DevelopmentPreferences.get(DevelopmentPreferences.crashHandler).invert()) {
                if (BuildConfig.DEBUG.invert()) {
                    CrashReporter(requireContext()).initialize()
                }
            }

            when {
                MainPreferences.isDisclaimerAgreed().invert() -> { // First check if disclaimer is agreed
                    openFragmentSlide(Disclaimer.newInstance())
                }

                requireArguments().getBoolean(BundleConstants.skip) -> { // Second check if setup is skipped
                    startLoaderService()
                }

                !checkForPermission() -> {
                    if (SetupPreferences.isDontShowAgain()) { // If setup not skipped open setup
                        startLoaderService()
                    } else {
                        openFragmentSlide(Setup.newInstance())
                    }
                }

                else -> { // Load all data
                    startLoaderService()
                }
            }
        }
    }

    private fun startLoaderService() {
        val intent = Intent(requireContext(), DataLoaderService::class.java)
        requireContext().bindService(intent, serviceConnection!!, Context.BIND_AUTO_CREATE)

        if (BehaviourPreferences.isSkipLoading()) {
            proceed()
        }
    }

    private fun proceed() {
        val appsViewModel = ViewModelProvider(requireActivity())[AppsViewModel::class.java]
        val usageStatsData = ViewModelProvider(requireActivity())[UsageStatsViewModel::class.java]
        val sensorsViewModel = ViewModelProvider(requireActivity())[SensorsViewModel::class.java]
        val searchViewModel = ViewModelProvider(requireActivity())[SearchViewModel::class.java]
        val homeViewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]
        val batchViewModel = ViewModelProvider(requireActivity())[BatchViewModel::class.java]
        val notesViewModel = ViewModelProvider(requireActivity())[NotesViewModel::class.java]
        val apkBrowserViewModel = ViewModelProvider(requireActivity())[ApkBrowserViewModel::class.java]
        val tagsViewModel = ViewModelProvider(requireActivity())[TagsViewModel::class.java]

        val batteryOptimizationViewModel = if (ConfigurationPreferences.isUsingRoot() || ConfigurationPreferences.isUsingShizuku()) {
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

        val debloatViewModel = if (ConfigurationPreferences.isUsingRoot() || ConfigurationPreferences.isUsingShizuku()) {
            ViewModelProvider(requireActivity())[DebloatViewModel::class.java]
        } else {
            isDebloatLoaded = true
            null
        }

        val startTime = System.currentTimeMillis()

        appsViewModel.getAppData().observe(viewLifecycleOwner) {
            Log.d(TAG, "Apps loaded in ${(System.currentTimeMillis() - startTime) / 1000} seconds")
            isAppDataLoaded = true
            openApp()
        }

        batchViewModel.getBatchData().observe(viewLifecycleOwner) {
            Log.d(TAG, "Batch loaded in ${(System.currentTimeMillis() - startTime) / 1000} seconds")
            isBatchLoaded = true
            openApp()
        }

        usageStatsData.usageData.observe(viewLifecycleOwner) {
            Log.d(TAG, "Usage stats loaded in ${(System.currentTimeMillis() - startTime) / 1000} seconds")
            isUsageDataLoaded = true
            openApp()
        }

        sensorsViewModel.getSensorsData().observe(viewLifecycleOwner) {
            Log.d(TAG, "Sensors loaded in ${(System.currentTimeMillis() - startTime) / 1000} seconds")
            areSensorsLoaded = true
            openApp()
        }

        searchViewModel.getSearchData().observe(viewLifecycleOwner) {
            Log.d(TAG, "Search data loaded in ${(System.currentTimeMillis() - startTime) / 1000} seconds")
            isSearchLoaded = true
            openApp()
        }

        searchViewModel.getSearchData().observe(viewLifecycleOwner) {
            Log.d(TAG, "Deep search data loaded in ${(System.currentTimeMillis() - startTime) / 1000} seconds")
            isSearchLoaded = true
            openApp()
        }

        homeViewModel.getRecentlyInstalled().observe(viewLifecycleOwner) {
            Log.d(TAG, "Recently installed data loaded in ${(System.currentTimeMillis() - startTime) / 1000} seconds")
            isRecentlyInstalledLoaded = true
            openApp()
        }

        homeViewModel.getRecentlyUpdated().observe(viewLifecycleOwner) {
            Log.d(TAG, "Recently updated data loaded in ${(System.currentTimeMillis() - startTime) / 1000} seconds")
            isRecentlyUpdatedLoaded = true
            openApp()
        }

        homeViewModel.getMostUsed().observe(viewLifecycleOwner) {
            Log.d(TAG, "Most used data loaded in ${(System.currentTimeMillis() - startTime) / 1000} seconds")
            isFrequentlyUsedLoaded = true
            openApp()
        }

        homeViewModel.getDisabledApps().observe(viewLifecycleOwner) {
            Log.d(TAG, "Disabled apps data loaded in ${(System.currentTimeMillis() - startTime) / 1000} seconds")
            isDisabledPackagesLoaded = true
            openApp()
        }

        homeViewModel.getUninstalledPackages().observe(viewLifecycleOwner) {
            Log.d(TAG, "Uninstalled packages data loaded in ${(System.currentTimeMillis() - startTime) / 1000} seconds")
            isUninstalledPackagesLoaded = true
            openApp()
        }

        batteryOptimizationViewModel?.getBatteryOptimizationData()?.observe(viewLifecycleOwner) {
            Log.d(TAG, "Battery optimization data loaded in ${(System.currentTimeMillis() - startTime) / 1000} seconds")
            isBatteryOptimizationLoaded = true
            openApp()
        }

        notesViewModel.getNotesData().observe(viewLifecycleOwner) {
            Log.d(TAG, "Notes data loaded in ${(System.currentTimeMillis() - startTime) / 1000} seconds")
            isNotesLoaded = true
            openApp()
        }

        tagsViewModel.getTags().observe(viewLifecycleOwner) {
            Log.d(TAG, "Tags data loaded in ${(System.currentTimeMillis() - startTime) / 1000} seconds")
            isTagsLoaded = true
            openApp()
        }

        /**
         * One shell warning is enough, I guess!!
         * Skip the boot manager ones if the user has already seen the warning
         */
        batteryOptimizationViewModel?.warning?.observe(viewLifecycleOwner) {
            showWarning(it, goBack = false)

            if (ConfigurationPreferences.isUsingShizuku()) {
                isBatteryOptimizationLoaded = true
                openApp()
            }
        }

        bootManagerViewModel?.getBootComponentData()?.observe(viewLifecycleOwner) {
            Log.d(TAG, "Boot manager data loaded in ${(System.currentTimeMillis() - startTime) / 1000} seconds")
            isBootManagerLoaded = true
            openApp()
        }

        /**
         * We'll only trigger the APKs loading if the user has enabled the feature
         */
        apkBrowserViewModel.getApkFiles().observe(viewLifecycleOwner) {
            Log.d(TAG, "Apk files loaded in ${(System.currentTimeMillis() - startTime) / 1000} seconds")
        }

        debloatViewModel?.getBloatList()?.observe(viewLifecycleOwner) {
            Log.d(TAG, "Debloat data loaded in ${(System.currentTimeMillis() - startTime) / 1000} seconds")
            isDebloatLoaded = true
            openApp()
        }

        if (BehaviourPreferences.isSkipLoading()) {
            openApp()
        }
    }

    private fun openApp() {
        if (BehaviourPreferences.isSkipLoading()) {
            launchHome()
        } else {
            if (isEverythingLoaded()) {
                launchHome()
            }
        }
    }

    private fun launchHome() {
        openFragmentArc(Home.newInstance(), icon)
    }

    private fun isEverythingLoaded(): Boolean {
        return isAppDataLoaded &&
                isUsageDataLoaded &&
                areSensorsLoaded &&
                isSearchLoaded &&
                isUninstalledPackagesLoaded &&
                isDisabledPackagesLoaded &&
                isFrequentlyUsedLoaded &&
                isRecentlyUpdatedLoaded &&
                isRecentlyInstalledLoaded &&
                isBatteryOptimizationLoaded &&
                isBatchLoaded &&
                isBootManagerLoaded &&
                isNotesLoaded &&
                isTagsLoaded
    }

    private fun checkForPermission(): Boolean {
        val appOps = requireContext().getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), requireContext().packageName)
        } else {
            @Suppress("Deprecation")
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), requireContext().packageName)
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            mode == AppOpsManagerCompat.MODE_ALLOWED && Environment.isExternalStorageManager()
        } else {
            mode == AppOpsManagerCompat.MODE_ALLOWED &&
                    (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        }
    }

    private fun unlockStateChecker() {
        launcherViewModel.getHasValidCertificate().observe(viewLifecycleOwner) { it ->
            if (it) {
                if (TrialPreferences.isFullVersion().invert()) {
                    kotlin.runCatching {
                        if (TrialPreferences.setFullVersion(value = true)) {
                            // showWarning(R.string.full_version_activated, goBack = false)
                            TrialPreferences.resetUnlockerWarningCount()
                            daysLeft.gone()
                        }
                    }.getOrElse {
                        it.printStackTrace()
                    }
                }
            } else {
                // showWarning(Warnings.getInvalidUnlockerWarning(), goBack = false)
                TrialPreferences.setFullVersion(false)
                TrialPreferences.resetUnlockerWarningCount()
            }
        }

        if (TrialPreferences.isTrialWithoutFull()) {
            if (TrialPreferences.isFullVersion()) {
                daysLeft.gone()
                TrialPreferences.resetUnlockerWarningCount()
            } else {
                daysLeft.text = getString(R.string.days_trial_period_remaining, TrialPreferences.getDaysLeft())
                TrialPreferences.resetUnlockerWarningCount()
            }
        } else if (TrialPreferences.isFullVersion()) {
            if (TrialPreferences.hasLicenceKey() && TrialPreferences.isUnlockerVerificationRequired().invert()) {
                Log.d(TAG, "Licence key mode")
                daysLeft.gone()
            } else {
                if (requirePackageManager().isPackageInstalled(AppUtils.unlockerPackageName)) {
                    daysLeft.gone()
                } else {
                    if (TrialPreferences.getUnlockerWarningCount() < 3) {
                        showWarning(R.string.unlocker_not_installed, goBack = false)
                        TrialPreferences.incrementUnlockerWarningCount()
                        daysLeft.gone()
                    } else {
                        showWarning(R.string.full_version_deactivated, goBack = false)
                        TrialPreferences.setFullVersion(false)
                        TrialPreferences.resetUnlockerWarningCount()
                        daysLeft.text = getString(R.string.days_trial_period_remaining, TrialPreferences.getDaysLeft())
                    }
                }
            }
        } else {
            // Should always be 0
            daysLeft.text = getString(R.string.days_trial_period_remaining, TrialPreferences.getDaysLeft())
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(broadcastReceiver!!, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            requireContext().unbindService(serviceConnection!!)
        } catch (e: java.lang.IllegalArgumentException) {
            e.printStackTrace() // Should crash if moving to another [Setup] fragment
        }

        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(broadcastReceiver!!)
    }

    companion object {
        fun newInstance(skip: Boolean): SplashScreen {
            val args = Bundle()
            args.putBoolean(BundleConstants.skip, skip)
            val fragment = SplashScreen()
            fragment.arguments = args
            return fragment
        }

        private const val TAG = "Splash Screen"
    }
}
