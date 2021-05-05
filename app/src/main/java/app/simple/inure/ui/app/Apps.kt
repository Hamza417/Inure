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
import androidx.recyclerview.selection.Selection
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.transition.Fade
import app.simple.inure.R
import app.simple.inure.adapters.ui.AppsAdapterSmall
import app.simple.inure.decorations.corners.DynamicCornerLinearLayout
import app.simple.inure.decorations.indicatorfastscroll.FastScrollItemIndicator
import app.simple.inure.decorations.indicatorfastscroll.FastScrollerThumbView
import app.simple.inure.decorations.indicatorfastscroll.FastScrollerView
import app.simple.inure.decorations.popup.PopupMenuCallback
import app.simple.inure.decorations.transitions.DetailsTransitionArc
import app.simple.inure.decorations.viewholders.VerticalListViewHolder
import app.simple.inure.decorations.views.CustomRecyclerView
import app.simple.inure.dialogs.app.AppsListConfiguration
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.interfaces.adapters.AppsAdapterCallbacks
import app.simple.inure.popups.app.MainListPopupMenu
import app.simple.inure.popups.app.PopupMainMenu
import app.simple.inure.preferences.MainPreferences
import app.simple.inure.ui.preferences.MainPreferencesScreen
import app.simple.inure.util.FragmentHelper.openFragmentLinear
import app.simple.inure.util.PackageUtils.killThisApp
import app.simple.inure.util.PackageUtils.launchThisPackage
import app.simple.inure.util.PackageUtils.uninstallThisPackage
import app.simple.inure.viewmodels.AppData
import java.util.*

class Apps : ScopedFragment() {

    private lateinit var appsListRecyclerView: CustomRecyclerView
    private lateinit var fastScrollerView: FastScrollerView
    private lateinit var scrollerThumb: FastScrollerThumbView

    private lateinit var appsAdapter: AppsAdapterSmall
    private lateinit var appUninstallObserver: ActivityResultLauncher<Intent>
    private var allAppsList = arrayListOf<ApplicationInfo>()
    private var tracker: SelectionTracker<Long>? = null

    private val model: AppData by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_all_apps, container, false)

        appsListRecyclerView = view.findViewById(R.id.all_apps_recycler_view)
        fastScrollerView = view.findViewById(R.id.all_apps_fast_scroller)
        scrollerThumb = view.findViewById(R.id.all_apps_thumb)

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
                    if (position == VerticalListViewHolder.TYPE_HEADER) {
                        FastScrollItemIndicator.Icon(R.drawable.ic_header_icon)
                    } else {
                        FastScrollItemIndicator.Text(allAppsList[position - 1].name.substring(0, 1)
                                                             .toUpperCase(Locale.ROOT))
                    }
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
                                    applicationInfo.uninstallThisPackage(appUninstallObserver)
                                }
                            }
                        }
                    })
                }

                override fun onSearchPressed(view: View) {
                    val fragment = requireActivity().supportFragmentManager.findFragmentByTag("search_panel")
                        ?: SearchPanel.newInstance()

                    requireActivity().supportFragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.dialog_in, R.anim.dialog_out)
                            .replace(R.id.app_container, fragment, "search_panel")
                            .addToBackStack("search_panel").commit()
                }

                override fun onSettingsPressed() {

                    AppsListConfiguration.newInstance().show(childFragmentManager, "apps_list_config")
                }

                override fun onPrefsIconPressed(view: View, view1: View) {
                    val v = PopupMainMenu(LayoutInflater.from(requireContext()).inflate(R.layout.popup_main_menu,
                                                                                        DynamicCornerLinearLayout(requireContext(), null)), view1)

                    v.setOnMenuClickListener(object : PopupMainMenu.PopupMainMenuCallbacks {
                        override fun onMenuClicked(string: String) {
                            when (string) {
                                getString(R.string.terminal) -> {
                                    openFragmentLinear(requireActivity().supportFragmentManager,
                                                       Terminal.newInstance(),
                                                       view,
                                                       "terminal_screen")
                                }
                                getString(R.string.preferences) -> {
                                    openFragmentLinear(requireActivity().supportFragmentManager,
                                                       MainPreferencesScreen.newInstance(),
                                                       view,
                                                       "preferences_screen")
                                }
                                getString(R.string.device_analytics) -> {
                                    openFragmentLinear(requireActivity().supportFragmentManager,
                                                       Analytics.newInstance(),
                                                       view,
                                                       "analytics")
                                }
                            }
                        }
                    })
                }

                override fun onItemSelected() {
                    super.onItemSelected()
                    tracker = SelectionTracker.Builder(
                        "selection",
                        appsListRecyclerView,
                        CustomRecyclerView.KeyProvider(appsListRecyclerView),
                        CustomRecyclerView.AppsLookup(appsListRecyclerView),
                        StorageStrategy.createLongStorage()
                    )
                            .withSelectionPredicate(SelectionPredicates.createSelectAnything())
                            .build()

                    appsAdapter.tracker = tracker
                    appsAdapter.notifyDataSetChanged()
                }
            })
        })

        tracker?.addObserver(
            object : SelectionTracker.SelectionObserver<Long>() {
                override fun onSelectionChanged() {
                    super.onSelectionChanged()
                    val items = tracker?.selection!!.size()
                    if (items == 2) {
                        launchSum(tracker?.selection!!)
                    }
                }
            })

        appUninstallObserver = registerForActivityResult(StartActivityForResult()) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    /**
                     * App uninstalled,
                     * Tell the viewModel to re-fetch the updated list
                     */
                    model.loadAppData()
                }
                Activity.RESULT_CANCELED -> {
                    /* no-op */
                }
            }
        }

        super.onViewCreated(view, savedInstanceState)
    }

    private fun openAppInfo(applicationInfo: ApplicationInfo, icon: ImageView) {
        val appInfo = requireActivity().supportFragmentManager.findFragmentByTag("app_info")
            ?: AppInfo.newInstance(applicationInfo, icon.transitionName)

        exitTransition = Fade()
        appInfo.sharedElementEnterTransition = DetailsTransitionArc()
        appInfo.enterTransition = Fade()
        appInfo.sharedElementReturnTransition = DetailsTransitionArc()

        requireActivity().supportFragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .addSharedElement(icon, icon.transitionName)
                .replace(R.id.app_container, appInfo, "app_info").addToBackStack("app_info").commit()
    }

    private fun launchSum(selection: Selection<Long>) {
        val list = selection.map {
            appsAdapter.apps[it.toInt()]
        }.toList()
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
            MainPreferences.sortStyle,
            MainPreferences.isSortingReversed,
            MainPreferences.listAppsCategory,
            -> {
                model.loadAppData()
            }
        }
    }
}
