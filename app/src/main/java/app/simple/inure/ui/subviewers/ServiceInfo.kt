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
import app.simple.inure.factories.subpanels.ServiceInfoFactory
import app.simple.inure.models.ServiceInfoModel
import app.simple.inure.util.ParcelUtils.parcelable
import app.simple.inure.viewmodels.subviewers.ServiceInfoViewModel

class ServiceInfo : ScopedFragment() {

    private lateinit var name: TypeFaceTextView
    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var backButton: DynamicRippleImageButton
    private lateinit var serviceInfoViewModel: ServiceInfoViewModel
    private lateinit var serviceInfoFactory: ServiceInfoFactory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_activity_details, container, false)

        name = view.findViewById(R.id.activity_name)
        recyclerView = view.findViewById(R.id.activity_info_recycler_view)
        backButton = view.findViewById(R.id.activity_info_back_button)

        with(requireArguments().parcelable<ServiceInfoModel>(BundleConstants.serviceInfo)!!) {
            packageInfo = requireArguments().parcelable(BundleConstants.packageInfo)!!
            this@ServiceInfo.name.text = name
            serviceInfoFactory = ServiceInfoFactory(this, packageInfo)
        }

        serviceInfoViewModel = ViewModelProvider(this, serviceInfoFactory).get(ServiceInfoViewModel::class.java)

        startPostponedEnterTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        serviceInfoViewModel.getServicesInfo().observe(viewLifecycleOwner) {
            recyclerView.adapter = AdapterInformation(it)
        }

        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    companion object {
        fun newInstance(serviceInfoModel: ServiceInfoModel, packageInfo: PackageInfo): ServiceInfo {
            val args = Bundle()
            args.putParcelable(BundleConstants.serviceInfo, serviceInfoModel)
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            val fragment = ServiceInfo()
            fragment.arguments = args
            return fragment
        }
    }
}