package app.simple.inure.ui.panels

import android.annotation.SuppressLint
import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.home.AdapterFOSS
import app.simple.inure.constants.BottomMenuConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.dialogs.app.AppMenu
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.interfaces.adapters.AdapterCallbacks
import app.simple.inure.viewmodels.panels.HomeViewModel

class FOSS : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private var adapterFOSS: AdapterFOSS? = null
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_foss, container, false)

        recyclerView = view.findViewById(R.id.foss_recycler_view)
        adapterFOSS = AdapterFOSS()

        homeViewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]

        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (homeViewModel.shouldShowFOSSLoader()) {
            showLoader(manualOverride = true)
        }

        homeViewModel.getFossApps().observe(viewLifecycleOwner) {
            hideLoader()
            postponeEnterTransition()

            adapterFOSS?.apps = it

            if (recyclerView.adapter == null) {
                recyclerView.adapter = adapterFOSS
            } else {
                adapterFOSS?.notifyDataSetChanged()
            }

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }

            adapterFOSS?.setOnItemClickListener(object : AdapterCallbacks {
                override fun onAppClicked(packageInfo: PackageInfo, icon: ImageView) {
                    openAppInfo(packageInfo, icon)
                }

                override fun onAppLongPressed(packageInfo: PackageInfo, icon: ImageView) {
                    AppMenu.newInstance(packageInfo)
                        .show(childFragmentManager, AppMenu.TAG)
                }
            })

            bottomRightCornerMenu?.initBottomMenuWithRecyclerView(BottomMenuConstants.getGenericBottomMenuItems(), recyclerView) { id, _ ->
                when (id) {
                    R.drawable.ic_settings -> {
                        openFragmentSlide(Preferences.newInstance(), Preferences.TAG)
                    }
                    R.drawable.ic_search -> {
                        openFragmentSlide(Search.newInstance(true), Search.TAG)
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
        fun newInstance(): FOSS {
            val args = Bundle()
            val fragment = FOSS()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "FOSS"
    }
}
