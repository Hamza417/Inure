package app.simple.inure.ui.deviceinfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import app.simple.inure.R
import app.simple.inure.adapters.deviceinfo.AdapterDeviceInfoContent
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.viewmodels.deviceinfo.SystemInfoViewModel

class SystemInfo : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView

    private var adapterOSInfoContent: AdapterDeviceInfoContent? = null
    private var adapterAdditionalInfoContent: AdapterDeviceInfoContent? = null

    private val systemInfoViewModel: SystemInfoViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.device_info_system, container, false)

        recyclerView = view.findViewById(R.id.system_info_rv)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        systemInfoViewModel.getInformation().observe(viewLifecycleOwner) {
            adapterOSInfoContent = AdapterDeviceInfoContent(it, getString(R.string.os))
            setAdapters()
        }

        systemInfoViewModel.getAdditionalInformation().observe(viewLifecycleOwner) {
            adapterAdditionalInfoContent = AdapterDeviceInfoContent(it, getString(R.string.additional_information))
            setAdapters()
        }
    }

    private fun setAdapters() {
        adapterOSInfoContent ?: return
        adapterAdditionalInfoContent ?: return

        val adapter = ConcatAdapter(adapterOSInfoContent, adapterAdditionalInfoContent)
        recyclerView.adapter = adapter
        recyclerView.scheduleLayoutAnimation()
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