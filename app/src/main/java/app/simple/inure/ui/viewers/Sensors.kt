package app.simple.inure.ui.viewers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import app.simple.inure.R
import app.simple.inure.adapters.details.AdapterSensors
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.viewmodels.viewers.SensorsViewModel

class Sensors : ScopedFragment() {

    private lateinit var back: DynamicRippleImageButton
    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var adapterSensors: AdapterSensors
    private val sensorsViewModel: SensorsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_sensors, container, false)

        back = view.findViewById(R.id.sensors_back_button)
        recyclerView = view.findViewById(R.id.sensors_recycler_view)

        startPostponedEnterTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sensorsViewModel.getSensorsData().observe(viewLifecycleOwner, {
            adapterSensors = AdapterSensors(it)
            recyclerView.adapter = adapterSensors
        })

        back.setOnClickListener {
            requireActivity().onBackPressed()
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