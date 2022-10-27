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
import app.simple.inure.adapters.details.AdapterReceivers
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.dialogs.action.ComponentState
import app.simple.inure.extensions.fragments.SearchBarScopedFragment
import app.simple.inure.extensions.popup.PopupMenuCallback
import app.simple.inure.factories.panels.PackageInfoFactory
import app.simple.inure.models.ActivityInfoModel
import app.simple.inure.popups.viewers.PopupReceiversMenu
import app.simple.inure.preferences.ReceiversPreferences
import app.simple.inure.ui.subviewers.ActivityInfo
import app.simple.inure.viewmodels.viewers.ReceiversViewModel

class Receivers : SearchBarScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView

    private var adapterReceivers: AdapterReceivers? = null
    private lateinit var receiversViewModel: ReceiversViewModel
    private lateinit var packageInfoFactory: PackageInfoFactory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_receivers, container, false)

        recyclerView = view.findViewById(R.id.receivers_recycler_view)
        search = view.findViewById(R.id.receivers_search_btn)
        searchBox = view.findViewById(R.id.receivers_search)
        title = view.findViewById(R.id.receivers_title)

        packageInfoFactory = PackageInfoFactory(packageInfo)
        receiversViewModel = ViewModelProvider(this, packageInfoFactory)[ReceiversViewModel::class.java]

        searchBoxState(false, ReceiversPreferences.isSearchVisible())
        startPostponedEnterTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        receiversViewModel.getReceivers().observe(viewLifecycleOwner) {
            adapterReceivers = AdapterReceivers(it, packageInfo, searchBox.text.toString().trim())
            recyclerView.adapter = adapterReceivers

            adapterReceivers?.setOnReceiversCallbackListener(object : AdapterReceivers.Companion.ReceiversCallbacks {
                override fun onReceiverClicked(activityInfoModel: ActivityInfoModel) {
                    openFragmentSlide(ActivityInfo.newInstance(activityInfoModel, packageInfo), "activity_info")
                }

                override fun onReceiverLongPressed(packageId: String, packageInfo: PackageInfo, icon: View, isComponentEnabled: Boolean, position: Int) {
                    val popupReceiversMenu = PopupReceiversMenu(icon, isComponentEnabled)

                    popupReceiversMenu.setOnMenuClickListener(object : PopupMenuCallback {
                        override fun onMenuItemClicked(source: String) {
                            when (source) {
                                getString(R.string.enable), getString(R.string.disable) -> {
                                    val componentState = ComponentState.newInstance(packageInfo, packageId, isComponentEnabled)
                                    componentState.setOnComponentStateChangeListener(object : ComponentState.Companion.ComponentStatusCallbacks {
                                        override fun onSuccess() {
                                            adapterReceivers?.notifyItemChanged(position)
                                        }
                                    })
                                    componentState.show(childFragmentManager, "component_state")
                                }
                            }
                        }
                    })
                }
            })

            searchBox.doOnTextChanged { text, _, _, _ ->
                if (searchBox.isFocused) {
                    receiversViewModel.getReceiversData(text.toString().trim())
                }
            }
        }

        receiversViewModel.getError().observe(viewLifecycleOwner) {
            showError(it)
        }

        receiversViewModel.notFound.observe(viewLifecycleOwner) {
            showWarning(R.string.no_receivers_found)
        }

        search.setOnClickListener {
            if (searchBox.text.isNullOrEmpty()) {
                ReceiversPreferences.setSearchVisibility(!ReceiversPreferences.isSearchVisible())
            } else {
                searchBox.text?.clear()
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            ReceiversPreferences.receiversSearch -> {
                searchBoxState(animate = true, ReceiversPreferences.isSearchVisible())
            }
        }
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