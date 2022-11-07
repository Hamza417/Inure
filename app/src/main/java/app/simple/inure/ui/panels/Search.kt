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
import app.simple.inure.adapters.ui.AdapterDeepSearch
import app.simple.inure.adapters.ui.AdapterSearch
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.decorations.searchview.SearchView
import app.simple.inure.decorations.searchview.SearchViewEventListener
import app.simple.inure.dialogs.menus.AppsMenu
import app.simple.inure.dialogs.menus.SearchMenu
import app.simple.inure.extensions.fragments.KeyboardScopedFragment
import app.simple.inure.interfaces.adapters.AdapterCallbacks
import app.simple.inure.preferences.SearchPreferences
import app.simple.inure.viewmodels.panels.SearchViewModel

class Search : KeyboardScopedFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var searchView: SearchView
    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var appsAdapterSearchSmall: AdapterSearch
    private lateinit var adapterDeepSearch: AdapterDeepSearch

    private lateinit var searchViewModel: SearchViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = layoutInflater.inflate(R.layout.fragment_search, container, false)

        searchView = view.findViewById(R.id.search_view)
        recyclerView = view.findViewById(R.id.search_recycler_view)

        if (requireArguments().getBoolean(BundleConstants.firstLaunch)) {
            startPostponedEnterTransition()
            requireArguments().putBoolean(BundleConstants.firstLaunch, false)
        }

        searchViewModel = ViewModelProvider(requireActivity())[SearchViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchView.editText.setWindowInsetsAnimationCallback()
        searchView.showInput()

        searchViewModel.getSearchData().observe(viewLifecycleOwner) {
            if (!SearchPreferences.isDeepSearchEnabled()) {
                postponeEnterTransition()
                searchView.hideLoader()
                searchView.setNewNumber(it.size)

                appsAdapterSearchSmall = AdapterSearch(it, searchViewModel.getSearchKeywords().value ?: "")
                recyclerView.adapter = appsAdapterSearchSmall

                appsAdapterSearchSmall.setOnItemClickListener(object : AdapterCallbacks {
                    override fun onAppClicked(packageInfo: PackageInfo, icon: ImageView) {
                        openAppInfo(packageInfo, icon)
                    }

                    override fun onAppLongPressed(packageInfo: PackageInfo, icon: ImageView) {
                        AppsMenu.newInstance(packageInfo)
                            .show(childFragmentManager, "apps_menu")
                    }
                })

                (view.parent as? ViewGroup)?.doOnPreDraw {
                    startPostponedEnterTransition()
                }
            }
        }

        searchViewModel.getDeepSearchData().observe(viewLifecycleOwner) {
            if (SearchPreferences.isDeepSearchEnabled()) {
                postponeEnterTransition()

                searchView.setNewNumber(it.size)
                searchView.hideLoader()

                adapterDeepSearch = AdapterDeepSearch(it, searchViewModel.getSearchKeywords().value ?: "")
                recyclerView.adapter = adapterDeepSearch

                adapterDeepSearch.setOnItemClickListener(object : AdapterCallbacks {
                    override fun onAppClicked(packageInfo: PackageInfo, icon: ImageView) {
                        openAppInfo(packageInfo, icon)
                    }

                    override fun onAppLongPressed(packageInfo: PackageInfo, icon: ImageView) {
                        AppsMenu.newInstance(packageInfo)
                            .show(childFragmentManager, "apps_menu")
                    }
                })

                (view.parent as? ViewGroup)?.doOnPreDraw {
                    startPostponedEnterTransition()
                }
            }
        }

        searchView.setSearchViewEventListener(object : SearchViewEventListener {
            override fun onSearchMenuPressed(button: View) {
                SearchMenu.newInstance()
                    .show(childFragmentManager, "search_list_config")
            }

            override fun onSearchTextChanged(keywords: String, count: Int) {
                searchViewModel.setSearchKeywords(keywords)
            }
        })
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            SearchPreferences.sortStyle,
            SearchPreferences.isSortingReversed,
            SearchPreferences.listAppsCategory,
            SearchPreferences.deepSearch -> {
                searchViewModel.initiateSearch(SearchPreferences.getLastSearchKeyword())
            }
            SearchPreferences.ignoreCasing -> {
                if (SearchPreferences.isDeepSearchEnabled()) {
                    adapterDeepSearch.ignoreCasing = SearchPreferences.isCasingIgnored()
                } else {
                    appsAdapterSearchSmall.ignoreCasing = SearchPreferences.isCasingIgnored()
                }
                searchViewModel.initiateSearch(SearchPreferences.getLastSearchKeyword())
            }
        }
    }

    companion object {
        fun newInstance(firstLaunch: Boolean): Search {
            val args = Bundle()
            args.putBoolean(BundleConstants.firstLaunch, firstLaunch)
            val fragment = Search()
            fragment.arguments = args
            return fragment
        }
    }
}