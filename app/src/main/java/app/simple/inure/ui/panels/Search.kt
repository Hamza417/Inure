package app.simple.inure.ui.panels

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.viewModels
import app.simple.inure.R
import app.simple.inure.adapters.ui.SearchAdapter
import app.simple.inure.apk.utils.PackageUtils.isPackageInstalled
import app.simple.inure.apk.utils.PackageUtils.killThisApp
import app.simple.inure.apk.utils.PackageUtils.launchThisPackage
import app.simple.inure.apk.utils.PackageUtils.uninstallThisPackage
import app.simple.inure.decorations.popup.PopupLinearLayout
import app.simple.inure.decorations.popup.PopupMenuCallback
import app.simple.inure.decorations.searchview.SearchView
import app.simple.inure.decorations.searchview.SearchViewEventListener
import app.simple.inure.decorations.views.CustomVerticalRecyclerView
import app.simple.inure.dialogs.app.AppsListConfiguration
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.interfaces.adapters.AppsAdapterCallbacks
import app.simple.inure.popups.app.PopupMainList
import app.simple.inure.preferences.MainPreferences
import app.simple.inure.ui.app.AppInfo
import app.simple.inure.util.FragmentHelper
import app.simple.inure.util.StatusBarHeight
import app.simple.inure.viewmodels.panels.SearchData
import java.util.*

class Search : ScopedFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var searchView: SearchView
    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var appsAdapterSmall: SearchAdapter

    private val searchModel: SearchData by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = layoutInflater.inflate(R.layout.fragment_search, container, false)

        searchView = view.findViewById(R.id.search_view)
        recyclerView = view.findViewById(R.id.search_recycler_view)
        appsAdapterSmall = SearchAdapter()

        val params = searchView.layoutParams as MarginLayoutParams
        params.setMargins(params.leftMargin,
                          StatusBarHeight.getStatusBarHeight(resources) + params.topMargin,
                          params.rightMargin,
                          params.bottomMargin)

        recyclerView.setPadding(recyclerView.paddingLeft,
                                recyclerView.paddingTop + params.topMargin + params.height + params.bottomMargin,
                                recyclerView.paddingRight,
                                recyclerView.paddingBottom)

        if (requireArguments().getBoolean("first_launch")) {
            startPostponedEnterTransition()
            requireArguments().putBoolean("first_launch", false)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchModel.getSearchData().observe(viewLifecycleOwner, {
            postponeEnterTransition()

            for (i in it.indices) {
                if (!it[i].isPackageInstalled(requireActivity().packageManager)) {
                    searchModel.loadSearchData()
                    return@observe
                }
            }

            appsAdapterSmall.apps = it
            appsAdapterSmall.searchKeyword = searchModel.getSearchKeywords().value!!

            recyclerView.adapter = appsAdapterSmall

            appsAdapterSmall.setOnItemClickListener(object : AppsAdapterCallbacks {
                override fun onAppClicked(applicationInfo: ApplicationInfo, icon: ImageView) {
                    openAppInfo(applicationInfo, icon)
                }

                override fun onAppLongPress(applicationInfo: ApplicationInfo, anchor: View, icon: ImageView, position: Int) {
                    val popupMenu = PopupMainList(layoutInflater.inflate(R.layout.popup_main_list, PopupLinearLayout(requireContext()), true),
                                                  applicationInfo, anchor)
                    popupMenu.setOnMenuItemClickListener(object : PopupMenuCallback {
                        override fun onMenuItemClicked(source: String, applicationInfo: ApplicationInfo) {
                            when (source) {
                                getString(R.string.app_information) -> {
                                    openAppInfo(applicationInfo, icon)
                                }
                                getString(R.string.launch) -> {
                                    applicationInfo.launchThisPackage(requireActivity())
                                }
                                getString(R.string.kill) -> {
                                    applicationInfo.killThisApp(requireActivity())
                                }
                                getString(R.string.uninstall) -> {
                                    applicationInfo.uninstallThisPackage(appUninstallObserver, position)
                                }
                            }
                        }
                    })
                }
            })

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }
        })

        searchView.setSearchViewEventListener(object : SearchViewEventListener {
            override fun onSearchMenuPressed(button: View) {
                AppsListConfiguration.newInstance()
                        .show(childFragmentManager, "apps_list_config")
            }

            override fun onSearchTextChanged(keywords: String, count: Int) {
                searchModel.setSearchKeywords(keywords)
                searchModel.loadSearchData()
            }
        })
    }

    private fun openAppInfo(applicationInfo: ApplicationInfo, icon: ImageView) {
        FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                    AppInfo.newInstance(applicationInfo, icon.transitionName),
                                    icon, "app_info_by_search")
    }

    override fun onAppUninstalled(result: Boolean, data: Intent?) {
        if (result) {
            appsAdapterSmall.notifyItemRemoved(data!!.getIntExtra("position", -1))
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            MainPreferences.sortStyle,
            MainPreferences.isSortingReversed,
            MainPreferences.listAppsCategory,
            -> {
                searchModel.loadSearchData()
            }
        }
    }

    companion object {
        fun newInstance(firstLaunch: Boolean): Search {
            val args = Bundle()
            args.putBoolean("first_launch", firstLaunch)
            val fragment = Search()
            fragment.arguments = args
            return fragment
        }
    }
}