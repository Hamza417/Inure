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
import app.simple.inure.adapters.details.AdapterServices
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceEditTextDynamicCorner
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.dialogs.action.ComponentState
import app.simple.inure.dialogs.miscellaneous.Error
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.extension.popup.PopupMenuCallback
import app.simple.inure.factories.panels.PackageInfoFactory
import app.simple.inure.models.ServiceInfoModel
import app.simple.inure.popups.viewers.PopupServicesMenu
import app.simple.inure.preferences.ServicesPreferences
import app.simple.inure.ui.subviewers.ServiceInfo
import app.simple.inure.util.FragmentHelper
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.viewers.ServicesViewModel

class Services : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var search: DynamicRippleImageButton
    private lateinit var title: TypeFaceTextView
    private lateinit var searchBox: TypeFaceEditTextDynamicCorner

    private var adapterServices: AdapterServices? = null
    private lateinit var servicesViewModel: ServicesViewModel
    private lateinit var packageInfoFactory: PackageInfoFactory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_services, container, false)

        recyclerView = view.findViewById(R.id.services_recycler_view)
        search = view.findViewById(R.id.services_search_btn)
        searchBox = view.findViewById(R.id.services_search)
        title = view.findViewById(R.id.services_title)

        packageInfo = requireArguments().getParcelable(BundleConstants.packageInfo)!!
        packageInfoFactory = PackageInfoFactory(requireActivity().application, packageInfo)
        servicesViewModel = ViewModelProvider(this, packageInfoFactory)[ServicesViewModel::class.java]

        searchBoxState()
        startPostponedEnterTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        servicesViewModel.getServices().observe(viewLifecycleOwner) {
            adapterServices = AdapterServices(it, packageInfo, searchBox.text.toString().trim())
            recyclerView.adapter = adapterServices

            adapterServices?.setOnServiceCallbackListener(object : AdapterServices.Companion.ServicesCallbacks {
                override fun onServiceClicked(serviceInfoModel: ServiceInfoModel) {
                    clearExitTransition()
                    FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                ServiceInfo.newInstance(serviceInfoModel, packageInfo),
                                                "services_info")
                }

                override fun onServiceLongPressed(packageId: String, packageInfo: PackageInfo, icon: View, isComponentEnabled: Boolean, position: Int) {
                    val v = PopupServicesMenu(icon, isComponentEnabled)

                    v.setOnMenuClickListener(object : PopupMenuCallback {
                        override fun onMenuItemClicked(source: String) {
                            when (source) {
                                getString(R.string.enable), getString(R.string.disable) -> {
                                    val p = ComponentState.newInstance(packageInfo, packageId, isComponentEnabled)
                                    p.setOnComponentStateChangeListener(object : ComponentState.Companion.ComponentStatusCallbacks {
                                        override fun onSuccess() {
                                            adapterServices?.notifyItemChanged(position)
                                        }
                                    })
                                    p.show(childFragmentManager, "component_state")
                                }
                            }
                        }
                    })
                }
            })

            searchBox.doOnTextChanged { text, _, _, _ ->
                if (searchBox.isFocused) {
                    servicesViewModel.getServicesData(text.toString().trim())
                }
            }
        }

        servicesViewModel.getError().observe(viewLifecycleOwner) {
            val e = Error.newInstance(it)
            e.show(childFragmentManager, "error_dialog")
            e.setOnErrorDialogCallbackListener(object : Error.Companion.ErrorDialogCallbacks {
                override fun onDismiss() {
                    requireActivity().onBackPressed()
                }
            })
        }

        search.setOnClickListener {
            if (searchBox.text.isNullOrEmpty()) {
                ServicesPreferences.setSearchVisibility(!ServicesPreferences.isSearchVisible())
            } else {
                searchBox.text?.clear()
            }
        }
    }

    private fun searchBoxState() {
        if (ServicesPreferences.isSearchVisible()) {
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
            ServicesPreferences.servicesSearch -> {
                searchBoxState()
            }
        }
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo): Services {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            val fragment = Services()
            fragment.arguments = args
            return fragment
        }
    }
}
