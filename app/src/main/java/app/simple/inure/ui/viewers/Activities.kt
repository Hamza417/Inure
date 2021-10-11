package app.simple.inure.ui.viewers

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.details.AdapterActivities
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.popup.PopupMenuCallback
import app.simple.inure.decorations.views.CustomVerticalRecyclerView
import app.simple.inure.dialogs.miscellaneous.ErrorPopup
import app.simple.inure.dialogs.miscellaneous.IntentAction
import app.simple.inure.dialogs.miscellaneous.ShellExecutorDialog
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.model.ActivityInfoModel
import app.simple.inure.popups.viewers.PopupActivitiesMenu
import app.simple.inure.ui.subviewers.ActivityInfo
import app.simple.inure.util.ActivityUtils
import app.simple.inure.util.FragmentHelper
import app.simple.inure.viewmodels.factory.PackageInfoFactory
import app.simple.inure.viewmodels.viewers.ApkDataViewModel

class Activities : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var componentsViewModel: ApkDataViewModel
    private lateinit var packageInfoFactory: PackageInfoFactory
    private lateinit var adapterActivities: AdapterActivities

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_activities, container, false)

        recyclerView = view.findViewById(R.id.activities_recycler_view)
        packageInfo = requireArguments().getParcelable(BundleConstants.packageInfo)!!

        packageInfoFactory = PackageInfoFactory(requireActivity().application, packageInfo)
        componentsViewModel = ViewModelProvider(this, packageInfoFactory).get(ApkDataViewModel::class.java)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startPostponedEnterTransition()

        componentsViewModel.getActivities().observe(viewLifecycleOwner, { it ->
            adapterActivities = AdapterActivities(packageInfo, it)
            recyclerView.adapter = adapterActivities

            adapterActivities.setOnActivitiesCallbacks(object : AdapterActivities.Companion.ActivitiesCallbacks {
                override fun onActivityClicked(androidComponent: ActivityInfoModel, packageId: String) {
                    clearExitTransition()
                    FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                ActivityInfo.newInstance(packageId, packageInfo),
                                                "activity_info")
                }

                override fun onActivityLongPressed(packageId: String, packageInfo: PackageInfo, icon: View, isComponentEnabled: Boolean, position: Int) {
                    val v = PopupActivitiesMenu(icon,
                                                isComponentEnabled)

                    v.setOnMenuClickListener(object : PopupMenuCallback {
                        override fun onMenuItemClicked(source: String) {
                            when (source) {
                                getString(R.string.force_launch) -> {
                                    ShellExecutorDialog.newInstance("am start -n ${packageInfo.packageName}/$packageId " +
                                                                            "-a android.intent.action.MAIN")
                                            .show(childFragmentManager, "shell_executor")
                                }
                                getString(R.string.force_launch_with_action) -> {
                                    IntentAction.newInstance(packageInfo, packageId)
                                            .show(childFragmentManager, "intent_action")
                                }
                                getString(R.string.enable) -> {
                                    val shell = ShellExecutorDialog.newInstance("pm enable ${packageInfo.packageName}/$packageId")

                                    shell.setOnCommandResultListener(object : ShellExecutorDialog.Companion.CommandResultCallbacks {
                                        override fun onCommandExecuted(result: String) {
                                            if (result.contains("Done!")) {
                                                adapterActivities.notifyItemChanged(position)
                                            }
                                        }
                                    })

                                    shell.show(childFragmentManager, "shell_executor")
                                }
                                getString(R.string.disable) -> {
                                    val shell = ShellExecutorDialog.newInstance("pm disable ${packageInfo.packageName}/$packageId")

                                    shell.setOnCommandResultListener(object : ShellExecutorDialog.Companion.CommandResultCallbacks {
                                        override fun onCommandExecuted(result: String) {
                                            if (result.contains("Done!")) {
                                                adapterActivities.notifyItemChanged(position)
                                            }
                                        }
                                    })

                                    shell.show(childFragmentManager, "shell_executor")
                                }
                            }
                        }
                    })
                }

                override fun onLaunchClicked(packageName: String, name: String) {
                    kotlin.runCatching {
                        ActivityUtils.launchPackage(requireContext(), packageName, name)
                    }.getOrElse {
                        ErrorPopup.newInstance(it.message ?: getString(R.string.unknown))
                                .show(childFragmentManager, "error_dialog")
                    }
                }
            })
        })

        componentsViewModel.getError().observe(viewLifecycleOwner, {
            val e = ErrorPopup.newInstance(it)
            e.show(childFragmentManager, "error_dialog")
            e.setOnErrorDialogCallbackListener(object : ErrorPopup.Companion.ErrorDialogCallbacks {
                override fun onDismiss() {
                    requireActivity().onBackPressed()
                }
            })
        })
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo): Activities {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            val fragment = Activities()
            fragment.arguments = args
            return fragment
        }
    }
}
