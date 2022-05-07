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
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceEditTextDynamicCorner
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.dialogs.action.ComponentState
import app.simple.inure.dialogs.miscellaneous.Error
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.extension.popup.PopupMenuCallback
import app.simple.inure.factories.panels.PackageInfoFactory
import app.simple.inure.models.ActivityInfoModel
import app.simple.inure.popups.viewers.PopupReceiversMenu
import app.simple.inure.preferences.ReceiversPreferences
import app.simple.inure.ui.subviewers.ActivityInfo
import app.simple.inure.util.FragmentHelper
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.viewers.ReceiversViewModel

class Receivers : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var search: DynamicRippleImageButton
    private lateinit var title: TypeFaceTextView
    private lateinit var searchBox: TypeFaceEditTextDynamicCorner

    private var adapterReceivers: AdapterReceivers? = null
    private lateinit var receiversViewModel: ReceiversViewModel
    private lateinit var packageInfoFactory: PackageInfoFactory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_receivers, container, false)

        recyclerView = view.findViewById(R.id.receivers_recycler_view)
        search = view.findViewById(R.id.receivers_search_btn)
        searchBox = view.findViewById(R.id.receivers_search)
        title = view.findViewById(R.id.receivers_title)

        packageInfo = requireArguments().getParcelable(BundleConstants.packageInfo)!!
        packageInfoFactory = PackageInfoFactory(requireActivity().application, packageInfo)
        receiversViewModel = ViewModelProvider(this, packageInfoFactory).get(ReceiversViewModel::class.java)

        searchBoxState()
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
                    clearExitTransition()
                    FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                ActivityInfo.newInstance(activityInfoModel, packageInfo),
                                                "activity_info")
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
                ReceiversPreferences.setSearchVisibility(!ReceiversPreferences.isSearchVisible())
            } else {
                searchBox.text?.clear()
            }
        }
    }

    private fun searchBoxState() {
        if (ReceiversPreferences.isSearchVisible()) {
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
            ReceiversPreferences.receiversSearch -> {
                searchBoxState()
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