package app.simple.inure.ui.panels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.viewModels
import app.simple.inure.R
import app.simple.inure.adapters.ui.AdapterDeviceInfo
import app.simple.inure.constants.BottomMenuConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.ui.deviceinfo.BatteryInfo
import app.simple.inure.ui.deviceinfo.DeviceInfo
import app.simple.inure.ui.deviceinfo.SystemInfo
import app.simple.inure.viewmodels.deviceinfo.PanelItemsViewModel

/**
 * This feature has been removed from the app
 */
class DeviceInfo : ScopedFragment() {

    private lateinit var panels: CustomVerticalRecyclerView
    private lateinit var adapterDeviceInfo: AdapterDeviceInfo
    private val panelItemsViewModel: PanelItemsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_device_info, container, false)

        panels = view.findViewById(R.id.device_info_panel_rv)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        panelItemsViewModel.getPanelItems().observe(viewLifecycleOwner) {
            postponeEnterTransition()

            adapterDeviceInfo = AdapterDeviceInfo(it)

            adapterDeviceInfo.setOnDeviceInfoCallbackListener(object : AdapterDeviceInfo.Companion.AdapterDeviceInfoCallbacks {
                override fun onItemClicked(source: String, icon: View) {
                    when (source) {
                        getString(R.string.system) -> {
                            openFragmentLinear(SystemInfo.newInstance(), icon, "system_info")
                        }
                        getString(R.string.device) -> {
                            openFragmentLinear(DeviceInfo.newInstance(), icon, "device_info")
                        }
                        getString(R.string.battery) -> {
                            openFragmentLinear(BatteryInfo.newInstance(), icon, "battery_info")
                        }
                        getString(R.string.sensors) -> {
                            openFragmentLinear(Sensors.newInstance(), icon, "sensors")
                        }
                    }
                }
            })

            panels.adapter = adapterDeviceInfo

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }

            bottomRightCornerMenu?.initBottomMenuWithRecyclerView(BottomMenuConstants.getGenericBottomMenuItems(), panels) { id, _ ->
                when (id) {
                    R.drawable.ic_search -> {
                        openFragmentSlide(Search.newInstance(true), "search")
                    }
                    R.drawable.ic_settings -> {
                        openFragmentSlide(Preferences.newInstance(), "prefs_screen")
                    }
                }
            }
        }
    }

    companion object {
        fun newInstance(): app.simple.inure.ui.panels.DeviceInfo {
            val args = Bundle()
            val fragment = app.simple.inure.ui.panels.DeviceInfo()
            fragment.arguments = args
            return fragment
        }
    }
}
