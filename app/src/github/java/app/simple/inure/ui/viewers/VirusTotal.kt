package app.simple.inure.ui.viewers

import android.content.pm.PackageInfo
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.WaveFillImageView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.factories.viewers.VirusTotalViewModelFactory
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.ParcelUtils.parcelable
import app.simple.inure.viewmodels.viewers.VirusTotalViewModel
import app.simple.inure.virustotal.VirusTotalResult

class VirusTotal : ScopedFragment() {

    private lateinit var shield: WaveFillImageView
    private lateinit var status: TypeFaceTextView
    private lateinit var virusTotalViewModel: VirusTotalViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_virustotal, container, false)

        shield = view.findViewById(R.id.shield)
        status = view.findViewById(R.id.status)

        packageInfo = requireArguments().parcelable(BundleConstants.packageInfo)!!
        virusTotalViewModel = ViewModelProvider(this, VirusTotalViewModelFactory(packageInfo))[VirusTotalViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        shield.setUnfilledColor(Color.LTGRAY)
        shield.setWaveColor(AppearancePreferences.getAccentColor())

        if (requireArguments().getBoolean(SHIELD_VISIBILITY, true).invert()) {
            shield.visibility = View.GONE
            status.visibility = View.GONE
        }

        virusTotalViewModel.getFailed().observe(viewLifecycleOwner) {
            showWarning(it.message)
            shield.setWaveAmplitude(0F)
        }

        virusTotalViewModel.getProgress().observe(viewLifecycleOwner) {
            when (it.progressCode) {
                VirusTotalResult.Progress.CALCULATING -> {
                    shield.setFillPercent(0.1F)
                    status.text = getString(R.string.checking_hash)

                }
                VirusTotalResult.Progress.UPLOADING -> {
                    shield.setFillPercent(0.1f + (it.progress - 0.1f) / 99.9f * 0.5f)
                    status.text = getString(R.string.uploading_file, it.progress.toInt())
                }
                VirusTotalResult.Progress.UPLOAD_SUCCESS -> {
                    shield.setFillPercent(0.6F)
                    status.text = getString(R.string.done)
                }
                VirusTotalResult.Progress.HASH_RESULT -> {
                    shield.setFillPercent(0.7F)
                    status.text = getString(R.string.hash_found)
                    Log.d(TAG, it.status)
                }
                VirusTotalResult.Progress.POLLING -> {
                    shield.setFillPercent(0.75F)
                    shield.startPollingWave()
                    status.text = getString(R.string.polling_for_response, it.pollingAttempts)
                }
                else -> {
                    shield.setWaveAmplitude(0F)
                    status.text = buildString {
                        append(getString(R.string.unknown))
                        append(" ")
                        append(it.status)
                    }
                }
            }
        }

        virusTotalViewModel.getResponse().observe(viewLifecycleOwner) {
            shield.setFillPercent(1.0F)
            shield.animate()
                .alpha(0F)
                .setDuration(250L)
                .setStartDelay(1000L)
                .start()

            status.animate()
                .alpha(0F)
                .setDuration(250L)
                .setStartDelay(250L)
                .start()

            requireArguments().putBoolean(SHIELD_VISIBILITY, false)
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

        private const val SHIELD_VISIBILITY = "shield_visibility"
    }
}
