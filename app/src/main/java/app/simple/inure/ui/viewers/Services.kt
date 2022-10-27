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
import app.simple.inure.dialogs.action.ComponentState
import app.simple.inure.extensions.fragments.SearchBarScopedFragment
import app.simple.inure.extensions.popup.PopupMenuCallback
import app.simple.inure.factories.panels.PackageInfoFactory
import app.simple.inure.models.ServiceInfoModel
import app.simple.inure.popups.viewers.PopupServicesMenu
import app.simple.inure.preferences.ServicesPreferences
import app.simple.inure.ui.subviewers.ServiceInfo
import app.simple.inure.viewmodels.viewers.ServicesViewModel

class Services : SearchBarScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView

    private var adapterServices: AdapterServices? = null
    private lateinit var servicesViewModel: ServicesViewModel
    private lateinit var packageInfoFactory: PackageInfoFactory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_services, container, false)

        recyclerView = view.findViewById(R.id.services_recycler_view)
        search = view.findViewById(R.id.services_search_btn)
        searchBox = view.findViewById(R.id.services_search)
        title = view.findViewById(R.id.services_title)

        packageInfoFactory = PackageInfoFactory(packageInfo)
        servicesViewModel = ViewModelProvider(this, packageInfoFactory)[ServicesViewModel::class.java]

        searchBoxState(false, ServicesPreferences.isSearchVisible())
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
                    openFragmentSlide(ServiceInfo.newInstance(serviceInfoModel, packageInfo), "services_info")
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
            showError(it)
        }

        servicesViewModel.notFound.observe(viewLifecycleOwner) {
            showWarning(R.string.no_services_found)
        }

        search.setOnClickListener {
            if (searchBox.text.isNullOrEmpty()) {
                ServicesPreferences.setSearchVisibility(!ServicesPreferences.isSearchVisible())
            } else {
                searchBox.text?.clear()
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            ServicesPreferences.servicesSearch -> {
                searchBoxState(animate = true, ServicesPreferences.isSearchVisible())
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
