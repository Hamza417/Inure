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
import app.simple.inure.adapters.home.AdapterUninstalled
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.dialogs.menus.AppsMenu
import app.simple.inure.dialogs.miscellaneous.UninstallInfo
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.interfaces.adapters.AdapterCallbacks
import app.simple.inure.viewmodels.panels.HomeViewModel

class Uninstalled : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private var adapterUninstalled: AdapterUninstalled? = null
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_uninstalled, container, false)

        recyclerView = view.findViewById(R.id.uninstalled_recycler_view)
        adapterUninstalled = AdapterUninstalled()

        homeViewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeViewModel.getUninstalledPackages().observe(viewLifecycleOwner) {
            postponeEnterTransition()

            adapterUninstalled?.apps = it
            recyclerView.adapter = adapterUninstalled

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }

            adapterUninstalled?.setOnItemClickListener(object : AdapterCallbacks {
                override fun onAppClicked(packageInfo: PackageInfo, icon: ImageView) {
                    openAppInfo(packageInfo, icon)
                }

                override fun onAppLongPressed(packageInfo: PackageInfo, icon: ImageView) {
                    AppsMenu.newInstance(packageInfo)
                        .show(childFragmentManager, "apps_menu")
                }
            })

            bottomRightCornerMenu?.initBottomMenuWithRecyclerView(arrayListOf(R.drawable.ic_info, R.drawable.ic_settings, -1, R.drawable.ic_search), recyclerView) { id, _ ->
                when (id) {
                    R.drawable.ic_settings -> {
                        openFragmentSlide(Preferences.newInstance(), "prefs_screen")
                    }
                    R.drawable.ic_search -> {
                        openFragmentSlide(Search.newInstance(true), "search")
                    }
                    R.drawable.ic_info -> {
                        UninstallInfo.newInstance()
                            .show(childFragmentManager, "uninstall_info")
                    }
                }
            }
        }
    }

    companion object {
        fun newInstance(): Uninstalled {
            val args = Bundle()
            val fragment = Uninstalled()
            fragment.arguments = args
            return fragment
        }
    }
}