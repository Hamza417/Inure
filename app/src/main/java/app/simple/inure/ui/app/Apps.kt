package app.simple.inure.ui.app

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.viewModels
import app.simple.inure.R
import app.simple.inure.adapters.AppsAdapter
import app.simple.inure.adapters.AppsAdapterSmall
import app.simple.inure.decorations.corners.DynamicCornerConstraintLayout
import app.simple.inure.decorations.corners.DynamicCornerLinearLayout
import app.simple.inure.decorations.indicatorfastscroll.FastScrollItemIndicator
import app.simple.inure.decorations.indicatorfastscroll.FastScrollerThumbView
import app.simple.inure.decorations.indicatorfastscroll.FastScrollerView
import app.simple.inure.decorations.popup.PopupMenuCallback
import app.simple.inure.decorations.searchview.SearchView
import app.simple.inure.decorations.searchview.SearchViewEventListener
import app.simple.inure.decorations.transitions.DetailsTransitionArc
import app.simple.inure.decorations.transitions.TransitionManager
import app.simple.inure.decorations.views.CustomRecyclerView
import app.simple.inure.dialogs.AppsListConfiguration
import app.simple.inure.extension.fragments.CoroutineScopedFragment
import app.simple.inure.interfaces.adapters.AppsAdapterCallbacks
import app.simple.inure.packagehelper.PackageUtils.killThisApp
import app.simple.inure.packagehelper.PackageUtils.launchThisPackage
import app.simple.inure.packagehelper.PackageUtils.uninstallThisPackage
import app.simple.inure.popups.MainListPopupMenu
import app.simple.inure.preferences.MainPreferences
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences
import app.simple.inure.util.Sort.getSortedList
import app.simple.inure.viewmodels.AppData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


class Apps : CoroutineScopedFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var appsListRecyclerView: CustomRecyclerView
    private lateinit var fastScrollerView: FastScrollerView
    private lateinit var scrollerThumb: FastScrollerThumbView
    private lateinit var searchView: SearchView

    private lateinit var appsAdapter: AppsAdapterSmall
    private lateinit var appUninstallObserver: ActivityResultLauncher<Intent>
    private var allAppsList = arrayListOf<ApplicationInfo>()

    private val model: AppData by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_all_apps, container, false)

        appsListRecyclerView = view.findViewById(R.id.all_apps_recycler_view)
        fastScrollerView = view.findViewById(R.id.all_apps_fast_scroller)
        scrollerThumb = view.findViewById(R.id.all_apps_thumb)
        searchView = view.findViewById(R.id.all_apps_search_view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        model.getAppData().observe(requireActivity(), {
            postponeEnterTransition()
            allAppsList = it

            appsAdapter = AppsAdapterSmall()
            appsAdapter.apps = allAppsList

            appsListRecyclerView.adapter = appsAdapter

            if (!fastScrollerView.isSetup) {
                fastScrollerView.setupWithRecyclerView(appsListRecyclerView, { position ->
                    FastScrollItemIndicator.Text(allAppsList[position].name.substring(0, 1)
                                                         .toUpperCase(Locale.ROOT))
                })

                scrollerThumb.setupWithFastScroller(fastScrollerView)
            }

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }

            appsAdapter.setOnItemClickListener(object : AppsAdapterCallbacks {
                override fun onAppClicked(applicationInfo: ApplicationInfo, icon: ImageView) {
                    openAppInfo(applicationInfo, icon)
                }

                override fun onMenuClicked(applicationInfo: ApplicationInfo, viewGroup: ViewGroup, xOff: Float, yOff: Float, icon: ImageView) {
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
        })

        searchView.setSearchViewEventListener(object : SearchViewEventListener {
            override fun onSearchMenuPressed(button: View) {
                AppsListConfiguration.newInstance()
                        .show(childFragmentManager, "apps_list_configuration")
            }

            override fun onSearchTextChanged(keywords: String, count: Int) {
                launch {

                    var filteredList = arrayListOf<ApplicationInfo>()

                    withContext(Dispatchers.Default) {
                        if (count > 0) {
                            try {
                                for (apps in allAppsList) {
                                    if (
                                        apps.packageName.toLowerCase(Locale.ROOT)
                                                .contains(keywords.toLowerCase(Locale.ROOT))
                                        || apps.name.toLowerCase(Locale.ROOT)
                                                .contains(keywords.toLowerCase(Locale.ROOT))) {
                                        filteredList.add(apps)
                                    }
                                }

                                filteredList.getSortedList(MainPreferences.getSortStyle(), context!!)
                            } catch (ignored: ConcurrentModificationException) {
                            } catch (ignored: IndexOutOfBoundsException) {
                            } catch (ignored: NullPointerException) {
                            }
                        } else {
                            filteredList = allAppsList
                        }
                    }

                    //appsAdapter.searchKeyword = keywords
                    appsAdapter.apps = filteredList
                    appsAdapter.notifyDataSetChanged()
                }
            }
        })

        appUninstallObserver = registerForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                /**
                 * Refresh the viewModel to re-fetch the updated list
                 */
                model.loadAppData()
                println("App Uninstalled")
            } else {
                println("Failed")
            }

            println("${result.data} : ${result.resultCode}")
        }

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        getSharedPreferences().registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this)
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
        fun newInstance(): Apps {
            val args = Bundle()
            val fragment = Apps()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            MainPreferences.sortStyle, MainPreferences.isSortingReversed -> {
                model.loadAppData()
            }
        }
    }
}
