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
import app.simple.inure.adapters.ui.AdapterSearch
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.decorations.searchview.SearchView
import app.simple.inure.decorations.searchview.SearchViewEventListener
import app.simple.inure.dialogs.app.AppsMenu
import app.simple.inure.dialogs.app.SearchMenu
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.interfaces.adapters.AppsAdapterCallbacks
import app.simple.inure.preferences.SearchPreferences
import app.simple.inure.ui.app.AppInfo
import app.simple.inure.util.FragmentHelper
import app.simple.inure.viewmodels.panels.SearchViewModel

class Search : ScopedFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var searchView: SearchView
    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var appsAdapterSearchSmall: AdapterSearch

    private lateinit var searchViewModel: SearchViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = layoutInflater.inflate(R.layout.fragment_search, container, false)

        searchView = view.findViewById(R.id.search_view)
        recyclerView = view.findViewById(R.id.search_recycler_view)

        if (requireArguments().getBoolean("first_launch")) {
            startPostponedEnterTransition()
            requireArguments().putBoolean("first_launch", false)
        }

        searchViewModel = ViewModelProvider(requireActivity())[SearchViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchViewModel.getSearchData().observe(viewLifecycleOwner) {
            postponeEnterTransition()

            searchView.setNewNumber(it.size)

            appsAdapterSearchSmall = AdapterSearch(it, searchViewModel.getSearchKeywords().value ?: "")
            recyclerView.adapter = appsAdapterSearchSmall

            appsAdapterSearchSmall.setOnItemClickListener(object : AppsAdapterCallbacks {
                override fun onAppClicked(packageInfo: PackageInfo, icon: ImageView) {
                    openAppInfo(packageInfo, icon)
                }

                override fun onAppLongPress(packageInfo: PackageInfo, anchor: View, icon: ImageView, position: Int) {
                    AppsMenu.newInstance(packageInfo)
                        .show(childFragmentManager, "apps_menu")
                }
            })

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }
        }

        searchView.setSearchViewEventListener(object : SearchViewEventListener {
            override fun onSearchMenuPressed(button: View) {
                SearchMenu.newInstance()
                    .show(childFragmentManager, "search_list_config")
            }

            override fun onSearchTextChanged(keywords: String, count: Int) {
                searchViewModel.setSearchKeywords(keywords)
                searchViewModel.loadSearchData()
            }
        })
    }

    private fun openAppInfo(packageInfo: PackageInfo, icon: ImageView) {
        FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                    AppInfo.newInstance(packageInfo, icon.transitionName),
                                    icon, "app_info_by_search")
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            SearchPreferences.sortStyle,
            SearchPreferences.isSortingReversed,
            SearchPreferences.listAppsCategory,
            -> {
                searchViewModel.loadSearchData()
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