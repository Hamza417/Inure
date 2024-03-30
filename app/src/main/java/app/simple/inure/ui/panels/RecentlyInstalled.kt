package app.simple.inure.ui.panels

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.home.AdapterRecentlyInstalled
import app.simple.inure.constants.BottomMenuConstants
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.dialogs.menus.AppsMenu
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.interfaces.adapters.AdapterCallbacks
import app.simple.inure.viewmodels.panels.HomeViewModel

class RecentlyInstalled : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private var adapterRecentlyInstalled: AdapterRecentlyInstalled? = null
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_recently_installed, container, false)

        recyclerView = view.findViewById(R.id.recently_installed_recycler_view)
        adapterRecentlyInstalled = AdapterRecentlyInstalled()

        homeViewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showLoader()

        homeViewModel.getRecentlyInstalled().observe(viewLifecycleOwner) {
            postponeEnterTransition()
            hideLoader()

            adapterRecentlyInstalled?.apps = it
            recyclerView.adapter = adapterRecentlyInstalled

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }

            adapterRecentlyInstalled?.setOnItemClickListener(object : AdapterCallbacks {
                override fun onAppClicked(packageInfo: PackageInfo, icon: ImageView) {
                    openAppInfo(packageInfo, icon)
                }

                override fun onAppLongPressed(packageInfo: PackageInfo, icon: ImageView) {
                    AppsMenu.newInstance(packageInfo)
                        .show(childFragmentManager, "apps_menu")
                }
            })

            bottomRightCornerMenu?.initBottomMenuWithRecyclerView(BottomMenuConstants.getGenericBottomMenuItems(), recyclerView) { id, _ ->
                when (id) {
                    R.drawable.ic_settings -> {
                        openFragmentSlide(Preferences.newInstance(), "prefs_screen")
                    }
                    R.drawable.ic_search -> {
                        openFragmentSlide(Search.newInstance(true), "search")
                    }
                    R.drawable.ic_refresh -> {
                        showLoader(manualOverride = true)
                        homeViewModel.refreshPackageData()
                    }
                }
            }
        }
    }

    companion object {
        fun newInstance(loading: Boolean = false): RecentlyInstalled {
            val args = Bundle()
            val fragment = RecentlyInstalled()
            args.putBoolean(BundleConstants.loading, loading)
            fragment.arguments = args
            return fragment
        }

        const val TAG = "recently_installed"
    }
}
