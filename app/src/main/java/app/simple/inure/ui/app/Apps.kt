package app.simple.inure.ui.app

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.viewModels
import androidx.transition.Explode
import androidx.transition.Fade
import app.simple.inure.R
import app.simple.inure.adapters.AppsAdapter
import app.simple.inure.decorations.indicatorfastscroll.FastScrollItemIndicator
import app.simple.inure.decorations.indicatorfastscroll.FastScrollerThumbView
import app.simple.inure.decorations.indicatorfastscroll.FastScrollerView
import app.simple.inure.decorations.searchview.SearchView
import app.simple.inure.decorations.searchview.SearchViewEventListener
import app.simple.inure.decorations.transitions.DetailsTransitionArc
import app.simple.inure.decorations.views.CustomRecyclerView
import app.simple.inure.decorations.views.MainListPopupMenu
import app.simple.inure.extension.fragments.CoroutineScopedFragment
import app.simple.inure.interfaces.adapters.AppsAdapterCallbacks
import app.simple.inure.interfaces.menu.PopupMenuCallback
import app.simple.inure.util.PackageUtils.killThisApp
import app.simple.inure.util.PackageUtils.launchThisPackage
import app.simple.inure.util.PackageUtils.uninstallThisPackage
import app.simple.inure.util.Sort
import app.simple.inure.util.Sort.getSortedList
import app.simple.inure.viewmodels.AppData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class Apps : CoroutineScopedFragment(), AppsAdapterCallbacks, PopupMenuCallback {

    private lateinit var appsListRecyclerView: CustomRecyclerView
    private lateinit var fastScrollerView: FastScrollerView
    private lateinit var scrollerThumb: FastScrollerThumbView
    private lateinit var searchView: SearchView
    private lateinit var appsAdapter: AppsAdapter
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

            appsAdapter = AppsAdapter(this)
            appsAdapter.apps = allAppsList

            appsListRecyclerView.adapter = appsAdapter

            fastScrollerView.setupWithRecyclerView(appsListRecyclerView, { position ->
                FastScrollItemIndicator.Text(allAppsList[position].name.substring(0, 1)
                                                 .toUpperCase(Locale.ROOT))
            })

            scrollerThumb.setupWithFastScroller(fastScrollerView)

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }
        })

        searchView.setSearchViewEventListener(object : SearchViewEventListener {
            override fun onSearchMenuPressed(button: View) {
                Toast.makeText(requireContext(), "Menu clicked", Toast.LENGTH_SHORT).show()
            }

            override fun onSearchTextChanged(keywords: String, count: Int) {
                Toast.makeText(requireContext(), "Called again", Toast.LENGTH_SHORT).show()
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

                                filteredList.getSortedList(Sort.ALPHABETICALLY, false)
                            } catch (ignored: ConcurrentModificationException) {
                            } catch (ignored: IndexOutOfBoundsException) {
                            } catch (ignored: NullPointerException) {
                            }
                        } else {
                            filteredList = allAppsList
                        }
                    }

                    appsAdapter.searchKeyword = keywords
                    appsAdapter.apps = filteredList
                    appsAdapter.notifyDataSetChanged()
                }
            }
        })

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onAppClicked(applicationInfo: ApplicationInfo, icon: ImageView) {
        openAppInfo(applicationInfo, icon)
    }

    override fun onMenuClicked(applicationInfo: ApplicationInfo, viewGroup: ViewGroup, xOff: Float, yOff: Float, icon: ImageView) {
        val popupMenu = MainListPopupMenu(layoutInflater.inflate(R.layout.menu_main_list, ConstraintLayout(requireContext()), true),
                                          viewGroup, xOff, yOff, applicationInfo, icon)
        popupMenu.popupMenuCallback = this
    }

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

    private fun openAppInfo(applicationInfo: ApplicationInfo, icon: ImageView) {
        val appInfo = requireActivity().supportFragmentManager.findFragmentByTag("app_info")
            ?: AppInfo.newInstance(applicationInfo, icon.transitionName)

        exitTransition = Explode()
        appInfo.sharedElementEnterTransition = DetailsTransitionArc()
        appInfo.enterTransition = Explode()
        appInfo.sharedElementReturnTransition = DetailsTransitionArc()

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
}
