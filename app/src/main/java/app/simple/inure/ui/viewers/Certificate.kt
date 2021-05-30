package app.simple.inure.ui.viewers

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.details.AdapterInformation
import app.simple.inure.decorations.views.CustomRecyclerView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.viewmodels.factory.ApplicationInfoFactory
import app.simple.inure.viewmodels.viewers.CertificatesViewModel

class Certificate : ScopedFragment() {

    private lateinit var recyclerView: CustomRecyclerView
    private lateinit var viewModel: CertificatesViewModel
    private lateinit var applicationInfoFactory: ApplicationInfoFactory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_certificate, container, false)

        recyclerView = view.findViewById(R.id.certificate_data_recycler_view)

        applicationInfo = requireArguments().getParcelable("application_info")!!

        applicationInfoFactory = ApplicationInfoFactory(requireActivity().application, applicationInfo)
        viewModel = ViewModelProvider(this, applicationInfoFactory).get(CertificatesViewModel::class.java)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startPostponedEnterTransition()

        viewModel.getCertificateData().observe(viewLifecycleOwner, {
            recyclerView.adapter = AdapterInformation(it)
        })
    }

    companion object {
        fun newInstance(applicationInfo: ApplicationInfo): Certificate {
            val args = Bundle()
            args.putParcelable("application_info", applicationInfo)
            val fragment = Certificate()
            fragment.arguments = args
            return fragment
        }
    }
}