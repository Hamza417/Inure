package app.simple.inure.ui.subviewers

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.details.AdapterInformation
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.factories.subpanels.ProviderInfoFactory
import app.simple.inure.models.ProviderInfoModel
import app.simple.inure.util.ParcelUtils.parcelable
import app.simple.inure.viewmodels.subviewers.ProviderInfoViewModel

class ProviderInfo : ScopedFragment() {

    private lateinit var name: TypeFaceTextView
    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var backButton: DynamicRippleImageButton
    private lateinit var providerInfoViewModel: ProviderInfoViewModel
    private lateinit var providerInfoFactory: ProviderInfoFactory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_activity_details, container, false)

        name = view.findViewById(R.id.activity_name)
        recyclerView = view.findViewById(R.id.activity_info_recycler_view)
        backButton = view.findViewById(R.id.activity_info_back_button)

        with(requireArguments().parcelable<ProviderInfoModel>(BundleConstants.providerInfo)!!) {
            packageInfo = requireArguments().parcelable(BundleConstants.packageInfo)!!
            this@ProviderInfo.name.text = name
            providerInfoFactory = ProviderInfoFactory(this, packageInfo)
        }

        providerInfoViewModel = ViewModelProvider(this, providerInfoFactory).get(ProviderInfoViewModel::class.java)

        startPostponedEnterTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        providerInfoViewModel.getProviderInfo().observe(viewLifecycleOwner) {
            recyclerView.adapter = AdapterInformation(it)
        }

        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    companion object {
        fun newInstance(providerInfoModel: ProviderInfoModel, packageInfo: PackageInfo): ProviderInfo {
            val args = Bundle()
            args.putParcelable(BundleConstants.providerInfo, providerInfoModel)
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            val fragment = ProviderInfo()
            fragment.arguments = args
            return fragment
        }
    }
}