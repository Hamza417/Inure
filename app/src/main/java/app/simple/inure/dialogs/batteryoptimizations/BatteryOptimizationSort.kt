package app.simple.inure.dialogs.batteryoptimizations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.constants.SortConstant
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.preferences.BatteryOptimizationPreferences
import app.simple.inure.util.FlagUtils
import app.simple.inure.util.Sort
import com.google.android.material.chip.ChipGroup

class BatteryOptimizationSort : ScopedBottomSheetFragment() {

    private lateinit var sortChipGroup: ChipGroup
    private lateinit var sortStyleChipGroup: ChipGroup
    private lateinit var applicationTypeChipGroup: ChipGroup
    private lateinit var filterChipGroup: ChipGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_sort_battery_optimization, container, false)

        sortChipGroup = view.findViewById(R.id.sort_chip_group)
        sortStyleChipGroup = view.findViewById(R.id.sorting_style_chip_group)
        applicationTypeChipGroup = view.findViewById(R.id.application_type_chip_group)
        filterChipGroup = view.findViewById(R.id.filter_chip_group)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setSortChipGroup()
        setSortStyleChipGroup()
        setApplicationTypeChipGroup()
        setFilterChipGroup()
    }

    private fun setFilterChipGroup() {
        if (FlagUtils.isFlagSet(BatteryOptimizationPreferences.getFilter(), SortConstant.OPTIMIZED)) {
            filterChipGroup.check(R.id.optimized)
        }

        if (FlagUtils.isFlagSet(BatteryOptimizationPreferences.getFilter(), SortConstant.NOT_OPTIMIZED)) {
            filterChipGroup.check(R.id.not_optimized)
        }

        filterChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            var flag = BatteryOptimizationPreferences.getFilter()

            flag = if (checkedIds.contains(R.id.optimized)) {
                FlagUtils.setFlag(flag, SortConstant.OPTIMIZED)
            } else {
                FlagUtils.unsetFlag(flag, SortConstant.OPTIMIZED)
            }

            flag = if (checkedIds.contains(R.id.not_optimized)) {
                FlagUtils.setFlag(flag, SortConstant.NOT_OPTIMIZED)
            } else {
                FlagUtils.unsetFlag(flag, SortConstant.NOT_OPTIMIZED)
            }

            BatteryOptimizationPreferences.setFilter(flag)
        }
    }

    private fun setApplicationTypeChipGroup() {
        when (BatteryOptimizationPreferences.getApplicationType()) {
            SortConstant.BOTH -> {
                applicationTypeChipGroup.check(R.id.both)
            }
            SortConstant.SYSTEM -> {
                applicationTypeChipGroup.check(R.id.system)
            }
            SortConstant.USER -> {
                applicationTypeChipGroup.check(R.id.user)
            }
        }

        applicationTypeChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            for (id in checkedIds) {
                when (id) {
                    R.id.both -> {
                        BatteryOptimizationPreferences.setApplicationType(SortConstant.BOTH)
                    }
                    R.id.system -> {
                        BatteryOptimizationPreferences.setApplicationType(SortConstant.SYSTEM)
                    }
                    R.id.user -> {
                        BatteryOptimizationPreferences.setApplicationType(SortConstant.USER)
                    }
                }
            }
        }
    }

    private fun setSortStyleChipGroup() {
        when (BatteryOptimizationPreferences.isSortingReversed()) {
            true -> {
                sortStyleChipGroup.check(R.id.descending)
            }
            false -> {
                sortStyleChipGroup.check(R.id.ascending)
            }
        }

        sortStyleChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            for (id in checkedIds) {
                when (id) {
                    R.id.ascending -> {
                        BatteryOptimizationPreferences.setSortingReversed(false)
                    }
                    R.id.descending -> {
                        BatteryOptimizationPreferences.setSortingReversed(true)
                    }
                }
            }
        }
    }

    private fun setSortChipGroup() {
        when (BatteryOptimizationPreferences.getSortStyle()) {
            Sort.NAME -> {
                sortChipGroup.check(R.id.name)
            }
            Sort.PACKAGE_NAME -> {
                sortChipGroup.check(R.id.package_name)
            }
            Sort.INSTALL_DATE -> {
                sortChipGroup.check(R.id.install_date)
            }
            Sort.UPDATE_DATE -> {
                sortChipGroup.check(R.id.update_date)
            }
            Sort.SIZE -> {
                sortChipGroup.check(R.id.size)
            }
            Sort.TARGET_SDK -> {
                sortChipGroup.check(R.id.target_sdk)
            }
        }

        sortChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            for (id in checkedIds) {
                when (id) {
                    R.id.name -> {
                        BatteryOptimizationPreferences.setSortStyle(Sort.NAME)
                    }
                    R.id.package_name -> {
                        BatteryOptimizationPreferences.setSortStyle(Sort.PACKAGE_NAME)
                    }
                    R.id.install_date -> {
                        BatteryOptimizationPreferences.setSortStyle(Sort.INSTALL_DATE)
                    }
                    R.id.update_date -> {
                        BatteryOptimizationPreferences.setSortStyle(Sort.UPDATE_DATE)
                    }
                    R.id.size -> {
                        BatteryOptimizationPreferences.setSortStyle(Sort.SIZE)
                    }
                    R.id.target_sdk -> {
                        BatteryOptimizationPreferences.setSortStyle(Sort.TARGET_SDK)
                    }
                }
            }
        }
    }

    companion object {
        fun newInstance(): BatteryOptimizationSort {
            val args = Bundle()
            val fragment = BatteryOptimizationSort()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showBatteryOptimizationSort() {
            val dialog = newInstance()
            dialog.show(this, "battery_optimization_sort")
        }
    }
}