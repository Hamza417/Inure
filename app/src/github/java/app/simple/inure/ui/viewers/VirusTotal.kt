package app.simple.inure.ui.viewers

import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import app.simple.inure.R
import app.simple.inure.adapters.viewers.AdapterVirusTotal
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.WaveFillImageView
import app.simple.inure.dialogs.virustotal.VirusTotalAnalysisResult.Companion.showAnalysisResult
import app.simple.inure.dialogs.virustotal.VirusTotalMenu.Companion.showVirusTotalMenu
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.factories.viewers.VirusTotalViewModelFactory
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.preferences.VirusTotalPreferences
import app.simple.inure.themes.manager.ThemeManager
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.IntentHelper.asUri
import app.simple.inure.util.IntentHelper.openInBrowser
import app.simple.inure.util.ParcelUtils.parcelable
import app.simple.inure.viewmodels.viewers.VirusTotalViewModel
import app.simple.inure.virustotal.VirusTotalResponse
import app.simple.inure.virustotal.VirusTotalResult

class VirusTotal : ScopedFragment() {

    private lateinit var shield: WaveFillImageView
    private lateinit var status: TypeFaceTextView
    private lateinit var options: DynamicRippleImageButton
    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var virusTotalViewModel: VirusTotalViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_virustotal, container, false)

        shield = view.findViewById(R.id.shield)
        status = view.findViewById(R.id.status)
        options = view.findViewById(R.id.options)
        recyclerView = view.findViewById(R.id.recycler_view)

        packageInfo = requireArguments().parcelable(BundleConstants.packageInfo)!!
        virusTotalViewModel = ViewModelProvider(
                this, VirusTotalViewModelFactory(packageInfo))[VirusTotalViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        setLoaderType()
        shield.setUnfilledColor(ThemeManager.theme.switchViewTheme.switchOffColor)
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
                    status.text = getString(R.string.uploading_file, it.progress)
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
                .start()

            status.animate()
                .alpha(0F)
                .setDuration(250L)
                .start()

            requireArguments().putBoolean(SHIELD_VISIBILITY, false)

            val adapter = AdapterVirusTotal(it, packageInfo)
            recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

            adapter.setAdapterVirusTotalListener(object : AdapterVirusTotal.Companion.AdapterVirusTotalListener {
                override fun onAnalysisResult(response: VirusTotalResponse) {
                    childFragmentManager.showAnalysisResult(response.lastAnalysisResults ?: HashMap())
                }

                override fun onOpenReportPage(response: VirusTotalResponse) {
                    buildString {
                        append("https://www.virustotal.com/gui/file/")
                        append(response.sha256)
                    }.asUri().openInBrowser(requireContext())
                }
            })

            recyclerView.adapter = adapter
        }

        virusTotalViewModel.getWarning().observe(viewLifecycleOwner) {
            showWarning(it)
        }

        options.setOnClickListener {
            childFragmentManager.showVirusTotalMenu()
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            VirusTotalPreferences.LOADER_TYPE -> {
                setLoaderType()
            }
        }
    }

    private fun setLoaderType() {
        when (VirusTotalPreferences.getLoaderType()) {
            VirusTotalPreferences.LOADER_TYPE_POLICY -> {
                shield.setImageResource(R.drawable.ic_policy)
            }
            VirusTotalPreferences.LOADER_TYPE_SECURITY -> {
                shield.setImageResource(R.drawable.ic_security)
            }
            VirusTotalPreferences.LOADER_TYPE_FIND_IN_PAGE -> {
                shield.setImageResource(R.drawable.ic_find_in_page)
            }
            VirusTotalPreferences.LOADER_TYPE_SEARCH -> {
                shield.setImageResource(R.drawable.ic_search)
            }
            VirusTotalPreferences.LOADER_TYPE_FINGERPRINT -> {
                shield.setImageResource(R.drawable.ic_fingerprint)
            }
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
