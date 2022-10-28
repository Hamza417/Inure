package app.simple.inure.ui.installer

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.details.AdapterInformation
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.factories.panels.CertificateViewModelFactory
import app.simple.inure.popups.viewers.PopupInformation
import app.simple.inure.viewmodels.viewers.CertificatesViewModel
import java.io.File

class Certificate : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var viewModel: CertificatesViewModel
    private lateinit var certificateViewModelFactory: CertificateViewModelFactory

    private var file: File? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.installer_fragment_information, container, false)

        recyclerView = view.findViewById(R.id.information_recycler_view)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            file = requireArguments().getSerializable(BundleConstants.file, File::class.java)
        } else {
            @Suppress("DEPRECATION")
            file = requireArguments().getSerializable(BundleConstants.file) as File
        }

        certificateViewModelFactory = CertificateViewModelFactory(null, file)
        viewModel = ViewModelProvider(this, certificateViewModelFactory)[CertificatesViewModel::class.java]

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
            showError(it)
        }
    }

    companion object {
        fun newInstance(file: File?): Certificate {
            val args = Bundle()
            args.putSerializable(BundleConstants.file, file)
            val fragment = Certificate()
            fragment.arguments = args
            return fragment
        }
    }
}