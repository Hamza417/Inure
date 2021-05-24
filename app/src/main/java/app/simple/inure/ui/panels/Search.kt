package app.simple.inure.ui.panels

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
import app.simple.inure.decorations.indicatorfastscroll.FastScrollItemIndicator
import app.simple.inure.decorations.indicatorfastscroll.FastScrollerThumbView
import app.simple.inure.decorations.indicatorfastscroll.FastScrollerView
import app.simple.inure.decorations.popup.PopupLinearLayout
import app.simple.inure.decorations.popup.PopupMenuCallback
import app.simple.inure.decorations.searchview.SearchView
import app.simple.inure.decorations.searchview.SearchViewEventListener
import app.simple.inure.decorations.viewholders.VerticalListViewHolder
import app.simple.inure.decorations.views.CustomRecyclerView
import app.simple.inure.dialogs.app.AppsListConfiguration
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.interfaces.adapters.AppsAdapterCallbacks
import app.simple.inure.popups.app.PopupMainList
import app.simple.inure.preferences.MainPreferences
import app.simple.inure.ui.app.AppInfo
import app.simple.inure.util.FragmentHelper
import app.simple.inure.util.PackageUtils.isPackageInstalled
import app.simple.inure.util.PackageUtils.killThisApp
import app.simple.inure.util.PackageUtils.launchThisPackage
import app.simple.inure.util.PackageUtils.uninstallThisPackage
import app.simple.inure.util.StatusBarHeight
import app.simple.inure.viewmodels.panels.SearchData
import java.util.*

class Search : ScopedFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var searchView: SearchView
    private lateinit var recyclerView: CustomRecyclerView
    private lateinit var appsAdapterSmall: SearchAdapter
    private lateinit var fastScrollerView: FastScrollerView
    private lateinit var scrollerThumb: FastScrollerThumbView

    private val searchModel: SearchData by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = layoutInflater.inflate(R.layout.fragment_search, container, false)

        searchView = view.findViewById(R.id.search_view)
        recyclerView = view.findViewById(R.id.search_recycler_view)
        fastScrollerView = view.findViewById(R.id.all_apps_fast_scroller)
        scrollerThumb = view.findViewById(R.id.all_apps_thumb)
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

            if (!fastScrollerView.isSetup) {
                fastScrollerView.setupWithRecyclerView(recyclerView, { position ->
                    if (position == VerticalListViewHolder.TYPE_HEADER) {
                        FastScrollItemIndicator.Icon(R.drawable.ic_search)
                    } else {
                        FastScrollItemIndicator.Text(it[position - 1].name.substring(0, 1)
                                                             .toUpperCase(Locale.ROOT))
                    }
                })

                scrollerThumb.setupWithFastScroller(fastScrollerView)
            }

            appsAdapterSmall.setOnItemClickListener(object : AppsAdapterCallbacks {
                override fun onAppClicked(applicationInfo: ApplicationInfo, icon: ImageView) {
                    openAppInfo(applicationInfo, icon)
                }

                override fun onAppLongPress(applicationInfo: ApplicationInfo, viewGroup: ViewGroup, xOff: Float, yOff: Float, icon: ImageView) {
                    val popupMenu = PopupMainList(layoutInflater.inflate(R.layout.popup_main_list, PopupLinearLayout(requireContext()), true),
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

    override fun onAppUninstalled(result: Boolean) {
        if (result) searchModel.loadSearchData()
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
        fun newInstance(): Search {
            val args = Bundle()
            val fragment = Search()
            fragment.arguments = args
            return fragment
        }
    }
}