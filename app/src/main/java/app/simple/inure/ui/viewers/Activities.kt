package app.simple.inure.ui.viewers

import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.details.AdapterActivities
import app.simple.inure.decorations.popup.PopupLinearLayout
import app.simple.inure.decorations.popup.PopupMenuCallback
import app.simple.inure.decorations.views.CustomRecyclerView
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.dialogs.miscellaneous.ErrorPopup
import app.simple.inure.dialogs.miscellaneous.IntentAction
import app.simple.inure.dialogs.miscellaneous.ShellExecutorDialog
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.popups.viewers.PopupActivitiesMenu
import app.simple.inure.ui.subviewers.ActivityInfo
import app.simple.inure.util.FragmentHelper
import app.simple.inure.viewmodels.factory.ApplicationInfoFactory
import app.simple.inure.viewmodels.panels.ApkDataViewModel
import com.jaredrummler.apkparser.model.AndroidComponent

class Activities : ScopedFragment() {

    private lateinit var recyclerView: CustomRecyclerView
    private lateinit var totalActivities: TypeFaceTextView
    private lateinit var componentsViewModel: ApkDataViewModel
    private lateinit var applicationInfoFactory: ApplicationInfoFactory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_activities, container, false)

        recyclerView = view.findViewById(R.id.activities_recycler_view)
        totalActivities = view.findViewById(R.id.total_activities)
        applicationInfo = requireArguments().getParcelable("application_info")!!

        applicationInfoFactory = ApplicationInfoFactory(requireActivity().application, applicationInfo)
        componentsViewModel = ViewModelProvider(this, applicationInfoFactory).get(ApkDataViewModel::class.java)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startPostponedEnterTransition()

        componentsViewModel.getActivities().observe(viewLifecycleOwner, {
            recyclerView.adapter = AdapterActivities(applicationInfo, it)

            totalActivities.text = getString(R.string.total, it.size)

            (recyclerView.adapter as AdapterActivities).setOnActivitiesCallbacks(object : AdapterActivities.Companion.ActivitiesCallbacks {
                override fun onActivityClicked(androidComponent: AndroidComponent, packageId: String) {
                    clearExitTransition()
                    FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                ActivityInfo.newInstance(packageId, applicationInfo),
                                                "activity_info")
                }

                override fun onActivityLongPressed(androidComponent: AndroidComponent, packageId: String, applicationInfo: ApplicationInfo, icon: View) {
                    val v = PopupActivitiesMenu(LayoutInflater.from(requireContext()).inflate(R.layout.popup_activities_menu,
                                                                                              PopupLinearLayout(requireContext())), icon)

                    v.setOnMenuClickListener(object : PopupMenuCallback {
                        override fun onMenuItemClicked(source: String) {
                            when (source) {
                                getString(R.string.force_launch) -> {
                                    ShellExecutorDialog.newInstance("am start -n ${applicationInfo.packageName}/$packageId " +
                                                                            "-a android.intent.action.MAIN")
                                            .show(childFragmentManager, "shell_executor")
                                }
                                getString(R.string.force_launch_with_action) -> {
                                    IntentAction.newInstance(applicationInfo, packageId)
                                            .show(childFragmentManager, "intent_action")
                                }
                            }
                        }
                    })
                }
            })
        })

        componentsViewModel.getError().observe(viewLifecycleOwner, {
            ErrorPopup.newInstance(it)
                    .show(childFragmentManager, "apk_error_window")
            totalActivities.text = getString(R.string.failed)
            totalActivities.setTextColor(Color.RED)
        })
    }

    companion object {
        fun newInstance(applicationInfo: ApplicationInfo): Activities {
            val args = Bundle()
            args.putParcelable("application_info", applicationInfo)
            val fragment = Activities()
            fragment.arguments = args
            return fragment
        }
    }
}
