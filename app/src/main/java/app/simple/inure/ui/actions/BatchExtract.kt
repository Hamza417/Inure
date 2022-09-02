package app.simple.inure.ui.actions

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.models.BatchPackageInfo
import app.simple.inure.services.BatchExtractService

class BatchExtract : ScopedFragment() {

    private var batchExtractService: BatchExtractService? = null
    private var serviceConnection: ServiceConnection? = null
    private var extractBroadcastReceiver: BroadcastReceiver? = null
    private var batchExtractIntentFilter = IntentFilter()

    private var serviceBound = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_batch_extract, container, false)


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                kotlin.runCatching {
                    batchExtractService = (service as BatchExtractService.BatchCopyBinder).getService()
                    batchExtractService?.appsList = requireArguments().getParcelableArrayList(BundleConstants.selectedBatchApps)!!
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
        // LocalBroadcastManager.getInstance(requireContext()).registerReceiver(extractBroadcastReceiver!!, batchExtractIntentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().unbindService(serviceConnection!!)
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
    }
}