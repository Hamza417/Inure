package app.simple.inure.dialogs.app

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.simple.inure.R
import app.simple.inure.constants.IntentConstants
import app.simple.inure.constants.LicenseConstants
import app.simple.inure.decorations.theme.ThemeIcon
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.preferences.TrialPreferences
import app.simple.inure.util.AppUtils

class License : ScopedBottomSheetFragment() {

    private lateinit var status: TypeFaceTextView
    private lateinit var progress: ThemeIcon

    private var broadcastReceiver: BroadcastReceiver? = null
    private var intentFilter: IntentFilter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_license, container, false)

        status = view.findViewById(R.id.status)
        progress = view.findViewById(R.id.loader)

        intentFilter = IntentFilter(IntentConstants.ACTION_VERIFICATION_RESPONSE)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: android.content.Context?, intent: Intent?) {
                when (intent?.getIntExtra(IntentConstants.EXTRA_LICENSE, -1)) {
                    LicenseConstants.LICENSED -> {
                        status.setTextWithAnimation(getString(R.string.full_version_activated))
                        TrialPreferences.setFullVersion(true)
                    }
                    LicenseConstants.NOT_LICENSED, LicenseConstants.ERROR -> {
                        status.setTextWithAnimation(getString(R.string.failed_to_activate_full_version))
                        TrialPreferences.setFullVersion(false)
                    }
                    LicenseConstants.UNSPECIFIED -> {
                        status.text = getString(R.string.unspecified_failure)
                        TrialPreferences.setFullVersion(false)
                    }
                }
            }
        }

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(broadcastReceiver!!, intentFilter!!)
        sendBroadcast()
    }

    private fun sendBroadcast() {
        Intent().apply {
            action = IntentConstants.ACTION_VERIFICATION_REQUEST
            flags = Intent.FLAG_INCLUDE_STOPPED_PACKAGES
            component = ComponentName(AppUtils.UNLOCKER_PACKAGE_NAME, AppUtils.RECEIVER_PACKAGE_NAME)
        }.also {
            requireActivity().sendBroadcast(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(broadcastReceiver!!)
    }

    companion object {
        fun newInstance(): License {
            val args = Bundle()
            val fragment = License()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showLicense() {
            newInstance().show(this, TAG)
        }

        const val TAG = "License"
    }
}
