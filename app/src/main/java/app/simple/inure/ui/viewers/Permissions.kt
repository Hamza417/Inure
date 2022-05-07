package app.simple.inure.ui.viewers

import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.details.AdapterPermissions
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceEditTextDynamicCorner
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.dialogs.action.PermissionStatus
import app.simple.inure.dialogs.menus.PermissionsMenu
import app.simple.inure.dialogs.miscellaneous.Error
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.extension.popup.PopupMenuCallback
import app.simple.inure.factories.panels.PackageInfoFactory
import app.simple.inure.models.PermissionInfo
import app.simple.inure.popups.viewers.PopupPermissions
import app.simple.inure.preferences.PermissionPreferences
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.viewers.PermissionsViewModel

class Permissions : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var options: DynamicRippleImageButton
    private lateinit var search: DynamicRippleImageButton
    private lateinit var title: TypeFaceTextView
    private lateinit var searchBox: TypeFaceEditTextDynamicCorner
    private lateinit var permissionsViewModel: PermissionsViewModel
    private lateinit var packageInfoFactory: PackageInfoFactory
    private lateinit var adapterPermissions: AdapterPermissions

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_permissions, container, false)

        recyclerView = view.findViewById(R.id.permissions_recycler_view)
        options = view.findViewById(R.id.permissions_option_btn)
        search = view.findViewById(R.id.permissions_search_btn)
        searchBox = view.findViewById(R.id.permissions_search)
        title = view.findViewById(R.id.permission_title)
        packageInfo = requireArguments().getParcelable("application_info")!!
        packageInfoFactory = PackageInfoFactory(requireActivity().application, packageInfo)
        permissionsViewModel = ViewModelProvider(this, packageInfoFactory).get(PermissionsViewModel::class.java)

        searchBoxState()
        startPostponedEnterTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        permissionsViewModel.getPermissions().observe(viewLifecycleOwner) {
            adapterPermissions = AdapterPermissions(it, searchBox.text.toString().trim())
            recyclerView.adapter = adapterPermissions

            adapterPermissions.setOnPermissionCallbacksListener(object : AdapterPermissions.Companion.PermissionCallbacks {
                override fun onPermissionClicked(container: View, permissionInfo: PermissionInfo, position: Int) {

                    val popup = PopupPermissions(container, permissionInfo)

                    popup.setOnMenuClickListener(object : PopupMenuCallback {
                        override fun onMenuItemClicked(source: String) {
                            when (source) {
                                getString(R.string.revoke) -> {
                                    val p = PermissionStatus.newInstance(packageInfo, permissionInfo, getString(R.string.revoke))
                                    p.show(childFragmentManager, "permission_status")
                                    p.setOnPermissionStatusCallbackListener(object : PermissionStatus.Companion.PermissionStatusCallbacks {
                                        override fun onSuccess(grantedStatus: Boolean) {
                                            adapterPermissions.permissionStatusChanged(position, grantedStatus)
                                        }
                                    })
                                }
                                getString(R.string.grant) -> {
                                    val p = PermissionStatus.newInstance(packageInfo, permissionInfo, getString(R.string.grant))
                                    p.show(childFragmentManager, "permission_status")
                                    p.setOnPermissionStatusCallbackListener(object : PermissionStatus.Companion.PermissionStatusCallbacks {
                                        override fun onSuccess(grantedStatus: Boolean) {
                                            adapterPermissions.permissionStatusChanged(position, grantedStatus)
                                        }
                                    })
                                }
                            }
                        }
                    })
                }
            })

            searchBox.doOnTextChanged { text, _, _, _ ->
                if (searchBox.isFocused) {
                    permissionsViewModel.loadPermissionData(text.toString().trim())
                }
            }
        }

        permissionsViewModel.getError().observe(viewLifecycleOwner) {
            val e = Error.newInstance(it)
            e.show(childFragmentManager, "error_dialog")
            e.setOnErrorDialogCallbackListener(object : Error.Companion.ErrorDialogCallbacks {
                override fun onDismiss() {
                    requireActivity().onBackPressed()
                }
            })
        }

        options.setOnClickListener {
            PermissionsMenu.newInstance()
                .show(childFragmentManager, "permission_menu")
        }

        search.setOnClickListener {
            if (searchBox.text.isNullOrEmpty()) {
                PermissionPreferences.setSearchVisibility(!PermissionPreferences.isSearchVisible())
            } else {
                searchBox.text?.clear()
            }
        }
    }

    private fun searchBoxState() {
        if (PermissionPreferences.isSearchVisible()) {
            search.setImageResource(R.drawable.ic_close)
            title.gone()
            searchBox.visible(true)
            searchBox.showInput()
        } else {
            search.setImageResource(R.drawable.ic_search)
            title.visible(true)
            searchBox.gone()
            searchBox.hideInput()
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            PermissionPreferences.permissionSearch -> {
                searchBoxState()
            }
            PermissionPreferences.labelType -> {
                adapterPermissions.update()
            }
        }
    }

    companion object {
        fun newInstance(applicationInfo: PackageInfo): Permissions {
            val args = Bundle()
            args.putParcelable("application_info", applicationInfo)
            val fragment = Permissions()
            fragment.arguments = args
            return fragment
        }
    }
}