package app.simple.inure.ui.viewers

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.factories.viewers.VirusTotalViewModelFactory
import app.simple.inure.util.ParcelUtils.parcelable
import app.simple.inure.viewmodels.viewers.VirusTotalViewModel

class VirusTotal : ScopedFragment() {

    private lateinit var response: TypeFaceTextView
    private lateinit var progress: TypeFaceTextView
    private lateinit var virusTotalViewModel: VirusTotalViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_virustotal, container, false)

        response = view.findViewById(R.id.response)
        progress = view.findViewById(R.id.progress)

        packageInfo = requireArguments().parcelable(BundleConstants.packageInfo)!!
        virusTotalViewModel = ViewModelProvider(this, VirusTotalViewModelFactory(packageInfo))[VirusTotalViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        virusTotalViewModel.getFailed().observe(viewLifecycleOwner) {
            showWarning(it)
        }

        virusTotalViewModel.getProgress().observe(viewLifecycleOwner) {
            progress.text = it
        }

        virusTotalViewModel.getResponse().observe(viewLifecycleOwner) {
            response.text = it.toString()
        }
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo): VirusTotal {
            val fragment = VirusTotal()
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            fragment.arguments = args
            return fragment
        }

        const val TAG = "VirusTotal"
    }
}