package app.simple.inure.ui.panels

import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.tags.AdapterUntaggedApps
import app.simple.inure.constants.BottomMenuConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.dialogs.app.AppMenu
import app.simple.inure.dialogs.apps.UntaggedSort.Companion.showUntaggedSortDialog
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.interfaces.adapters.AdapterCallbacks
import app.simple.inure.preferences.UntaggedPreferences
import app.simple.inure.viewmodels.panels.UntaggedViewModel

class Untagged : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var untaggedViewModel: UntaggedViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_untagged, container, false)

        recyclerView = view.findViewById(R.id.recycler_view)

        untaggedViewModel = ViewModelProvider(requireActivity())[UntaggedViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        if (untaggedViewModel.shouldShowLoader()) {
            showLoader(manualOverride = true)
        }

        untaggedViewModel.getUntaggedApps().observe(viewLifecycleOwner) {
            hideLoader()

            val adapterUntaggedApps = AdapterUntaggedApps(it)
            recyclerView.adapter = adapterUntaggedApps

            adapterUntaggedApps.setOnAdapterCallbacks(object : AdapterCallbacks {
                override fun onAppClicked(packageInfo: PackageInfo, icon: ImageView) {
                    openAppInfo(packageInfo, icon)
                }

                override fun onAppLongPressed(packageInfo: PackageInfo, icon: ImageView) {
                    AppMenu.newInstance(packageInfo)
                        .show(childFragmentManager, AppMenu.TAG)
                }
            })

            bottomRightCornerMenu?.initBottomMenuWithRecyclerView(
                    BottomMenuConstants.getAllAppsBottomMenuItems(), recyclerView) { id, _ ->
                when (id) {
                    R.drawable.ic_filter -> {
                        childFragmentManager.showUntaggedSortDialog()
                    }
                    R.drawable.ic_search -> {
                        openFragmentSlide(Search.newInstance(true), Search.TAG)
                    }
                    R.drawable.ic_refresh -> {
                        showLoader(true)
                        untaggedViewModel.loadUntaggedApps()
                    }
                    R.drawable.ic_settings -> {
                        openFragmentSlide(Preferences.newInstance(), Preferences.TAG)
                    }
                }
            }

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            UntaggedPreferences.SORT_STYLE,
            UntaggedPreferences.IS_SORTING_REVERSED,
            UntaggedPreferences.APPS_TYPE -> {
                untaggedViewModel.loadUntaggedApps()
            }
        }
    }

    companion object {
        fun newInstance(): Untagged {
            val args = Bundle()
            val fragment = Untagged()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "untagged"
    }
}
