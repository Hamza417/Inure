package app.simple.inure.ui.panels

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.ui.AdapterBatteryOptimization
import app.simple.inure.constants.BottomMenuConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.interfaces.adapters.AdapterCallbacks
import app.simple.inure.models.BatteryOptimizationModel
import app.simple.inure.popups.battery.PopupBatteryOptimizationCategory
import app.simple.inure.popups.battery.PopupBatteryOptimizationSortingStyle
import app.simple.inure.popups.battery.PopupOptimizationSwitch
import app.simple.inure.preferences.BatteryOptimizationPreferences
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.viewmodels.panels.BatteryOptimizationViewModel

class BatteryOptimization : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var adapterBatteryOptimization: AdapterBatteryOptimization

    private lateinit var batteryOptimizationViewModel: BatteryOptimizationViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_battery_optimization, container, false)

        recyclerView = view.findViewById(R.id.battery_optimization_recycler_view)

        batteryOptimizationViewModel = ViewModelProvider(requireActivity())[BatteryOptimizationViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        fullVersionCheck()

        batteryOptimizationViewModel.getBatteryOptimizationData().observe(viewLifecycleOwner) { batteryOptimizationModelArrayList ->
            adapterBatteryOptimization = AdapterBatteryOptimization(batteryOptimizationModelArrayList)

            adapterBatteryOptimization.setOnItemClickListener(object : AdapterCallbacks {
                override fun onFilterPressed(view: View) {
                    PopupBatteryOptimizationCategory(view)
                }

                override fun onSortPressed(view: View) {
                    PopupBatteryOptimizationSortingStyle(view)
                }

                override fun onSearchPressed(view: View) {
                    openFragmentSlide(Search.newInstance(true), "search")
                }

                override fun onSettingsPressed(view: View) {
                    openFragmentSlide(Preferences.newInstance(), "preferences")
                }

                override fun onBatteryOptimizationClicked(view: View, batteryOptimizationModel: BatteryOptimizationModel, position: Int) {
                    PopupOptimizationSwitch(view, batteryOptimizationModel).setOnOptimizeClicked {
                        batteryOptimizationViewModel.getBatteryOptimizationUpdate().observe(viewLifecycleOwner) {
                            if (it.isNotNull()) {
                                adapterBatteryOptimization.updateItem(it.first, it.second)
                                batteryOptimizationViewModel.clearBatteryOptimizationAppData()
                            }
                        }

                        batteryOptimizationViewModel.setBatteryOptimization(batteryOptimizationModel, position)
                    }
                }
            })

            recyclerView.adapter = adapterBatteryOptimization

            bottomRightCornerMenu?.initBottomMenuWithRecyclerView(BottomMenuConstants.getAllAppsBottomMenuItems(), recyclerView) { id, view ->
                when (id) {
                    R.drawable.ic_filter -> {
                        PopupBatteryOptimizationCategory(view)
                    }
                    R.drawable.ic_sort -> {
                        PopupBatteryOptimizationSortingStyle(view)
                    }
                    R.drawable.ic_search -> {
                        openFragmentSlide(Search.newInstance(true), "search")
                    }
                    R.drawable.ic_settings -> {
                        openFragmentSlide(Preferences.newInstance(), "preferences")
                    }
                }
            }

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            BatteryOptimizationPreferences.batteryOptimizationSortStyle,
            BatteryOptimizationPreferences.batteryOptimizationIsSortingReversed,
            BatteryOptimizationPreferences.batteryOptimizationCategory -> {
                batteryOptimizationViewModel.refresh()
            }
        }
    }

    companion object {
        fun newInstance(): BatteryOptimization {
            return BatteryOptimization()
        }
    }
}