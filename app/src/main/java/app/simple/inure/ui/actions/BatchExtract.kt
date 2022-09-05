package app.simple.inure.ui.actions

import android.content.*
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
import app.simple.inure.decorations.views.CustomProgressBar
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.models.BatchPackageInfo
import app.simple.inure.services.BatchExtractService
import app.simple.inure.util.IntentHelper
import app.simple.inure.util.NullSafety.isNotNull

class BatchExtract : ScopedBottomSheetFragment() {

    private var batchExtractService: BatchExtractService? = null
    private var serviceConnection: ServiceConnection? = null
    private var extractBroadcastReceiver: BroadcastReceiver? = null
    private var batchExtractIntentFilter = IntentFilter()
    private val stringBuilder = StringBuilder()
    private var appList = arrayListOf<BatchPackageInfo>()

    private var serviceBound = false

    private lateinit var progressStatus: TypeFaceTextView
    private lateinit var progressDetails: TypeFaceTextView
    private lateinit var progress: CustomProgressBar
    private lateinit var cancel: DynamicRippleTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_batch_extract, container, false)

        progressStatus = view.findViewById(R.id.progress_status)
        progressDetails = view.findViewById(R.id.progress_details)
        progress = view.findViewById(R.id.progress)
        cancel = view.findViewById(R.id.cancel)

        batchExtractIntentFilter.addAction(ServiceConstants.actionBatchCopyStart)
        batchExtractIntentFilter.addAction(ServiceConstants.actionBatchApkType)
        batchExtractIntentFilter.addAction(ServiceConstants.actionQuitExtractService)
        batchExtractIntentFilter.addAction(ServiceConstants.actionCopyProgress)
        batchExtractIntentFilter.addAction(ServiceConstants.actionCopyFinished)

        appList = requireArguments().getParcelableArrayList(BundleConstants.selectedBatchApps)!!

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        extractBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    ServiceConstants.actionBatchCopyStart -> {
                        packageInfo = appList[intent.extras?.getInt(IntentHelper.INT_EXTRA)!!].packageInfo
                        val fileName = "${packageInfo.applicationInfo.name}_(${packageInfo.versionName})"

                        if (packageInfo.applicationInfo.splitSourceDirs.isNotNull()) { // For split packages
                            if (progressStatus.text.isNotEmpty()) {
                                progressStatus.append("\n$fileName.apkm")
                            } else {
                                progressStatus.append("$fileName.apkm")
                            }
                        } else { // For APK files
                            if (progressStatus.text.isNotEmpty()) {
                                progressStatus.append("\n$fileName.apk")
                            } else {
                                progressStatus.append("$fileName.apk")
                            }
                        }
                    }
                    ServiceConstants.actionCopyProgressMax -> {
                        progress.max = intent.extras?.getInt(IntentHelper.INT_EXTRA)!!
                    }
                    ServiceConstants.actionCopyProgress -> {
                        progress.animateProgress(intent.extras?.getInt(IntentHelper.INT_EXTRA)!!)
                    }
                    ServiceConstants.actionBatchApkType -> {
                        progressDetails.text = when (intent.extras?.getInt(BatchExtractService.APK_TYPE_EXTRA)) {
                            BatchExtractService.APK_TYPE_FILE -> {
                                getString(R.string.preparing_apk_file)
                            }
                            BatchExtractService.APK_TYPE_SPLIT -> {
                                getString(R.string.creating_split_package)
                            }
                            else -> {
                                getString(R.string.unknown)
                            }
                        }
                    }
                    ServiceConstants.actionCopyFinished -> {
                        progressStatus.append("... Done")
                    }
                }
            }
        }

        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                kotlin.runCatching {
                    batchExtractService = (service as BatchExtractService.BatchCopyBinder).getService()
                    batchExtractService?.appsList = appList

                    serviceBound = true
                }.getOrElse {
                    it.printStackTrace()
                    showError(it.stackTraceToString())
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                serviceBound = false
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(requireActivity(), BatchExtractService::class.java)
        requireContext().startService(intent)
        serviceConnection?.let { requireContext().bindService(intent, it, Context.BIND_AUTO_CREATE) }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(extractBroadcastReceiver!!, batchExtractIntentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().unbindService(serviceConnection!!)
        requireActivity().stopService(Intent(requireActivity(), BatchExtractService::class.java))
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(extractBroadcastReceiver!!)
    }

    companion object {
        fun newInstance(arrayList: ArrayList<BatchPackageInfo>): BatchExtract {
            val args = Bundle()
            args.putParcelableArrayList(BundleConstants.selectedBatchApps, arrayList)
            val fragment = BatchExtract()
            fragment.arguments = args
            return fragment
        }
    }
}