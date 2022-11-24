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
import app.simple.inure.adapters.details.AdapterOperations
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.dialogs.app.Sure
import app.simple.inure.extensions.fragments.SearchBarScopedFragment
import app.simple.inure.factories.panels.PackageInfoFactory
import app.simple.inure.interfaces.fragments.SureCallbacks
import app.simple.inure.models.AppOpsModel
import app.simple.inure.preferences.OperationsPreferences
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.viewmodels.viewers.OperationsViewModel

class Operations : SearchBarScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var options: DynamicRippleImageButton
    private lateinit var operationsViewModel: OperationsViewModel
    private lateinit var packageInfoFactory: PackageInfoFactory
    private var adapterOperations: AdapterOperations? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_operations, container, false)

        recyclerView = view.findViewById(R.id.operations_recycler_view)
        options = view.findViewById(R.id.operations_option_btn)
        search = view.findViewById(R.id.operations_search_btn)
        searchBox = view.findViewById(R.id.operations_search)
        title = view.findViewById(R.id.operations_title)

        packageInfoFactory = PackageInfoFactory(packageInfo)
        operationsViewModel = ViewModelProvider(this, packageInfoFactory)[OperationsViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()
        fullVersionCheck()
        options.gone()
        searchBoxState(false, OperationsPreferences.isSearchVisible())

        operationsViewModel.getAppOpsData().observe(viewLifecycleOwner) {
            adapterOperations = AdapterOperations(it, searchBox.text.toString().trim())

            adapterOperations?.setOnOpsCheckedChangeListener(object : AdapterOperations.Companion.AdapterOpsCallbacks {
                override fun onCheckedChanged(appOpsModel: AppOpsModel, position: Int) {
                    val p0 = Sure.newInstance()
                    p0.setOnSureCallbackListener(object : SureCallbacks {
                        override fun onSure() {
                            operationsViewModel.updateAppOpsState(appOpsModel, position)
                        }

                        override fun onCancel() {
                            adapterOperations?.updateOperation(appOpsModel, position)
                        }
                    })
                    p0.show(childFragmentManager, "sure")
                }
            })

            recyclerView.adapter = adapterOperations
        }

        operationsViewModel.getAppOpsState().observe(viewLifecycleOwner) {
            adapterOperations?.updateOperation(it.first, it.second)
        }

        searchBox.doOnTextChanged { text, _, _, _ ->
            if (searchBox.isFocused) {
                operationsViewModel.loadAppOpsData(text.toString().trim())
            }
        }

        search.setOnClickListener {
            if (searchBox.text.isNullOrEmpty()) {
                OperationsPreferences.setSearchVisibility(!OperationsPreferences.isSearchVisible())
            } else {
                searchBox.text?.clear()
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            OperationsPreferences.operationsSearch -> {
                searchBoxState(true, OperationsPreferences.isSearchVisible())
            }
        }
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo): Operations {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            val fragment = Operations()
            fragment.arguments = args
            return fragment
        }
    }
}