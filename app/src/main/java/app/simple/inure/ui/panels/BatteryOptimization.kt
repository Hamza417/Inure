package app.simple.inure.ui.panels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.ui.AdapterBatteryOptimization
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.extensions.fragments.ScopedFragment
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

        batteryOptimizationViewModel.getBatteryOptimizationData().observe(viewLifecycleOwner) {
            adapterBatteryOptimization = AdapterBatteryOptimization(it)
            recyclerView.adapter = adapterBatteryOptimization

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }
        }
    }

    companion object {
        fun newInstance(): BatteryOptimization {
            return BatteryOptimization()
        }
    }
}