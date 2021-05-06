package app.simple.inure.ui.app

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.WindowManager
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.viewModels
import app.simple.inure.R
import app.simple.inure.adapters.ui.SearchAdapter
import app.simple.inure.decorations.popup.PopupLinearLayout
import app.simple.inure.decorations.popup.PopupMenuCallback
import app.simple.inure.decorations.searchview.SearchView
import app.simple.inure.decorations.searchview.SearchViewEventListener
import app.simple.inure.decorations.transitions.DetailsTransitionArc
import app.simple.inure.decorations.transitions.TransitionManager
import app.simple.inure.decorations.views.CustomRecyclerView
import app.simple.inure.dialogs.app.AppsListConfiguration
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.interfaces.adapters.AppsAdapterCallbacks
import app.simple.inure.popups.app.MainListPopupMenu
import app.simple.inure.preferences.MainPreferences
import app.simple.inure.util.FragmentHelper
import app.simple.inure.util.PackageUtils.killThisApp
import app.simple.inure.util.PackageUtils.launchThisPackage
import app.simple.inure.util.PackageUtils.uninstallThisPackage
import app.simple.inure.util.StatusBarHeight
import app.simple.inure.viewmodels.SearchData

class SearchPanel : ScopedFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var searchView: SearchView
    private lateinit var recyclerView: CustomRecyclerView
    private lateinit var appsAdapterSmall: SearchAdapter
    private lateinit var appUninstallObserver: ActivityResultLauncher<Intent>

    private val searchData: SearchData by viewModels()

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

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchData.getSearchData().observe(requireActivity(), {
            postponeEnterTransition()

            appsAdapterSmall.apps = it
            appsAdapterSmall.searchKeyword = searchData.getSearchKeywords().value!!

            recyclerView.adapter = appsAdapterSmall

            appsAdapterSmall.setOnItemClickListener(object : AppsAdapterCallbacks {
                override fun onAppClicked(applicationInfo: ApplicationInfo, icon: ImageView) {
                    openAppInfo(applicationInfo, icon)
                }

                override fun onAppLongPress(applicationInfo: ApplicationInfo, viewGroup: ViewGroup, xOff: Float, yOff: Float, icon: ImageView) {
                    val popupMenu = MainListPopupMenu(layoutInflater.inflate(R.layout.popup_main_list, PopupLinearLayout(requireContext()), true),
                                                      viewGroup, xOff, yOff, applicationInfo, icon)
                    popupMenu.setOnMenuItemClickListener(object : PopupMenuCallback {
                        override fun onMenuItemClicked(source: String, applicationInfo: ApplicationInfo, icon: ImageView) {
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
                                    applicationInfo.uninstallThisPackage(appUninstallObserver)
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

        appUninstallObserver = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    /**
                     * App uninstalled,
                     * Tell the viewModel to re-fetch the updated list
                     */
                    searchData.loadSearchData()
                }
                Activity.RESULT_CANCELED -> {
                    /* no-op */
                }
            }
        }

        searchView.setSearchViewEventListener(object : SearchViewEventListener {
            override fun onSearchMenuPressed(button: View) {
                AppsListConfiguration.newInstance()
                        .show(childFragmentManager, "apps_list_config")
            }

            override fun onSearchTextChanged(keywords: String, count: Int) {
                searchData.setSearchKeywords(keywords)
                searchData.loadSearchData()
            }
        })
    }

    private fun openAppInfo(applicationInfo: ApplicationInfo, icon: ImageView) {
        FragmentHelper.openFragmentLinear(requireActivity().supportFragmentManager,
                                          AppInfo.newInstance(applicationInfo, icon.transitionName),
                                          icon, "app_info")
    }

    companion object {
        fun newInstance(): SearchPanel {
            val args = Bundle()
            val fragment = SearchPanel()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            MainPreferences.sortStyle,
            MainPreferences.isSortingReversed,
            MainPreferences.listAppsCategory,
            -> {
                searchData.loadSearchData()
            }
        }
    }
}