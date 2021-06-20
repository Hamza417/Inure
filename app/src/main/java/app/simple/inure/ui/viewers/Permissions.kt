package app.simple.inure.ui.viewers

import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.details.AdapterPermissions
import app.simple.inure.decorations.popup.PopupLinearLayout
import app.simple.inure.decorations.popup.PopupMenuCallback
import app.simple.inure.decorations.views.CustomVerticalRecyclerView
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.dialogs.miscellaneous.ErrorPopup
import app.simple.inure.dialogs.miscellaneous.ShellExecutorDialog
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.model.PermissionInfo
import app.simple.inure.popups.viewers.PopupPermissions
import app.simple.inure.viewmodels.factory.ApplicationInfoFactory
import app.simple.inure.viewmodels.viewers.ApkDataViewModel

class Permissions : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var totalPermissions: TypeFaceTextView
    private lateinit var componentsViewModel: ApkDataViewModel
    private lateinit var applicationInfoFactory: ApplicationInfoFactory
    private lateinit var adapterPermissions: AdapterPermissions

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_permissions, container, false)

        recyclerView = view.findViewById(R.id.permissions_recycler_view)
        totalPermissions = view.findViewById(R.id.total_permissions)

        applicationInfo = requireArguments().getParcelable("application_info")!!
        applicationInfoFactory = ApplicationInfoFactory(requireActivity().application, applicationInfo)
        componentsViewModel = ViewModelProvider(this, applicationInfoFactory).get(ApkDataViewModel::class.java)

        startPostponedEnterTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        componentsViewModel.getPermissions().observe(viewLifecycleOwner, {
            adapterPermissions = AdapterPermissions(it)
            recyclerView.adapter = adapterPermissions
            recyclerView.setHasFixedSize(false)
            totalPermissions.text = getString(R.string.total, it.size)

            adapterPermissions.setOnPermissionCallbacksListener(object : AdapterPermissions.Companion.PermissionCallbacks {
                override fun onPermissionClicked(container: View, permissionInfo: PermissionInfo, position: Int) {

                    val popup = PopupPermissions(layoutInflater.inflate(R.layout.popup_permission_options,
                                                                        PopupLinearLayout(requireContext()),
                                                                        true), container, permissionInfo)

                    popup.setOnMenuClickListener(object : PopupMenuCallback {
                        override fun onMenuItemClicked(source: String) {
                            when (source) {
                                getString(R.string.revoke) -> {
                                    val shell = ShellExecutorDialog.newInstance("pm revoke ${applicationInfo.packageName} ${permissionInfo.name}")
                                    shell.setOnCommandResultListener(object : ShellExecutorDialog.Companion.CommandResultCallbacks {
                                        override fun onCommandExecuted(result: String) {
                                            if (result.contains(getString(R.string.done))) {
                                                adapterPermissions.permissionStatusChanged(position, false)
                                            }
                                        }
                                    })
                                    shell.show(childFragmentManager, "shell_executor")
                                }
                                getString(R.string.grant) -> {
                                    val shell = ShellExecutorDialog.newInstance("pm grant ${applicationInfo.packageName} ${permissionInfo.name}")
                                    shell.setOnCommandResultListener(object : ShellExecutorDialog.Companion.CommandResultCallbacks {
                                        override fun onCommandExecuted(result: String) {
                                            if (result.contains(getString(R.string.done))) {
                                                adapterPermissions.permissionStatusChanged(position, true)
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
            totalPermissions.text = getString(R.string.failed)
            totalPermissions.setTextColor(Color.RED)
        })
    }

    companion object {
        fun newInstance(applicationInfo: ApplicationInfo): Permissions {
            val args = Bundle()
            args.putParcelable("application_info", applicationInfo)
            val fragment = Permissions()
            fragment.arguments = args
            return fragment
        }
    }
}