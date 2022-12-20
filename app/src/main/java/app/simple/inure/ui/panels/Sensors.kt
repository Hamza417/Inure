package app.simple.inure.ui.panels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.viewModels
import app.simple.inure.R
import app.simple.inure.adapters.ui.AdapterSensors
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.popups.sensors.PopupSortingStyle
import app.simple.inure.viewmodels.viewers.SensorsViewModel

class Sensors : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var adapterSensors: AdapterSensors
    private val sensorsViewModel: SensorsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_sensors, container, false)

        recyclerView = view.findViewById(R.id.sensors_recycler_view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sensorsViewModel.getSensorsData().observe(viewLifecycleOwner) {
            adapterSensors = AdapterSensors(it)

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }

            recyclerView.adapter = adapterSensors

            bottomRightCornerMenu?.initBottomMenuWithRecyclerView(arrayListOf(R.drawable.ic_sort), recyclerView) { id, view ->
                when (id) {
                    R.drawable.ic_sort -> {
                        PopupSortingStyle(view)
                    }
                }
            }
        }
    }

    companion object {
        fun newInstance(): Sensors {
            val args = Bundle()
            val fragment = Sensors()
            fragment.arguments = args
            return fragment
        }
    }
}