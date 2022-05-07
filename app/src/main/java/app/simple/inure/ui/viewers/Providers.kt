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
import app.simple.inure.adapters.details.AdapterProviders
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
import app.simple.inure.models.ProviderInfoModel
import app.simple.inure.popups.viewers.PopupProvidersMenu
import app.simple.inure.preferences.ProvidersPreferences
import app.simple.inure.ui.subviewers.ProviderInfo
import app.simple.inure.util.FragmentHelper
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.viewers.ProvidersViewModel

class Providers : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var search: DynamicRippleImageButton
    private lateinit var title: TypeFaceTextView
    private lateinit var searchBox: TypeFaceEditTextDynamicCorner

    private var adapterProviders: AdapterProviders? = null
    private lateinit var providersViewModel: ProvidersViewModel
    private lateinit var packageInfoFactory: PackageInfoFactory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_provider, container, false)

        recyclerView = view.findViewById(R.id.providers_recycler_view)
        search = view.findViewById(R.id.providers_search_btn)
        searchBox = view.findViewById(R.id.providers_search)
        title = view.findViewById(R.id.providers_title)

        packageInfo = requireArguments().getParcelable(BundleConstants.packageInfo)!!
        packageInfoFactory = PackageInfoFactory(requireActivity().application, packageInfo)
        providersViewModel = ViewModelProvider(this, packageInfoFactory).get(ProvidersViewModel::class.java)

        startPostponedEnterTransition()
        searchBoxState()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        providersViewModel.getProviders().observe(viewLifecycleOwner) {
            adapterProviders = AdapterProviders(it, packageInfo, searchBox.text.toString().trim())
            recyclerView.adapter = adapterProviders

            adapterProviders?.setOnProvidersCallbackListener(object : AdapterProviders.Companion.ProvidersCallbacks {
                override fun onProvidersClicked(providerInfoModel: ProviderInfoModel) {
                    clearExitTransition()
                    FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                ProviderInfo.newInstance(providerInfoModel, packageInfo),
                                                "provider_info")
                }

                override fun onProvidersLongPressed(packageId: String, packageInfo: PackageInfo, icon: View, isComponentEnabled: Boolean, position: Int) {
                    val v = PopupProvidersMenu(icon, isComponentEnabled)

                    v.setOnMenuClickListener(object : PopupMenuCallback {
                        override fun onMenuItemClicked(source: String) {
                            when (source) {
                                getString(R.string.enable), getString(R.string.disable) -> {
                                    val p = ComponentState.newInstance(packageInfo, packageId, isComponentEnabled)
                                    p.setOnComponentStateChangeListener(object : ComponentState.Companion.ComponentStatusCallbacks {
                                        override fun onSuccess() {
                                            adapterProviders?.notifyItemChanged(position)
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
                    providersViewModel.getProvidersData(text.toString().trim())
                }
            }
        }

        providersViewModel.getError().observe(viewLifecycleOwner) {
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
                ProvidersPreferences.setSearchVisibility(!ProvidersPreferences.isSearchVisible())
            } else {
                searchBox.text?.clear()
            }
        }
    }

    private fun searchBoxState() {
        if (ProvidersPreferences.isSearchVisible()) {
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
            ProvidersPreferences.providersSearch -> {
                searchBoxState()
            }
        }
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo): Providers {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            val fragment = Providers()
            fragment.arguments = args
            return fragment
        }
    }
}
