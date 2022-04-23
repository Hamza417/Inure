package app.simple.inure.ui.launcher

import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.content.Context
import android.os.Bundle
import android.os.Process
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.core.app.AppOpsManagerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import app.simple.inure.R
import app.simple.inure.decorations.views.LoaderImageView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.preferences.BehaviourPreferences
import app.simple.inure.preferences.MainPreferences
import app.simple.inure.ui.app.Home
import app.simple.inure.util.FragmentHelper.openFragment
import app.simple.inure.util.PermissionUtils.arePermissionsGranted
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
    private lateinit var loader: LoaderImageView

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
        loader = view.findViewById(R.id.loader)

        icon.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.app_icon_animation))

        if (BehaviourPreferences.isSkipLoadingMainScreenState()) {
            loader.alpha = 0F
        }

        // (icon.drawable as AnimatedVectorDrawable).start()

        viewLifecycleOwner.lifecycleScope.launch {
            delay(500)

            when {
                requireArguments().getBoolean("skip") -> {
                    proceed()
                }
                !checkForPermission() -> {
                    openFragment(requireActivity().supportFragmentManager,
                                 Setup.newInstance(), icon)
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

        homeViewModel.getUninstalledPackages().observe(viewLifecycleOwner) {
            isUninstalledPackagesLoaded = true
            openApp()
        }

        if (BehaviourPreferences.isSkipLoadingMainScreenState()) {
            openFragment(
                    requireActivity().supportFragmentManager,
                    Home.newInstance(),
                    requireView().findViewById(R.id.imageView))
        }
    }

    private fun openApp() {
        if (BehaviourPreferences.isSkipLoadingMainScreenState()) return
        if (isAppDataLoaded && isUsageDataLoaded && areSensorsLoaded && isSearchLoaded && isUninstalledPackagesLoaded) {
            openFragment(requireActivity().supportFragmentManager,
                         Home.newInstance(),
                         requireView().findViewById(R.id.imageView))
        }
    }

    private fun checkForPermission(): Boolean {
        val appOps = requireContext().getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), requireContext().packageName)
        } else {
            @Suppress("Deprecation")
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), requireContext().packageName)
        }

        return mode == AppOpsManagerCompat.MODE_ALLOWED && requireContext().arePermissionsGranted(MainPreferences.getStoragePermissionUri())
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