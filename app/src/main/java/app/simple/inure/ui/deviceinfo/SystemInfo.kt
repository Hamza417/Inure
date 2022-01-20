package app.simple.inure.ui.deviceinfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.adapters.deviceinfo.AdapterDeviceInfoContent
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.viewmodels.deviceinfo.SystemInfoViewModel

class SystemInfo : ScopedFragment() {

    private lateinit var device: RecyclerView
    private lateinit var additional: RecyclerView

    private lateinit var adapterDeviceInfoContent: AdapterDeviceInfoContent
    private val systemInfoViewModel: SystemInfoViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.device_info_system, container, false)

        device = view.findViewById(R.id.system_info_rv)
        additional = view.findViewById(R.id.additional_info_rv)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        systemInfoViewModel.getInformation().observe(viewLifecycleOwner, {
            adapterDeviceInfoContent = AdapterDeviceInfoContent(it)
            device.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            device.adapter = adapterDeviceInfoContent
            device.scheduleLayoutAnimation()
        })

        systemInfoViewModel.getAdditionalInformation().observe(viewLifecycleOwner, {
            additional.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            additional.adapter = AdapterDeviceInfoContent(it)
            additional.scheduleLayoutAnimation()
        })
    }

    companion object {
        fun newInstance(): SystemInfo {
            val args = Bundle()
            val fragment = SystemInfo()
            fragment.arguments = args
            return fragment
        }
    }
}