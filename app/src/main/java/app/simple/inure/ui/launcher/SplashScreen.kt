package app.simple.inure.ui.launcher

import android.Manifest
import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Process
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.core.app.AppOpsManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import app.simple.inure.R
import app.simple.inure.constants.Misc
import app.simple.inure.crash.CrashReporter
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.LoaderImageView
import app.simple.inure.dialogs.app.FullVersionReminder.Companion.showFullVersionReminder
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.preferences.*
import app.simple.inure.ui.panels.Home
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.viewmodels.panels.*
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
    private var isUninstalledPackagesLoaded = false
    private var isDisabledPackagesLoaded = false
    private var isRecentlyInstalledLoaded = false
    private var isRecentlyUpdatedLoaded = false
    private var isFrequentlyUsedLoaded = false
    private var isBatteryOptimizationLoaded = false
    private var isBootManagerLoaded = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_splash_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startPostponedEnterTransition()

        icon = view.findViewById(R.id.imageView)
        loaderImageView = view.findViewById(R.id.loader)
        daysLeft = view.findViewById(R.id.days_left)

        if (MainPreferences.isWithinTrialPeriod()) {
            if (MainPreferences.isFullVersion()) {
                daysLeft.gone()
            } else {
                daysLeft.text = getString(R.string.days_trial_period_remaining, MainPreferences.getDaysLeft())

                if (MainPreferences.getLaunchCount() % 5 == 0) {
                    parentFragmentManager.showFullVersionReminder()
                }
            }
        } else if (MainPreferences.isFullVersion()) {
            daysLeft.gone()
        } else {
            // Should always be 0
            daysLeft.text = getString(R.string.days_trial_period_remaining, MainPreferences.getDaysLeft())

            if (MainPreferences.getLaunchCount() % 5 == 0) {
                parentFragmentManager.showFullVersionReminder()
            }
        }

        if (AccessibilityPreferences.isAnimationReduced().invert()) {
            icon.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.app_icon_animation))
        }

        if (BehaviourPreferences.isSkipLoadingMainScreenState()) {
            loaderImageView.alpha = 0F
        }

        // (icon.drawable as AnimatedVectorDrawable).start()

        viewLifecycleOwner.lifecycleScope.launch {
            delay(Misc.delay)

            // Initialize native crash handler
            if (DevelopmentPreferences.get(DevelopmentPreferences.crashHandler)) {
                CrashReporter(requireContext()).initialize()
            }

            when {
                requireArguments().getBoolean("skip") -> {
                    proceed()
                }
                !checkForPermission() -> {
                    openFragmentSlide(Setup.newInstance())
                }
                else -> {
                    proceed()
                }
            }
        }
    }

    private fun proceed() {
        val appsViewModel = ViewModelProvider(requireActivity())[AppsViewModel::class.java]
        val usageStatsData = ViewModelProvider(requireActivity())[UsageStatsViewModel::class.java]
        val sensorsViewModel = ViewModelProvider(requireActivity())[SensorsViewModel::class.java]
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

        searchViewModel.getDeepSearchData().observe(viewLifecycleOwner) {
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

        bootManagerViewModel?.getBootComponentData()?.observe(viewLifecycleOwner) {
            Log.d(TAG, "Boot manager data loaded in ${(System.currentTimeMillis() - startTime) / 1000} seconds")
            isBootManagerLoaded = true
            openApp()
        }

        if (BehaviourPreferences.isSkipLoadingMainScreenState()) {
            openFragmentArc(Home.newInstance(), icon)
        }
    }

    private fun openApp() {
        if (BehaviourPreferences.isSkipLoadingMainScreenState()) return
        if (isEverythingLoaded()) {
            openFragmentArc(Home.newInstance(), icon)
        }
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
                isBootManagerLoaded
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

    companion object {
        fun newInstance(skip: Boolean): SplashScreen {
            val args = Bundle()
            args.putBoolean("skip", skip)
            val fragment = SplashScreen()
            fragment.arguments = args
            return fragment
        }

        private const val TAG = "Splash Screen"
    }
}
