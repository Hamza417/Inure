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
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.WaveFillImageView
import app.simple.inure.dialogs.virustotal.ShouldUpload
import app.simple.inure.dialogs.virustotal.ShouldUpload.Companion.showShouldUpload
import app.simple.inure.dialogs.virustotal.VirusTotalAnalysisResult.Companion.showAnalysisResult
import app.simple.inure.dialogs.virustotal.VirusTotalMenu.Companion.showVirusTotalMenu
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.preferences.VirusTotalPreferences
import app.simple.inure.services.VirusTotalClientService
import app.simple.inure.themes.manager.ThemeManager
import app.simple.inure.util.IntentHelper.asUri
import app.simple.inure.util.IntentHelper.openInBrowser
import app.simple.inure.util.LocaleUtils
import app.simple.inure.util.ParcelUtils.parcelable
import app.simple.inure.virustotal.VirusTotalResponse
import app.simple.inure.virustotal.VirusTotalResult
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class VirusTotal : ScopedFragment() {

    private lateinit var shield: WaveFillImageView
    private lateinit var status: TypeFaceTextView
    private lateinit var elapsed: TypeFaceTextView
    private lateinit var options: DynamicRippleImageButton
    private lateinit var recyclerView: CustomVerticalRecyclerView

    private var virusTotalClientService: VirusTotalClientService? = null
    private var serviceConnection: ServiceConnection? = null
    private var elapsedJob: Job? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_virustotal, container, false)
        shield = view.findViewById(R.id.shield)
        status = view.findViewById(R.id.status)
        elapsed = view.findViewById(R.id.elapsed)
        options = view.findViewById(R.id.options)
        recyclerView = view.findViewById(R.id.recycler_view)
        packageInfo = requireArguments().parcelable(BundleConstants.packageInfo)!!
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()
        setLoaderType()
        shield.setUnfilledColor(ThemeManager.theme.switchViewTheme.switchOffColor)
        shield.setWaveColor(AppearancePreferences.getAccentColor())

        handleVisibility(
                shield, status, elapsed, visible = requireArguments().getBoolean(SHIELD_VISIBILITY, true))

        options.setOnClickListener {
            childFragmentManager.showVirusTotalMenu()
        }

        serviceConnection = createServiceConnection(view)
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
        val loaderType = VirusTotalPreferences.getLoaderType()
        val iconRes = when (loaderType) {
            VirusTotalPreferences.LOADER_TYPE_POLICY -> R.drawable.ic_policy
            VirusTotalPreferences.LOADER_TYPE_SECURITY -> R.drawable.ic_security
            VirusTotalPreferences.LOADER_TYPE_FIND_IN_PAGE -> R.drawable.ic_find_in_page
            VirusTotalPreferences.LOADER_TYPE_SEARCH -> R.drawable.ic_search
            VirusTotalPreferences.LOADER_TYPE_FINGERPRINT -> R.drawable.ic_fingerprint
            else -> R.drawable.ic_policy
        }
        shield.setImageResource(iconRes)
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
        unbindServiceIfNeeded()
    }

    private fun startElapsedTimer(startTimeMillis: Long) {
        elapsedJob?.cancel()
        elapsedJob = viewLifecycleOwner.lifecycleScope.launch {
            while (true) {
                val elapsedMillis = System.currentTimeMillis() - startTimeMillis
                elapsed.text = formatElapsedTime(elapsedMillis)
                delay(1000)
            }
        }
    }

    private fun stopElapsedTimer() {
        elapsedJob?.cancel()
    }

    private fun formatElapsedTime(millis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        return String.format(LocaleUtils.getAppLocale(), "%02d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        stopElapsedTimer()
        super.onDestroy()
    }

    private fun handleVisibility(vararg views: View, visible: Boolean) {
        val visibility = if (visible) View.VISIBLE else View.GONE
        views.forEach { it.visibility = visibility }
    }

    @Suppress("SameParameterValue")
    private fun animateAlpha(vararg views: View, alpha: Float, duration: Long = 250L) {
        views.forEach {
            it.animate().alpha(alpha).setDuration(duration).start()
        }
    }

    private fun unbindServiceIfNeeded() {
        if (virusTotalClientService != null && serviceConnection != null) {
            try {
                requireActivity().unbindService(serviceConnection!!)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            } finally {
                serviceConnection = null
            }
        }
    }

    private fun createServiceConnection(view: View): ServiceConnection {
        return object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                virusTotalClientService = (service as VirusTotalClientService.LocalBinder).getService()
                virusTotalClientService?.clearEverything(packageInfo)
                startElapsedTimer(System.currentTimeMillis())
                virusTotalClientService?.scanFile(packageInfo)

                viewLifecycleOwner.lifecycleScope.launch {
                    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        launch { collectProgress() }
                        launch { collectSuccess() }
                        launch { collectFailure() }
                        launch { collectWarning() }
                        launch { collectExit() }
                    }
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                virusTotalClientService = null
            }
        }
    }

    @OptIn(FlowPreview::class)
    private suspend fun collectProgress() {
        virusTotalClientService?.progressFlow?.sample(100L)?.collect { progress ->
            when (progress.progressCode) {
                VirusTotalResult.Progress.CALCULATING -> {
                    shield.setFillPercent(0.1F)
                    status.text = getString(R.string.checking_hash)
                }
                VirusTotalResult.Progress.UPLOADING -> {
                    shield.setWaveAmplitude(20F)
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
                VirusTotalResult.Progress.HASH_NOT_FOUND -> {
                    shield.setWaveAmplitude(0F)
                    status.text = getString(R.string.not_available)
                    childFragmentManager.showShouldUpload(packageInfo)
                        .setOnShouldUploadListener(object : ShouldUpload.Companion.ShouldUploadListener {
                            override fun onYes() {
                                virusTotalClientService?.startUpload(packageInfo)
                            }

                            override fun onClose() {
                                goBack()
                            }
                        })
                }
                VirusTotalResult.Progress.POLLING -> {
                    shield.setFillPercent(0.7F)
                    shield.startPollingWave()
                    status.text = getString(R.string.scanning_desc)
                }
                else -> {
                    shield.setWaveAmplitude(0F)
                    status.text = buildString {
                        append(getString(R.string.unknown))
                        append(" ")
                        append(progress.status)
                    }
                    stopElapsedTimer()
                }
            }
        }
    }

    private suspend fun collectSuccess() {
        virusTotalClientService?.successFlow?.collect { response ->
            stopElapsedTimer()
            shield.setFillPercent(1.0F)
            animateAlpha(shield, status, elapsed, alpha = 0F)
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

    private suspend fun collectFailure() {
        virusTotalClientService?.failedFlow?.collect { error ->
            showWarning(error.message)
            shield.setWaveAmplitude(0F)
            stopElapsedTimer()
        }
    }

    private suspend fun collectWarning() {
        virusTotalClientService?.warningFlow?.collect { warning ->
            showWarning(warning)
            shield.setWaveAmplitude(0F)
            stopElapsedTimer()
        }
    }

    private suspend fun collectExit() {
        virusTotalClientService?.exitFlow?.collect {
            goBack()
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