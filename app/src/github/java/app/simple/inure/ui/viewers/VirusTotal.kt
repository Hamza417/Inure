package app.simple.inure.ui.viewers

import android.content.pm.PackageInfo
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.views.LoaderImageView
import app.simple.inure.decorations.views.ShimmerImageView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.factories.viewers.VirusTotalViewModelFactory
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.util.ParcelUtils.parcelable
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.viewmodels.viewers.VirusTotalViewModel
import app.simple.inure.virustotal.VirusTotalResult

class VirusTotal : ScopedFragment() {

    private lateinit var shield: ShimmerImageView
    private lateinit var loader: LoaderImageView
    private lateinit var virusTotalViewModel: VirusTotalViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_virustotal, container, false)

        shield = view.findViewById(R.id.shield)
        loader = view.findViewById(R.id.loader)

        packageInfo = requireArguments().parcelable(BundleConstants.packageInfo)!!
        virusTotalViewModel = ViewModelProvider(this, VirusTotalViewModelFactory(packageInfo))[VirusTotalViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        shield.imageTintList = ColorStateList.valueOf(AppearancePreferences.getAccentColor())

        virusTotalViewModel.getFailed().observe(viewLifecycleOwner) {
            showWarning(it.message)
            loader.error()
            loader.gone(animate = true)
        }

        virusTotalViewModel.getProgress().observe(viewLifecycleOwner) {
            when (it.progressCode) {
                VirusTotalResult.Progress.CALCULATING -> {
                    loader.start()
                }
                VirusTotalResult.Progress.UPLOADING -> {

                }
                VirusTotalResult.Progress.UPLOAD_SUCCESS -> {

                }
                VirusTotalResult.Progress.POLLING -> {

                }
                else -> {

                }
            }
        }

        virusTotalViewModel.getResponse().observe(viewLifecycleOwner) {
            loader.loaded()
            loader.gone(animate = true)
        }

        virusTotalViewModel.getWarning().observe(viewLifecycleOwner) {
            showWarning(it)
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