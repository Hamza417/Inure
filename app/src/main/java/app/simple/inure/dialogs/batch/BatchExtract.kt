package app.simple.inure.dialogs.batch

import android.Manifest
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.constants.Misc
import app.simple.inure.constants.ServiceConstants
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.math.Extensions.percentOf
import app.simple.inure.models.BatchPackageInfo
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.services.BatchExtractService
import app.simple.inure.themes.manager.ThemeManager
import app.simple.inure.util.AdapterUtils
import app.simple.inure.util.FileSizeHelper.toSize
import app.simple.inure.util.IntentHelper
import app.simple.inure.util.NullSafety.isNotNull
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.launch

class BatchExtract : ScopedBottomSheetFragment() {

    private var batchExtractService: BatchExtractService? = null
    private var serviceConnection: ServiceConnection? = null
    private var extractBroadcastReceiver: BroadcastReceiver? = null
    private var batchExtractIntentFilter = IntentFilter()
    private var appList: ArrayList<BatchPackageInfo>? = null
    private var vibrator: Vibrator? = null
    private var vibratorManager: VibratorManager? = null

    private var serviceBound = false
    private var disableRepeat = -1
    private var maxLength = 0L
    private val vibratePattern = longArrayOf(500, 500)

    private lateinit var hide: DynamicRippleImageButton
    private lateinit var count: TypeFaceTextView
    private lateinit var name: TypeFaceTextView
    private lateinit var progressStatus: TypeFaceTextView
    private lateinit var size: TypeFaceTextView
    private lateinit var progress: LinearProgressIndicator
    private lateinit var percentage: TypeFaceTextView
    private lateinit var cancel: DynamicRippleTextView

    private val requestPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
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

        hide = view.findViewById(R.id.hide)
        count = view.findViewById(R.id.progress_count)
        name = view.findViewById(R.id.name)
        progressStatus = view.findViewById(R.id.progress_status)
        size = view.findViewById(R.id.progress_size)
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            vibratorManager = requireActivity().getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibrator = vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            vibrator = requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
        }

        progress.setIndicatorColor(AppearancePreferences.getAccentColor())
        progress.trackColor = ThemeManager.theme.viewGroupTheme.viewerBackground
        progress.trackCornerRadius = AppearancePreferences.getCornerRadius().toInt()

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
                        try {
                            val position = batchExtractService?.position ?: 0

                            count.text = buildString {
                                append(position.plus(1))
                                append("/")
                                append(appList?.size)
                            }

                            packageInfo = appList!![position].packageInfo
                            val fileName = "${packageInfo.safeApplicationInfo.name}_(${packageInfo.versionName})"

                            if (packageInfo.safeApplicationInfo.splitSourceDirs.isNotNull()) { // For split packages
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

                            progressStatus.text = buildString {
                                if (packageInfo.safeApplicationInfo.splitSourceDirs.isNotNull()) { // For split packages
                                    append(getString(R.string.creating_split_package))
                                } else { // For APK files
                                    append(getString(R.string.preparing_apk_file))
                                }
                            }

                            /**
                             * Highlight the extension
                             */
                            AdapterUtils.searchHighlighter(name, name.text.substring(name.text.lastIndexOf(".")))
                        } catch (e: NullPointerException) {
                            showWarning("ERR: unexpected empty state of service")
                        } catch (e: IndexOutOfBoundsException) {
                            showWarning("ERR: unexpected value of position ${batchExtractService?.position}")
                        }
                    }
                    ServiceConstants.actionCopyProgress -> {
                        val copyProgress = intent.extras?.getLong(IntentHelper.INT_EXTRA)!!
                        val percent = copyProgress.percentOf(maxLength)
                        progress.setProgressCompat(percent.toInt(), true)
                        percentage.text = getString(R.string.progress, percent.toInt())

                        size.text = buildString {
                            append(" | ")
                            append("~")
                            append(maxLength.toSize())
                            append("/")
                            append(copyProgress.toSize())
                        }
                    }
                    ServiceConstants.actionBatchApkType -> {
                        /* no-op */
                    }
                    ServiceConstants.actionCopyFinished -> {
                        /* no-op */
                    }
                    ServiceConstants.actionExtractDone -> {
                        progressStatus.setText(R.string.done)
                        progress.setProgressCompat(100, true)

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
                    batchExtractService = (service as BatchExtractService.BatchExtractServiceBinder).getService()

                    appList?.clear()
                    appList = batchExtractService?.getAppList()
                    maxLength = batchExtractService?.maxSize!!

                    count.text = buildString {
                        append(batchExtractService?.position!!)
                        append("/")
                        append(appList?.size)
                    }

                    packageInfo = appList!![batchExtractService?.position!!].packageInfo
                    val fileName = "${packageInfo.safeApplicationInfo.name}_(${packageInfo.versionName})"

                    if (packageInfo.safeApplicationInfo.splitSourceDirs.isNotNull()) { // For split packages
                        this@BatchExtract.name.text = buildString {
                            append(fileName)
                            append(Misc.splitApkFormat)
                        }
                    } else { // For APK files
                        this@BatchExtract.name.text = buildString {
                            append(fileName)
                            append(Misc.apkFormat)
                        }
                    }

                    /**
                     * Highlight the extension
                     */
                    AdapterUtils.searchHighlighter(
                            this@BatchExtract.name,
                            this@BatchExtract.name.text.substring(
                                    this@BatchExtract.name.text.lastIndexOf(".")))

                    progressStatus.text = when (batchExtractService?.apkType) {
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

                    LocalBroadcastManager.getInstance(requireContext()).registerReceiver(extractBroadcastReceiver!!, batchExtractIntentFilter)
                    batchExtractService?.startCopying()
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

        cancel.setOnClickListener {
            batchExtractService?.interruptCopying()
            unbindService()
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(extractBroadcastReceiver!!)
            dismiss()
        }

        hide.setOnClickListener {
            dismiss()
        }

        bindService()
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(extractBroadcastReceiver!!)
        unbindService()
        super.onDestroy()
    }

    private fun bindService() {
        kotlin.runCatching {
            if (!serviceBound) {
                val intent = Intent(requireContext(), BatchExtractService::class.java)
                requireContext().startService(intent)
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
        requireContext().stopService(Intent(requireActivity(), BatchExtractService::class.java))
    }

    companion object {
        fun newInstance(): BatchExtract {
            val args = Bundle()
            val fragment = BatchExtract()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showBatchExtract(): BatchExtract {
            val fragment = newInstance()
            fragment.show(this, "BatchExtract")
            return fragment
        }

        const val TAG = "BatchExtract"
    }
}
