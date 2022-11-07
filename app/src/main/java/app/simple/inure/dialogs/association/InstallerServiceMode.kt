package app.simple.inure.dialogs.association

import android.app.Service
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.constants.ServiceConstants
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.AppIconImageView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.services.BatchExtractService
import app.simple.inure.services.InstallerService
import app.simple.inure.util.ParcelUtils.parcelable

class InstallerServiceMode : ScopedBottomSheetFragment() {

    private var installerService: InstallerService? = null
    private var serviceConnection: ServiceConnection? = null
    private var installerBroadcastReceiver: BroadcastReceiver? = null
    private var installerIntentFilter = IntentFilter()

    private lateinit var icon: AppIconImageView
    private lateinit var name: TypeFaceTextView
    private lateinit var packageName: TypeFaceTextView
    private lateinit var positiveButton: DynamicRippleTextView
    private lateinit var negativeButton: DynamicRippleTextView

    private var update = false
    private var installed = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_installer, container, false)

        icon = view.findViewById(R.id.icon)
        name = view.findViewById(R.id.name)
        packageName = view.findViewById(R.id.package_id)
        positiveButton = view.findViewById(R.id.install)
        negativeButton = view.findViewById(R.id.cancel)

        installerIntentFilter.addAction(ServiceConstants.actionPackageInfo)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        installerBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    ServiceConstants.actionPackageInfo -> {
                        packageInfo = intent.extras?.parcelable(BundleConstants.packageInfo)!!
                        icon.setImageDrawable(packageInfo.applicationInfo.loadIcon(requireContext().packageManager))
                        name.text = packageInfo.applicationInfo.loadLabel(requireContext().packageManager)
                        packageName.text = buildString {
                            append(packageInfo.packageName)
                            append("(${packageInfo.versionName})")
                        }
                    }
                }
            }
        }

        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                installerService = (service as InstallerService.InstallerServiceBinder).getService()
                installerService?.uri = requireArguments().parcelable(BundleConstants.uri)
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                /* no-op */
            }
        }

        positiveButton.setOnClickListener {
            if (update) {

            } else {

            }
        }

        negativeButton.setOnClickListener {
            if (installed) {

            } else {
                unbindService()
                stopService()
                dismiss()
                requireActivity().finish()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        requireActivity().bindService(Intent(requireContext(), InstallerService::class.java), serviceConnection!!, Service.BIND_AUTO_CREATE)
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(installerBroadcastReceiver!!, installerIntentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(installerBroadcastReceiver!!)
    }

    private fun unbindService() {
        kotlin.runCatching {
            requireActivity().unbindService(serviceConnection!!)
        }.getOrElse {
            it.printStackTrace()
        }
    }

    private fun stopService() {
        requireActivity().stopService(Intent(requireActivity(), BatchExtractService::class.java))
    }

    companion object {
        fun newInstance(uri: Uri): InstallerServiceMode {
            val args = Bundle()
            args.putParcelable(BundleConstants.uri, uri)
            val fragment = InstallerServiceMode()
            fragment.arguments = args
            return fragment
        }
    }
}