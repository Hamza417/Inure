package app.simple.inure.ui.deviceinfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import app.simple.inure.R
import app.simple.inure.adapters.deviceinfo.AdapterDeviceInfoContent
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.decorations.theme.ThemeIcon
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.util.BatteryUtils
import app.simple.inure.viewmodels.deviceinfo.BatteryViewModel

class BatteryInfo : ScopedFragment() {

    private var adapterBatteryInfo: AdapterDeviceInfoContent? = null
    private val batteryViewModel: BatteryViewModel by viewModels()

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var batteryIcon: ThemeIcon

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.device_info_battery, container, false)

        recyclerView = view.findViewById(R.id.device_info_rv)
        batteryIcon = view.findViewById(R.id.battery_icon)

        batteryIcon.setImageResource(BatteryUtils.getBatteryDrawable(requireContext()))

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        batteryViewModel.getBasics().observe(viewLifecycleOwner, {
            adapterBatteryInfo = AdapterDeviceInfoContent(it, getString(R.string.battery))
            setAdapters()
        })
    }

    private fun setAdapters() {
        adapterBatteryInfo ?: return

        recyclerView.adapter = ConcatAdapter(adapterBatteryInfo)
    }

    companion object {
        fun newInstance(): BatteryInfo {
            val args = Bundle()
            val fragment = BatteryInfo()
            fragment.arguments = args
            return fragment
        }
    }
}