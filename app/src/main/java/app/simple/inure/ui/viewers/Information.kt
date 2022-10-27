package app.simple.inure.ui.viewers

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
import app.simple.inure.decorations.views.CustomProgressBar
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.factories.panels.PackageInfoFactory
import app.simple.inure.popups.viewers.PopupInformation
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.viewmodels.viewers.AppInformationViewModel

class Information : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var back: DynamicRippleImageButton
    private lateinit var progress: CustomProgressBar

    private lateinit var viewModel: AppInformationViewModel
    private lateinit var packageInfoFactory: PackageInfoFactory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = layoutInflater.inflate(R.layout.fragment_information, container, false)

        recyclerView = view.findViewById(R.id.information_data_recycler_view)
        back = view.findViewById(R.id.app_info_back_button)
        progress = view.findViewById(R.id.information_data_progress)

        packageInfoFactory = PackageInfoFactory(packageInfo)
        viewModel = ViewModelProvider(this, packageInfoFactory)[AppInformationViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startPostponedEnterTransition()

        viewModel.getInformation().observe(viewLifecycleOwner) {
            progress.gone()
            val adapterInformation = AdapterInformation(it)

            adapterInformation.setOnAdapterInformationCallbacks(object : AdapterInformation.Companion.AdapterInformationCallbacks {
                override fun onInformationClicked(view: View, string: String) {
                    PopupInformation(view, string)
                }
            })

            recyclerView.adapter = adapterInformation
        }

        back.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo): Information {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            val fragment = Information()
            fragment.arguments = args
            return fragment
        }
    }
}