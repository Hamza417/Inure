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
import app.simple.inure.adapters.details.AdapterTags
import app.simple.inure.adapters.ui.AdapterDeepSearch
import app.simple.inure.adapters.ui.AdapterSearch
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomHorizontalRecyclerView
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.decorations.searchview.SearchView
import app.simple.inure.decorations.searchview.SearchViewEventListener
import app.simple.inure.decorations.views.AppIconImageView
import app.simple.inure.dialogs.menus.AppsMenu.Companion.showAppsMenu
import app.simple.inure.dialogs.menus.SearchMenu
import app.simple.inure.extensions.fragments.KeyboardScopedFragment
import app.simple.inure.interfaces.adapters.AdapterCallbacks
import app.simple.inure.preferences.SearchPreferences
import app.simple.inure.ui.viewers.Activities
import app.simple.inure.ui.viewers.Permissions
import app.simple.inure.ui.viewers.Providers
import app.simple.inure.ui.viewers.Receivers
import app.simple.inure.ui.viewers.Resources
import app.simple.inure.ui.viewers.Services
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.panels.SearchViewModel
import app.simple.inure.viewmodels.panels.TagsViewModel

class Search : KeyboardScopedFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var searchView: SearchView
    private lateinit var tags: CustomHorizontalRecyclerView
    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var appsAdapterSearchSmall: AdapterSearch
    private lateinit var adapterDeepSearch: AdapterDeepSearch

    private lateinit var searchViewModel: SearchViewModel
    private lateinit var tagsViewModel: TagsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = layoutInflater.inflate(R.layout.fragment_search, container, false)

        searchView = view.findViewById(R.id.search_view)
        recyclerView = view.findViewById(R.id.search_recycler_view)
        tags = view.findViewById(R.id.tags_recycler_view)

        if (requireArguments().getBoolean(BundleConstants.firstLaunch)) {
            startPostponedEnterTransition()
            requireArguments().putBoolean(BundleConstants.firstLaunch, false)
        }

        searchViewModel = ViewModelProvider(requireActivity())[SearchViewModel::class.java]
        tagsViewModel = ViewModelProvider(requireActivity())[TagsViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // super.onViewCreated(view, savedInstanceState)

        postponeEnterTransition()

        recyclerView.addHeightKeyboardCallbacks()
        searchView.editText.setWindowInsetsAnimationCallback()
        searchView.showInput()

        searchViewModel.getSearchData().observe(viewLifecycleOwner) {
            hideLoader()
            if (SearchPreferences.isDeepSearchEnabled().invert()) {
                postponeEnterTransition()
                searchView.hideLoader()
                searchView.setNewNumber(it.size)

                appsAdapterSearchSmall = AdapterSearch(it, searchViewModel.getSearchKeywords().value ?: "")
                recyclerView.adapter = appsAdapterSearchSmall

                appsAdapterSearchSmall.setOnItemClickListener(object : AdapterCallbacks {
                    override fun onAppClicked(packageInfo: PackageInfo, icon: ImageView) {
                        SearchPreferences.setSearchKeywordMode(false)
                        openAppInfo(packageInfo, icon)
                    }

                    override fun onAppLongPressed(packageInfo: PackageInfo, icon: ImageView) {
                        childFragmentManager.showAppsMenu(packageInfo, searchViewModel.getSearchKeywords().value ?: "").onDismissListener = {
                            postDelayed(250) {
                                // searchView.showInput()
                            }
                        }
                    }
                })

                (view.parent as? ViewGroup)?.doOnPreDraw {
                    startPostponedEnterTransition()
                }
            }
        }

        searchViewModel.getSearchData().observe(viewLifecycleOwner) {
            hideLoader()
            if (SearchPreferences.isDeepSearchEnabled()) {
                postponeEnterTransition()

                searchView.setNewNumber(it.size)
                searchView.hideLoader()

                adapterDeepSearch = AdapterDeepSearch(it, searchViewModel.getSearchKeywords().value ?: "")
                recyclerView.adapter = adapterDeepSearch

                adapterDeepSearch.setOnItemClickListener(object : AdapterDeepSearch.Companion.AdapterDeepSearchCallbacks {
                    override fun onPermissionsClicked(packageInfo: PackageInfo) {
                        if (SearchPreferences.setSearchKeywordMode(true)) {
                            openFragmentSlide(
                                    Permissions.newInstance(
                                            packageInfo, searchViewModel.getSearchKeywords().value ?: ""), "permission")
                        }
                    }

                    override fun onActivitiesClicked(packageInfo: PackageInfo) {
                        if (SearchPreferences.setSearchKeywordMode(true)) {
                            openFragmentSlide(
                                    Activities.newInstance(
                                            packageInfo, searchViewModel.getSearchKeywords().value ?: ""), "activities")
                        }
                    }

                    override fun onServicesClicked(packageInfo: PackageInfo) {
                        if (SearchPreferences.setSearchKeywordMode(true)) {
                            openFragmentSlide(
                                    Services.newInstance(
                                            packageInfo, searchViewModel.getSearchKeywords().value ?: ""), "services")
                        }
                    }

                    override fun onReceiversClicked(packageInfo: PackageInfo) {
                        if (SearchPreferences.setSearchKeywordMode(true)) {
                            openFragmentSlide(
                                    Receivers.newInstance(
                                            packageInfo, searchViewModel.getSearchKeywords().value ?: ""), "receivers")
                        }
                    }

                    override fun onProvidersClicked(packageInfo: PackageInfo) {
                        if (SearchPreferences.setSearchKeywordMode(true)) {
                            openFragmentSlide(
                                    Providers.newInstance(
                                            packageInfo, searchViewModel.getSearchKeywords().value ?: ""), "providers")
                        }
                    }

                    override fun onResourcesClicked(packageInfo: PackageInfo) {
                        if (SearchPreferences.setSearchKeywordMode(true)) {
                            openFragmentSlide(
                                    Resources.newInstance(
                                            packageInfo, searchViewModel.getSearchKeywords().value ?: ""), "resources")
                        }
                    }

                    override fun onAppClicked(packageInfo: PackageInfo, icon: AppIconImageView) {
                        if (SearchPreferences.setSearchKeywordMode(false)) {
                            openAppInfo(packageInfo, icon)
                        }
                    }

                    override fun onAppLongPressed(packageInfo: PackageInfo, icon: AppIconImageView) {
                        childFragmentManager.showAppsMenu(packageInfo, searchViewModel.getSearchKeywords().value ?: "").onDismissListener = {
                            // Open keyboard after menu is dismissed
                            //                            postDelayed(250) {
                            //                                searchView.showInput()
                            //                            }
                        }
                    }
                })

                (view.parent as? ViewGroup)?.doOnPreDraw {
                    startPostponedEnterTransition()
                }
            }
        }

        tagsViewModel.getTagNames().observe(viewLifecycleOwner) {
            tags.adapter = AdapterTags(it, showNewTag = false).apply {
                setOnTagCallbackListener(object : AdapterTags.Companion.TagsCallback {
                    override fun onTagClicked(tag: String) {
                        searchView.editText.setText(buildString {
                            append("#")
                            append(tag)
                        })

                        searchView.editText.text?.length?.let { it1 ->
                            searchView.editText.setSelection(it1)
                        }
                    }

                    override fun onTagLongClicked(tag: String) {
                        /* no-op */
                    }

                    override fun onAddClicked() {
                        /* no-op */
                    }
                })
            }

            setTagsStripState(SearchPreferences.getLastSearchKeyword())
        }

        searchView.setSearchViewEventListener(object : SearchViewEventListener {
            override fun onSearchMenuPressed(button: View) {
                SearchMenu.newInstance()
                    .show(childFragmentManager, "search_list_config")
            }

            override fun onSearchTextChanged(keywords: String, count: Int) {
                removeHandlerCallbacks()
                if (keywords.isNotEmpty()) {
                    postDelayed(1000L) { // Todo : Find a better way to do this
                        searchViewModel.initiateSearch(keywords)
                    }
                } else {
                    searchViewModel.clearSearch()
                }

                setTagsStripState(keywords)
            }

            override fun onSearchRefreshPressed(button: View?) {
                searchViewModel.reload()
            }

            override fun onClear(button: View?) {
                setTagsStripState("")
                searchViewModel.clearSearch()
            }
        })
    }

    private fun setTagsStripState(keywords: String) {
        if (SearchPreferences.isDeepSearchEnabled()) {
            tags.gone(animate = false)
        } else {
            kotlin.runCatching {
                (tags.adapter as? AdapterTags)?.highlightedTag = keywords.removePrefix("#")
            }

            if (keywords.startsWith("#")) {
                tags.visible(animate = false)
            } else {
                tags.gone(animate = false)
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            SearchPreferences.sortStyle,
            SearchPreferences.isSortingReversed,
            SearchPreferences.listAppsCategory,
            SearchPreferences.appsFilter -> {
                searchViewModel.initiateSearch(SearchPreferences.getLastSearchKeyword())
            }

            SearchPreferences.deepSearch -> {
                searchViewModel.initiateSearch(SearchPreferences.getLastSearchKeyword())
                setTagsStripState(SearchPreferences.getLastSearchKeyword())
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

    override fun onDestroy() {
        super.onDestroy()
        SearchPreferences.setSearchKeywordMode(false)
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
