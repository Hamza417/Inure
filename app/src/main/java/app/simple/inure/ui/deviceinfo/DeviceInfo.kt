package app.simple.inure.ui.deviceinfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ConcatAdapter
import app.simple.inure.R
import app.simple.inure.adapters.deviceinfo.AdapterDeviceInfoContent
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.factories.deviceinfo.DeviceInfoFactory
import app.simple.inure.viewmodels.deviceinfo.DeviceInfoViewModel

class DeviceInfo : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView

    private var adapterBasicInfo: AdapterDeviceInfoContent? = null
    private var adapterDisplayInfo: AdapterDeviceInfoContent? = null
    private lateinit var data: DeviceInfoViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.device_info_device, container, false)

        recyclerView = view.findViewById(R.id.device_info_rv)

        val deviceInfoFactory = DeviceInfoFactory(requireApplication(), requireActivity().window)
        data = ViewModelProvider(this, deviceInfoFactory)[DeviceInfoViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        data.getBasics().observe(viewLifecycleOwner, {
            adapterBasicInfo = AdapterDeviceInfoContent(it, getString(R.string.device))
            setAdapters()
        })

        data.getDisplay().observe(viewLifecycleOwner, {
            adapterDisplayInfo = AdapterDeviceInfoContent(it, getString(R.string.display))
            setAdapters()
        })
    }

    private fun setAdapters() {
        adapterBasicInfo ?: return
        adapterDisplayInfo ?: return

        recyclerView.adapter = ConcatAdapter(adapterBasicInfo, adapterDisplayInfo)
    }

    companion object {
        fun newInstance(): DeviceInfo {
            val args = Bundle()
            val fragment = DeviceInfo()
            fragment.arguments = args
            return fragment
        }
    }
}