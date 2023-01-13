package app.simple.inure.models

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.InureRadioButton
import app.simple.inure.extensions.fragments.ScopedDialogFragment
import app.simple.inure.interfaces.dialog.BatteryOptimizationCallbacks
import app.simple.inure.util.ParcelUtils.parcelable

class BatteryOptimizationSwitch : ScopedDialogFragment() {

    private lateinit var packageId: TypeFaceTextView
    private lateinit var optimizeRadioButton: InureRadioButton
    private lateinit var dontOptimizeRadioButton: InureRadioButton
    private lateinit var done: DynamicRippleTextView
    private lateinit var cancel: DynamicRippleTextView

    private var batteryOptimizationModel: BatteryOptimizationModel? = null
    private var batteryOptimizationCallbacks: BatteryOptimizationCallbacks? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_battery_optimization_switch, container, false)

        packageId = view.findViewById(R.id.package_id)
        optimizeRadioButton = view.findViewById(R.id.optimize_rb)
        dontOptimizeRadioButton = view.findViewById(R.id.dont_optimize_rb)
        done = view.findViewById(R.id.done)
        cancel = view.findViewById(R.id.cancel)

        batteryOptimizationModel = requireArguments().parcelable(BundleConstants.batteryOptimizationModel)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        packageId.text = batteryOptimizationModel?.packageInfo?.applicationInfo?.name
        optimizeRadioButton.isChecked = batteryOptimizationModel?.isOptimized!!
        dontOptimizeRadioButton.isChecked = !batteryOptimizationModel?.isOptimized!!

        optimizeRadioButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                batteryOptimizationModel?.isOptimized = false // This should be inverted in [BatteryOptimizationViewModel.setBatteryOptimization]
                dontOptimizeRadioButton.isChecked = false
            }
        }

        dontOptimizeRadioButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                batteryOptimizationModel?.isOptimized = true // This should be inverted in [BatteryOptimizationViewModel.setBatteryOptimization]
                optimizeRadioButton.isChecked = false
            }
        }

        done.setOnClickListener {
            batteryOptimizationModel?.let { batteryOptimizationModel ->
                batteryOptimizationCallbacks?.onOptimizationSet(batteryOptimizationModel)
                dismiss()
            }
        }

        cancel.setOnClickListener {
            dismiss()
        }
    }

    fun setBatteryOptimizationCallbacks(batteryOptimizationCallbacks: BatteryOptimizationCallbacks) {
        this.batteryOptimizationCallbacks = batteryOptimizationCallbacks
    }

    companion object {
        fun newInstance(batteryOptimizationModel: BatteryOptimizationModel): BatteryOptimizationSwitch {
            val args = Bundle()
            args.putParcelable(BundleConstants.batteryOptimizationModel, batteryOptimizationModel)
            val fragment = BatteryOptimizationSwitch()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showBatteryOptimizationSwitch(batteryOptimizationModel: BatteryOptimizationModel): BatteryOptimizationSwitch {
            val fragment = newInstance(batteryOptimizationModel)
            fragment.show(this, "battery_optimization_switch")
            return fragment
        }
    }
}