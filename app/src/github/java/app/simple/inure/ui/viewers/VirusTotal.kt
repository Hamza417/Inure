package app.simple.inure.ui.viewers

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import app.simple.inure.R
import app.simple.inure.adapters.viewers.AdapterVirusTotal
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.decorations.padding.PaddingAwareLinearLayout
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.theme.ThemeDivider
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.WaveFillImageView
import app.simple.inure.dialogs.virustotal.VirusTotalAnalysisResult.Companion.showAnalysisResult
import app.simple.inure.dialogs.virustotal.VirusTotalMenu.Companion.VirusTotalMenuListener
import app.simple.inure.dialogs.virustotal.VirusTotalMenu.Companion.showVirusTotalMenu
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.preferences.VirusTotalPreferences
import app.simple.inure.services.VirusTotalClientService
import app.simple.inure.themes.manager.ThemeManager
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.IntentHelper.asUri
import app.simple.inure.util.IntentHelper.openInBrowser
import app.simple.inure.util.ParcelUtils.parcelable
import app.simple.inure.virustotal.VirusTotalResponse
import app.simple.inure.virustotal.VirusTotalResult
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch

class VirusTotal : ScopedFragment() {

    private lateinit var shield: WaveFillImageView
    private lateinit var status: TypeFaceTextView
    private lateinit var options: DynamicRippleImageButton
    private lateinit var recyclerView: CustomVerticalRecyclerView

    private var virusTotalClientService: VirusTotalClientService? = null
    private var serviceConnection: ServiceConnection? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_virustotal, container, false)

        shield = view.findViewById(R.id.shield)
        status = view.findViewById(R.id.status)
        options = view.findViewById(R.id.options)
        recyclerView = view.findViewById(R.id.recycler_view)

        packageInfo = requireArguments().parcelable(BundleConstants.packageInfo)!!

        return view
    }

    @OptIn(FlowPreview::class)
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

        if (requireArguments().getBoolean(HEADER_VISIBILITY, true).invert()) {
            view.findViewById<PaddingAwareLinearLayout>(R.id.header).visibility = View.GONE
            view.findViewById<ThemeDivider>(R.id.divider).visibility = View.GONE
        }

        options.setOnClickListener {
            childFragmentManager.showVirusTotalMenu().setVirusTotalMenuListener(object : VirusTotalMenuListener {
                override fun onRefetch() {
                    // virusTotalClientService?.refetch()
                }
            })
        }

        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                virusTotalClientService = (service as VirusTotalClientService.LocalBinder).getService()
                virusTotalClientService?.clearEverything(packageInfo)
                virusTotalClientService?.startUpload(packageInfo)

                viewLifecycleOwner.lifecycleScope.launch {
                    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        launch {
                            virusTotalClientService?.progressFlow?.sample(100L)?.collect { progress ->
                                when (progress.progressCode) {
                                    VirusTotalResult.Progress.CALCULATING -> {
                                        shield.setFillPercent(0.1F)
                                        status.text = getString(R.string.checking_hash)

                                    }
                                    VirusTotalResult.Progress.UPLOADING -> {
                                        shield.setFillPercent(0.1f + (progress.progress - 0.1f) / 99.9f * 0.5f)
                                        status.text = getString(R.string.uploading_file, progress.progress)
                                    }
                                    VirusTotalResult.Progress.UPLOAD_SUCCESS -> {
                                        shield.setFillPercent(0.6F)
                                        status.text = getString(R.string.done)
                                    }
                                    VirusTotalResult.Progress.HASH_RESULT -> {
                                        shield.setFillPercent(0.65F)
                                        status.text = getString(R.string.hash_found)
                                        Log.d(TAG, progress.status)
                                    }
                                    VirusTotalResult.Progress.POLLING -> {
                                        shield.setFillPercent(0.7F)
                                        shield.startPollingWave()
                                        status.text = getString(R.string.polling_for_response, progress.pollingAttempts)
                                    }
                                    else -> {
                                        shield.setWaveAmplitude(0F)
                                        status.text = buildString {
                                            append(getString(R.string.unknown))
                                            append(" ")
                                            append(progress.status)
                                        }
                                    }
                                }
                            }
                        }

                        launch {
                            virusTotalClientService?.successFlow?.collect { response ->
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

                                val adapter = AdapterVirusTotal(response, packageInfo)
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
                        }

                        launch {
                            virusTotalClientService?.failedFlow?.collect { error ->
                                showWarning(error.message)
                                shield.setWaveAmplitude(0F)
                            }
                        }

                        launch {
                            virusTotalClientService?.warningFlow?.collect { warning ->
                                showWarning(warning)
                                shield.setWaveAmplitude(0F)
                            }
                        }

                        launch {
                            virusTotalClientService?.exitFlow?.collect {
                                goBack()
                            }
                        }
                    }
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                virusTotalClientService = null
            }
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

    override fun onStart() {
        super.onStart()
        if (virusTotalClientService == null) {
            requireActivity().startService(VirusTotalClientService.newIntent(requireActivity()))
            requireActivity().bindService(
                    VirusTotalClientService.newIntent(requireActivity()),
                    serviceConnection!!,
                    Context.BIND_AUTO_CREATE
            )
        }
    }

    override fun onStop() {
        super.onStop()
        if (virusTotalClientService != null) {
            if (serviceConnection != null) {
                try {
                    requireActivity().unbindService(serviceConnection!!)
                    serviceConnection = null
                } catch (e: IllegalArgumentException) {
                    serviceConnection = null
                }
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

        fun newInstanceForInstaller(packageInfo: PackageInfo): VirusTotal {
            val fragment = VirusTotal()
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            args.putBoolean(HEADER_VISIBILITY, false)
            fragment.arguments = args
            return fragment
        }

        const val TAG = "VirusTotal"

        private const val SHIELD_VISIBILITY = "shield_visibility"
        private const val HEADER_VISIBILITY = "header_visibility"
    }
}
