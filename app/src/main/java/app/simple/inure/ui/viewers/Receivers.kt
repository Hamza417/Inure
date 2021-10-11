package app.simple.inure.ui.viewers

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.details.AdapterReceivers
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.popup.PopupMenuCallback
import app.simple.inure.decorations.views.CustomVerticalRecyclerView
import app.simple.inure.dialogs.miscellaneous.ErrorPopup
import app.simple.inure.dialogs.miscellaneous.ShellExecutorDialog
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.popups.viewers.PopupReceiversMenu
import app.simple.inure.viewmodels.factory.PackageInfoFactory
import app.simple.inure.viewmodels.viewers.ApkDataViewModel

class Receivers : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var adapterReceivers: AdapterReceivers
    private lateinit var componentsViewModel: ApkDataViewModel
    private lateinit var packageInfoFactory: PackageInfoFactory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_receivers, container, false)

        recyclerView = view.findViewById(R.id.broadcast_recycler_view)
        packageInfo = requireArguments().getParcelable(BundleConstants.packageInfo)!!

        packageInfoFactory = PackageInfoFactory(requireActivity().application, packageInfo)
        componentsViewModel = ViewModelProvider(this, packageInfoFactory).get(ApkDataViewModel::class.java)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startPostponedEnterTransition()

        componentsViewModel.getReceivers().observe(viewLifecycleOwner, {
            adapterReceivers = AdapterReceivers(it, packageInfo)
            recyclerView.adapter = adapterReceivers

            adapterReceivers.setOnReceiversCallbackListener(object : AdapterReceivers.Companion.ReceiversCallbacks {
                override fun onReceiversLongPressed(packageId: String, packageInfo: PackageInfo, icon: View, isComponentEnabled: Boolean, position: Int) {
                    val v = PopupReceiversMenu(icon, isComponentEnabled)

                    v.setOnMenuClickListener(object : PopupMenuCallback {
                        override fun onMenuItemClicked(source: String) {
                            when (source) {
                                getString(R.string.enable) -> {
                                    val shell = ShellExecutorDialog.newInstance("pm enable ${packageInfo.packageName}/$packageId")

                                    shell.setOnCommandResultListener(object : ShellExecutorDialog.Companion.CommandResultCallbacks {
                                        override fun onCommandExecuted(result: String) {
                                            if (result.contains("Done!")) {
                                                adapterReceivers.notifyItemChanged(position)
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
                                                adapterReceivers.notifyItemChanged(position)
                                            }
                                        }
                                    })

                                    shell.show(childFragmentManager, "shell_executor")
                                }
                            }
                        }
                    })
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
        fun newInstance(packageInfo: PackageInfo): Receivers {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            val fragment = Receivers()
            fragment.arguments = args
            return fragment
        }
    }
}