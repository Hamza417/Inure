package app.simple.inure.ui.viewers

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.details.AdapterInformation
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.dialogs.miscellaneous.Error
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.factories.panels.PackageInfoFactory
import app.simple.inure.popups.viewers.PopupInformation
import app.simple.inure.viewmodels.viewers.CertificatesViewModel

class Certificate : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var viewModel: CertificatesViewModel
    private lateinit var packageInfoFactory: PackageInfoFactory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_certificate, container, false)

        recyclerView = view.findViewById(R.id.certificate_data_recycler_view)

        packageInfo = requireArguments().getParcelable("application_info")!!

        packageInfoFactory = PackageInfoFactory(requireActivity().application, packageInfo)
        viewModel = ViewModelProvider(this, packageInfoFactory).get(CertificatesViewModel::class.java)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startPostponedEnterTransition()

        viewModel.getCertificateData().observe(viewLifecycleOwner) {
            val adapterInformation = AdapterInformation(it)

            adapterInformation.setOnAdapterInformationCallbacks(object : AdapterInformation.Companion.AdapterInformationCallbacks {
                override fun onInformationClicked(view: View, string: String) {
                    PopupInformation(view, string)
                }
            })

            recyclerView.adapter = adapterInformation
        }

        viewModel.getError().observe(viewLifecycleOwner) {
            val e = Error.newInstance(it)
            e.show(childFragmentManager, "error_dialog")
            e.setOnErrorDialogCallbackListener(object : Error.Companion.ErrorDialogCallbacks {
                override fun onDismiss() {
                    requireActivity().onBackPressed()
                }
            })
        }
    }

    companion object {
        fun newInstance(applicationInfo: PackageInfo): Certificate {
            val args = Bundle()
            args.putParcelable("application_info", applicationInfo)
            val fragment = Certificate()
            fragment.arguments = args
            return fragment
        }
    }
}