package app.simple.inure.ui.actions

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.simple.inure.R
import app.simple.inure.adapters.batch.AdapterBatchExtract
import app.simple.inure.constants.BundleConstants
import app.simple.inure.constants.ServiceConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.models.BatchPackageInfo
import app.simple.inure.services.BatchExtractService
import app.simple.inure.util.IntentHelper
import java.util.stream.Collectors.toCollection

class BatchExtract : ScopedFragment() {

    private var batchExtractService: BatchExtractService? = null
    private var serviceConnection: ServiceConnection? = null
    private var extractBroadcastReceiver: BroadcastReceiver? = null
    private var batchExtractIntentFilter = IntentFilter()
    private var adapterBatchExtract: AdapterBatchExtract? = null

    private var serviceBound = false

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private var holder: AdapterBatchExtract.Holder? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_batch_extract, container, false)

        recyclerView = view.findViewById(R.id.batch_process_recycler_view)

        batchExtractIntentFilter.addAction(ServiceConstants.actionBatchCopyStart)
        batchExtractIntentFilter.addAction(ServiceConstants.actionBatchApkType)
        batchExtractIntentFilter.addAction(ServiceConstants.actionQuitExtractService)
        batchExtractIntentFilter.addAction(ServiceConstants.actionCopyProgress)
        batchExtractIntentFilter.addAction(ServiceConstants.actionCopyFinished)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        extractBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    ServiceConstants.actionBatchCopyStart -> {
                        holder = recyclerView.findViewHolderForAdapterPosition(1) as AdapterBatchExtract.Holder
                    }
                    ServiceConstants.actionCopyProgress -> {
                        holder?.progress?.progress = intent.extras?.getInt(IntentHelper.INT_EXTRA) ?: 0
                    }
                    ServiceConstants.actionBatchApkType -> {
                        holder?.status?.text = when (intent.extras?.getInt(BatchExtractService.APK_TYPE_EXTRA)) {
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
                        adapterBatchExtract?.removeTopItem()
                        holder = null
                    }
                }
            }
        }

        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                kotlin.runCatching {
                    requireArguments().getParcelableArrayList<BatchPackageInfo>(BundleConstants.selectedBatchApps)!!.stream()
                    adapterBatchExtract = AdapterBatchExtract(requireArguments().getParcelableArrayList(BundleConstants.selectedBatchApps)!!)
                    batchExtractService = (service as BatchExtractService.BatchCopyBinder).getService()
                    batchExtractService?.appsList = requireArguments()
                        .getParcelableArrayList<BatchPackageInfo>(BundleConstants.selectedBatchApps)!!
                        .stream()
                        .map {
                            BatchPackageInfo(it.packageInfo, it.isSelected, it.dateSelected)
                        }
                        .collect(toCollection { ArrayList() })

                    serviceBound = true

                    recyclerView.adapter = adapterBatchExtract
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