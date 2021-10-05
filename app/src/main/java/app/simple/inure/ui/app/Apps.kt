package app.simple.inure.ui.app

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.ui.AppsAdapterSmall
import app.simple.inure.decorations.popup.PopupMenuCallback
import app.simple.inure.decorations.views.CustomVerticalRecyclerView
import app.simple.inure.dialogs.app.AppsListConfiguration
import app.simple.inure.dialogs.miscellaneous.Preparing
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.interfaces.adapters.AppsAdapterCallbacks
import app.simple.inure.popups.app.PopupMainList
import app.simple.inure.preferences.MainPreferences
import app.simple.inure.ui.panels.Search
import app.simple.inure.util.FragmentHelper
import app.simple.inure.viewmodels.panels.AllAppsData

class Apps : ScopedFragment() {

    private lateinit var appsListRecyclerView: CustomVerticalRecyclerView
    private lateinit var appsAdapter: AppsAdapterSmall
    private lateinit var model: AllAppsData

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_all_apps, container, false)

        appsListRecyclerView = view.findViewById(R.id.all_apps_recycler_view)

        model = ViewModelProvider(requireActivity()).get(AllAppsData::class.java)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        model.getAppData().observe(viewLifecycleOwner, {
            postponeEnterTransition()

            appsAdapter = AppsAdapterSmall()
            appsAdapter.apps = it

            appsListRecyclerView.adapter = appsAdapter

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }

            appsAdapter.setOnItemClickListener(object : AppsAdapterCallbacks {
                override fun onAppClicked(applicationInfo: ApplicationInfo, icon: ImageView) {
                    openAppInfo(applicationInfo, icon)
                }

                override fun onAppLongPress(applicationInfo: ApplicationInfo, anchor: View, icon: ImageView, position: Int) {
                    val popupMenu = PopupMainList(anchor)
                    popupMenu.setOnMenuItemClickListener(object : PopupMenuCallback {
                        override fun onMenuItemClicked(source: String) {
                            when (source) {
                                getString(R.string.app_information) -> {
                                    openAppInfo(applicationInfo, icon)
                                }
                                getString(R.string.send) -> {
                                    Preparing.newInstance(applicationInfo)
                                            .show(parentFragmentManager, "send_app")
                                }
                            }
                        }
                    })
                }

                override fun onSearchPressed(view: View) {
                    clearEnterTransition()
                    clearExitTransition()
                    FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                Search.newInstance(true),
                                                "search")
                }

                override fun onFilterPressed() {
                    AppsListConfiguration.newInstance().show(childFragmentManager, "apps_list_config")
                }
            })
        })

        model.appLoaded.observe(viewLifecycleOwner, {
            it.getContentIfNotHandledOrReturnNull()?.let {
                Log.d("Apps", if(it) "Apps Loaded" else "Failed")
            }
        })

        super.onViewCreated(view, savedInstanceState)
    }

    private fun openAppInfo(applicationInfo: ApplicationInfo, icon: ImageView) {
        FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                    AppInfo.newInstance(applicationInfo, icon.transitionName),
                                    icon, "app_info")
    }

    override fun onAppUninstalled(result: Boolean, data: Intent?) {
        if (result) {
            appsAdapter.notifyItemRemoved(data!!.getIntExtra("position", -1))
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            MainPreferences.sortStyle,
            MainPreferences.isSortingReversed,
            MainPreferences.listAppsCategory,
            -> {
                model.loadAppData()
            }
        }
    }

    companion object {
        fun newInstance(): Apps {
            val args = Bundle()
            val fragment = Apps()
            fragment.arguments = args
            return fragment
        }
    }
}
