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
import app.simple.inure.decorations.views.LoaderImageView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.preferences.AccessibilityPreferences
import app.simple.inure.preferences.BehaviourPreferences
import app.simple.inure.ui.app.Home
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.viewmodels.panels.AppsViewModel
import app.simple.inure.viewmodels.panels.HomeViewModel
import app.simple.inure.viewmodels.panels.SearchViewModel
import app.simple.inure.viewmodels.panels.UsageStatsViewModel
import app.simple.inure.viewmodels.viewers.SensorsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashScreen : ScopedFragment() {

    private lateinit var icon: ImageView
    private lateinit var loaderImageView: LoaderImageView

    private var isAppDataLoaded = false
    private var isUsageDataLoaded = false
    private var areSensorsLoaded = false
    private var isSearchLoaded = false
    private var isUninstalledPackagesLoaded = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_splash_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startPostponedEnterTransition()

        icon = view.findViewById(R.id.imageView)
        loaderImageView = view.findViewById(R.id.loader)

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
            CrashReporter(requireContext()).initialize()

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

        appsViewModel.getAppData().observe(viewLifecycleOwner) {
            isAppDataLoaded = true
            openApp()
        }

        usageStatsData.usageData.observe(viewLifecycleOwner) {
            isUsageDataLoaded = true
            openApp()
        }

        sensorsViewModel.getSensorsData().observe(viewLifecycleOwner) {
            areSensorsLoaded = true
            openApp()
        }

        searchViewModel.getSearchData().observe(viewLifecycleOwner) {
            isSearchLoaded = true
            openApp()
        }

        searchViewModel.getDeepSearchData().observe(viewLifecycleOwner) {
            isSearchLoaded = true
            openApp()
        }

        homeViewModel.getUninstalledPackages().observe(viewLifecycleOwner) {
            isUninstalledPackagesLoaded = true
            openApp()
        }

        if (BehaviourPreferences.isSkipLoadingMainScreenState()) {
            openFragmentArc(Home.newInstance(), icon)
        }
    }

    private fun openApp() {
        if (BehaviourPreferences.isSkipLoadingMainScreenState()) return
        if (isAppDataLoaded && isUsageDataLoaded && areSensorsLoaded && isSearchLoaded && isUninstalledPackagesLoaded) {
            openFragmentArc(Home.newInstance(), icon)
        }
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
    }
}
