package app.simple.inure.dialogs.batch

import android.Manifest
import android.content.*
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.constants.Misc
import app.simple.inure.constants.ServiceConstants
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.CustomProgressBar
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.math.Extensions.percentOf
import app.simple.inure.models.BatchPackageInfo
import app.simple.inure.services.BatchExtractService
import app.simple.inure.util.IntentHelper
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.util.ParcelUtils.parcelableArrayList
import kotlinx.coroutines.launch

class BatchExtract : ScopedBottomSheetFragment() {

    private var batchExtractService: BatchExtractService? = null
    private var serviceConnection: ServiceConnection? = null
    private var extractBroadcastReceiver: BroadcastReceiver? = null
    private var batchExtractIntentFilter = IntentFilter()
    private var appList = arrayListOf<BatchPackageInfo>()
    private var vibrator: Vibrator? = null
    private var vibratorManager: VibratorManager? = null

    private var serviceBound = false
    private var disableRepeat = -1
    private var maxLength = 0L
    private val vibratePattern = longArrayOf(500, 500)

    private lateinit var count: TypeFaceTextView
    private lateinit var name: TypeFaceTextView
    private lateinit var progressStatus: TypeFaceTextView
    private lateinit var progress: CustomProgressBar
    private lateinit var percentage: TypeFaceTextView
    private lateinit var cancel: DynamicRippleTextView

    private val requestPermissionLauncher = registerForActivityResult<String, Boolean>(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted.
            try {
                if (batchExtractService != null) {
                    batchExtractService?.reshowNotification()
                    Log.d(TAG, "Notification is reshowed")
                }
            } catch (e: IllegalStateException) {
                Log.e(TAG, "IllegalStateException: failed to reshow notification")
            }
        } else {
            Log.d(TAG, "Permission is denied.")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_batch_extract, container, false)

        count = view.findViewById(R.id.progress_count)
        name = view.findViewById(R.id.name)
        progressStatus = view.findViewById(R.id.progress_status)
        progress = view.findViewById(R.id.progress)
        percentage = view.findViewById(R.id.progress_percentage)
        cancel = view.findViewById(R.id.cancel)

        batchExtractIntentFilter.addAction(ServiceConstants.actionBatchCopyStart)
        batchExtractIntentFilter.addAction(ServiceConstants.actionBatchApkType)
        batchExtractIntentFilter.addAction(ServiceConstants.actionQuitExtractService)
        batchExtractIntentFilter.addAction(ServiceConstants.actionCopyProgress)
        batchExtractIntentFilter.addAction(ServiceConstants.actionCopyFinished)
        batchExtractIntentFilter.addAction(ServiceConstants.actionExtractDone)
        batchExtractIntentFilter.addAction(ServiceConstants.actionCopyProgressMax)

        appList = requireArguments().parcelableArrayList(BundleConstants.selectedBatchApps)!!

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            vibratorManager = requireActivity().getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibrator = vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            vibrator = requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Request notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!NotificationManagerCompat.from(requireContext()).areNotificationsEnabled()) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        extractBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    ServiceConstants.actionBatchCopyStart -> {
                        count.text = buildString {
                            append(intent.extras?.getInt(IntentHelper.INT_EXTRA)!!)
                            append("/")
                            append(appList.size)
                        }

                        packageInfo = appList[intent.extras?.getInt(IntentHelper.INT_EXTRA)!!].packageInfo
                        val fileName = "${packageInfo.applicationInfo.name}_(${packageInfo.versionName})"

                        if (packageInfo.applicationInfo.splitSourceDirs.isNotNull()) { // For split packages
                            name.text = buildString {
                                append(fileName)
                                append(Misc.splitApkFormat)
                            }
                        } else { // For APK files
                            name.text = buildString {
                                append(fileName)
                                append(Misc.apkFormat)
                            }
                        }
                    }
                    ServiceConstants.actionCopyProgress -> {
                        val percent = intent.extras?.getLong(IntentHelper.INT_EXTRA)!!.percentOf(maxLength)
                        progress.animateProgress(percent.toInt())
                        percentage.text = getString(R.string.progress, percent.toInt())
                    }
                    ServiceConstants.actionBatchApkType -> {
                        progressStatus.text = when (intent.extras?.getInt(BatchExtractService.APK_TYPE_EXTRA)) {
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
                        Log.d(TAG, "Copy finished")
                    }
                    ServiceConstants.actionExtractDone -> {
                        progressStatus.setText(R.string.done)
                        progress.animateProgress(progress.max)

                        viewLifecycleOwner.lifecycleScope.launch {
                            cancel.callOnClick()
                            if (Build.VERSION.SDK_INT >= 26) {
                                vibrator?.vibrate(VibrationEffect.createWaveform(vibratePattern, disableRepeat))
                            } else {
                                @Suppress("DEPRECATION")
                                vibrator?.vibrate(vibratePattern, disableRepeat)
                            }
                        }

                        unbindService()
                    }
                    ServiceConstants.actionCopyProgressMax -> {
                        maxLength = intent.extras?.getLong(IntentHelper.INT_EXTRA)!!
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
                stopService()
                dismiss()
            }
        }

        cancel.setOnClickListener {
            batchExtractService?.interruptCopying()
            dismiss()
        }

        bindService()
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(extractBroadcastReceiver!!, batchExtractIntentFilter)
    }

    override fun onDestroy() {
        unbindService()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(extractBroadcastReceiver!!)
        super.onDestroy()
    }

    private fun bindService() {
        kotlin.runCatching {
            if (!serviceBound) {
                val intent = Intent(requireActivity(), BatchExtractService::class.java)
                serviceConnection?.let { requireContext().bindService(intent, it, Context.BIND_AUTO_CREATE) }
            }
        }.getOrElse {
            it.printStackTrace()
        }
    }

    private fun unbindService() {
        kotlin.runCatching {
            if (serviceBound) {
                requireContext().unbindService(serviceConnection!!)
            } else {
                stopService()
                dismiss()
            }
        }.getOrElse {
            it.printStackTrace()
        }
    }

    private fun stopService() {
        requireActivity().stopService(Intent(requireActivity(), BatchExtractService::class.java))
    }

    companion object {
        fun newInstance(arrayList: ArrayList<BatchPackageInfo>): BatchExtract {
            val args = Bundle()
            args.putParcelableArrayList(BundleConstants.selectedBatchApps, arrayList)
            val fragment = BatchExtract()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showBatchExtract(arrayList: ArrayList<BatchPackageInfo>): BatchExtract {
            val fragment = newInstance(arrayList)
            fragment.show(this, "BatchExtract")
            return fragment
        }

        const val TAG = "BatchExtract"
    }
}