package app.simple.inure.ui.app

import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.viewModels
import app.simple.inure.R
import app.simple.inure.adapters.SearchAdapter
import app.simple.inure.decorations.corners.DynamicCornerLinearLayout
import app.simple.inure.decorations.popup.PopupMenuCallback
import app.simple.inure.decorations.searchview.SearchView
import app.simple.inure.decorations.searchview.SearchViewEventListener
import app.simple.inure.decorations.transitions.DetailsTransitionArc
import app.simple.inure.decorations.transitions.TransitionManager
import app.simple.inure.decorations.views.CustomRecyclerView
import app.simple.inure.dialogs.AppsListConfiguration
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.interfaces.adapters.AppsAdapterCallbacks
import app.simple.inure.packagehelper.PackageUtils.killThisApp
import app.simple.inure.packagehelper.PackageUtils.launchThisPackage
import app.simple.inure.packagehelper.PackageUtils.uninstallThisPackage
import app.simple.inure.popups.MainListPopupMenu
import app.simple.inure.preferences.MainPreferences
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences
import app.simple.inure.viewmodels.SearchData

class SearchPanel : ScopedFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var searchView: SearchView
    private lateinit var recyclerView: CustomRecyclerView
    private lateinit var appsAdapterSmall: SearchAdapter

    private val searchData: SearchData by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = layoutInflater.inflate(R.layout.fragment_search, container, false)

        searchView = view.findViewById(R.id.search_view)
        recyclerView = view.findViewById(R.id.search_recycler_view)
        appsAdapterSmall = SearchAdapter()

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
                    val popupMenu = MainListPopupMenu(layoutInflater.inflate(R.layout.popup_main_list, DynamicCornerLinearLayout(requireContext(), null, 0), true),
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
                                    applicationInfo.uninstallThisPackage(requireActivity())
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
                searchData.setSearchKeywords(keywords)
                searchData.loadSearchData()
            }
        })
    }

    private fun openAppInfo(applicationInfo: ApplicationInfo, icon: ImageView) {
        val appInfo = requireActivity().supportFragmentManager.findFragmentByTag("app_info")
            ?: AppInfo.newInstance(applicationInfo, icon.transitionName)

        exitTransition = TransitionManager.getEnterTransitions(TransitionManager.FADE)
        appInfo.sharedElementEnterTransition = DetailsTransitionArc(1.5F)
        appInfo.enterTransition = TransitionManager.getExitTransition(TransitionManager.FADE)
        appInfo.sharedElementReturnTransition = DetailsTransitionArc(1.2F)

        requireActivity().supportFragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .addSharedElement(icon, icon.transitionName)
                .replace(R.id.app_container, appInfo, "app_info").addToBackStack("app_info").commit()
    }

    companion object {
        fun newInstance(): SearchPanel {
            val args = Bundle()
            val fragment = SearchPanel()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
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