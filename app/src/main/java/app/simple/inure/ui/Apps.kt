package app.simple.inure.ui

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Explode
import app.simple.inure.R
import app.simple.inure.adapters.AppsAdapter
import app.simple.inure.adapters.AppsAdapterSmall
import app.simple.inure.decorations.indicatorfastscroll.FastScrollItemIndicator
import app.simple.inure.decorations.indicatorfastscroll.FastScrollerThumbView
import app.simple.inure.decorations.indicatorfastscroll.FastScrollerView
import app.simple.inure.decorations.transitions.DetailsTransitionArc
import app.simple.inure.decorations.views.CustomRecyclerView
import app.simple.inure.decorations.views.MainListPopupMenu
import app.simple.inure.interfaces.adapters.AppsAdapterCallbacks
import app.simple.inure.interfaces.menu.PopupMenuCallback
import app.simple.inure.util.PackageUtils.killThisApp
import app.simple.inure.util.PackageUtils.launchThisPackage
import app.simple.inure.util.PackageUtils.uninstallThisPackage
import app.simple.inure.viewmodels.AppData
import java.util.*

class Apps : Fragment(), AppsAdapterCallbacks, PopupMenuCallback {

    private lateinit var appsListRecyclerView: CustomRecyclerView
    private lateinit var fastScrollerView: FastScrollerView
    private lateinit var scrollerThumb: FastScrollerThumbView

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
            val appsAdapter = AppsAdapter(it, this)
            appsAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.ALLOW
            appsListRecyclerView.adapter = appsAdapter
            appsListRecyclerView.scheduleLayoutAnimation()
            fastScrollerView.setupWithRecyclerView(appsListRecyclerView, { position ->
                val item = it[position]
                FastScrollItemIndicator.Text(item.name.substring(0, 1).toUpperCase(Locale.ROOT))
            })
            scrollerThumb.setupWithFastScroller(fastScrollerView)
        })

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onAppClicked(applicationInfo: ApplicationInfo, icon: ImageView) {
        openAppInfo(applicationInfo, icon)
    }

    override fun onMenuClicked(applicationInfo: ApplicationInfo, viewGroup: ViewGroup, xOff: Float, yOff: Float, icon: ImageView) {
        val view =
            layoutInflater.inflate(R.layout.menu_main_list, ConstraintLayout(requireContext()), true)
        view.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val popupMenu = MainListPopupMenu(view, viewGroup, xOff, yOff, applicationInfo, icon)
        popupMenu.popupMenuCallback = this
    }

    companion object {
        fun newInstance(): Apps {
            val args = Bundle()
            val fragment = Apps()
            fragment.arguments = args
            return fragment
        }
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
        parentFragment?.postponeEnterTransition()

        requireActivity().supportFragmentManager.beginTransaction()
            .addSharedElement(icon, icon.transitionName)
            .replace(R.id.app_container, appInfo, "app_info").addToBackStack("app_info").commit()
    }
}