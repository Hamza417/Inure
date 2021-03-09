package app.simple.inure.ui

import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Fade
import app.simple.inure.R
import app.simple.inure.adapters.AppsAdapter
import app.simple.inure.decorations.transitions.DetailsTransitionArc
import app.simple.inure.decorations.views.CustomPopupWindow
import app.simple.inure.decorations.views.StateAwareRecyclerView
import app.simple.inure.interfaces.adapters.AppsAdapterCallbacks
import app.simple.inure.viewmodels.AppData


class Apps : Fragment(), AppsAdapterCallbacks {

    private lateinit var appsListRecyclerView: StateAwareRecyclerView
    private val model: AppData by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_all_apps, container, false)

        appsListRecyclerView = view.findViewById(R.id.all_apps_recycler_view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        model.getAppData().observe(requireActivity(), {
            val appsAdapter = AppsAdapter(it, this)
            appsAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.ALLOW
            appsListRecyclerView.adapter = appsAdapter
            appsListRecyclerView.scheduleLayoutAnimation()
        })

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onAppClicked(packageName: String, icon: ImageView) {

        val appInfo = requireActivity().supportFragmentManager.findFragmentByTag("app_info")
                ?: AppInfo.newInstance(packageName, icon.transitionName)

        exitTransition = Fade()
        appInfo.sharedElementEnterTransition = DetailsTransitionArc()
        appInfo.enterTransition = Fade()
        appInfo.sharedElementReturnTransition = DetailsTransitionArc()
        parentFragment?.postponeEnterTransition()

        requireActivity().supportFragmentManager
                .beginTransaction()
                .addSharedElement(icon, icon.transitionName)
                .replace(R.id.app_container, appInfo, "app_info")
                .addToBackStack("app_info")
                .commit()
    }

    override fun onMenuClicked(packageName: String, menu: ImageButton) {
        val popupWindow = CustomPopupWindow(
                layoutInflater.inflate(R.layout.menu_main_list, ConstraintLayout(requireContext()), true))
        popupWindow.showAsDropDown(menu, -500, -150, Gravity.START)
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