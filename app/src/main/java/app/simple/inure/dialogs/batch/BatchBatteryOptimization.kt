package app.simple.inure.dialogs.batch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.InureRadioButton
import app.simple.inure.dialogs.app.Result.Companion.showResult
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.factories.batch.BatchAppsFactory
import app.simple.inure.models.BatchPackageInfo
import app.simple.inure.util.ParcelUtils.parcelableArrayList
import app.simple.inure.viewmodels.dialogs.BatchBatteryOptimizationViewModel
import app.simple.inure.viewmodels.panels.BatteryOptimizationViewModel

class BatchBatteryOptimization : ScopedBottomSheetFragment() {

    private lateinit var totalApps: TypeFaceTextView
    private lateinit var optimize: InureRadioButton
    private lateinit var dontOptimize: InureRadioButton
    private lateinit var set: DynamicRippleTextView
    private lateinit var cancel: DynamicRippleTextView

    private var batchBatteryOptimizationViewModel: BatchBatteryOptimizationViewModel? = null
    private var batchAppsFactory: BatchAppsFactory? = null
    private var batteryOptimizationViewModel: BatteryOptimizationViewModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_batch_battery_optimization, container, false)

        totalApps = view.findViewById(R.id.total_apps)
        optimize = view.findViewById(R.id.optimize_rb)
        dontOptimize = view.findViewById(R.id.dont_optimize_rb)
        set = view.findViewById(R.id.set)
        cancel = view.findViewById(R.id.cancel)

        batchAppsFactory = BatchAppsFactory(requireArguments().parcelableArrayList(BundleConstants.BATCH_BATTERY_OPTIMIZATION)!!)
        batchBatteryOptimizationViewModel = ViewModelProvider(this, batchAppsFactory!!)[BatchBatteryOptimizationViewModel::class.java]
        batteryOptimizationViewModel = ViewModelProvider(requireActivity())[BatteryOptimizationViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        totalApps.text = getString(
                R.string.total_apps,
                requireArguments().parcelableArrayList<BatchPackageInfo>
                (BundleConstants.BATCH_BATTERY_OPTIMIZATION)!!.size)

        optimize.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                dontOptimize.isChecked = false
            }
        }

        dontOptimize.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                optimize.isChecked = false
            }
        }

        set.setOnClickListener {
            batchBatteryOptimizationViewModel?.init(optimize.isChecked)
        }

        cancel.setOnClickListener {
            dismiss()
        }

        batchBatteryOptimizationViewModel?.getResult()?.observe(viewLifecycleOwner) {
            parentFragmentManager.showResult(it)
            batteryOptimizationViewModel?.refresh()
            dismiss()
        }

        batchBatteryOptimizationViewModel?.getWarning()?.observe(viewLifecycleOwner) {
            showWarning(it)
        }

        batchBatteryOptimizationViewModel?.getError()?.observe(viewLifecycleOwner) {
            showError(it)
        }
    }

    companion object {
        fun newInstance(apps: ArrayList<BatchPackageInfo>): BatchBatteryOptimization {
            val args = Bundle()
            args.putParcelableArrayList(BundleConstants.BATCH_BATTERY_OPTIMIZATION, apps)
            val fragment = BatchBatteryOptimization()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showBatchBatteryOptimization(apps: ArrayList<BatchPackageInfo>): BatchBatteryOptimization {
            val dialog = newInstance(apps)
            dialog.show(this, "batch_battery_optimization")
            return dialog
        }
    }
}
