package app.simple.inure.dialogs.virustotal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.views.WaveFillImageView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.preferences.VirusTotalPreferences

class VirusTotalMenu : ScopedBottomSheetFragment() {

    private lateinit var refetch: DynamicRippleTextView
    private lateinit var policy: WaveFillImageView
    private lateinit var security: WaveFillImageView
    private lateinit var findInPage: WaveFillImageView
    private lateinit var search: WaveFillImageView
    private lateinit var fingerprint: WaveFillImageView
    private lateinit var openSettings: DynamicRippleTextView

    private var virusTotalMenuListener: VirusTotalMenuListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_menu_virustotal, container, false)

        refetch = view.findViewById(R.id.refetch_report)
        policy = view.findViewById(R.id.policy)
        security = view.findViewById(R.id.security)
        findInPage = view.findViewById(R.id.find_in_page)
        search = view.findViewById(R.id.search)
        fingerprint = view.findViewById(R.id.fingerprint)
        openSettings = view.findViewById(R.id.dialog_open_apps_settings)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        policy.setSimpleDefaults()
        security.setSimpleDefaults()
        findInPage.setSimpleDefaults()
        search.setSimpleDefaults()
        fingerprint.setSimpleDefaults()

        refetch.setOnClickListener {
            virusTotalMenuListener?.onRefetch().also {
                dismiss()
            }
        }

        policy.setOnClickListener {
            VirusTotalPreferences.setLoaderType(VirusTotalPreferences.LOADER_TYPE_POLICY)
            policy.startPollingWave()
        }

        security.setOnClickListener {
            VirusTotalPreferences.setLoaderType(VirusTotalPreferences.LOADER_TYPE_SECURITY)
            security.startPollingWave()
        }

        findInPage.setOnClickListener {
            VirusTotalPreferences.setLoaderType(VirusTotalPreferences.LOADER_TYPE_FIND_IN_PAGE)
            findInPage.startPollingWave()
        }

        search.setOnClickListener {
            VirusTotalPreferences.setLoaderType(VirusTotalPreferences.LOADER_TYPE_SEARCH)
            search.startPollingWave()
        }

        fingerprint.setOnClickListener {
            VirusTotalPreferences.setLoaderType(VirusTotalPreferences.LOADER_TYPE_FINGERPRINT)
            fingerprint.startPollingWave()
        }

        openSettings.setOnClickListener {
            openSettings()
        }
    }

    fun setVirusTotalMenuListener(listener: VirusTotalMenuListener) {
        this.virusTotalMenuListener = listener
    }

    companion object {
        fun newInstance(): VirusTotalMenu {
            val args = Bundle()
            val fragment = VirusTotalMenu()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showVirusTotalMenu(): VirusTotalMenu {
            val dialog = newInstance()
            dialog.show(this, TAG)
            return dialog
        }

        const val TAG = "VirusTotalMenu"

        interface VirusTotalMenuListener {
            fun onRefetch()
        }
    }
}