package app.simple.inure.dialogs.usagestats

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.constants.SortConstant
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.preferences.StatisticsPreferences
import app.simple.inure.util.SortUsageStats
import com.google.android.material.chip.ChipGroup

class UsageStatsSort : ScopedBottomSheetFragment() {

    private lateinit var sortChipGroup: ChipGroup
    private lateinit var sortStyleChipGroup: ChipGroup
    private lateinit var applicationTypeChipGroup: ChipGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_sort_usage_stats, container, false)

        sortChipGroup = view.findViewById(R.id.sort_chip_group)
        sortStyleChipGroup = view.findViewById(R.id.sorting_style_chip_group)
        applicationTypeChipGroup = view.findViewById(R.id.application_type_chip_group)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            sortChipGroup.removeView(view.findViewById(R.id.min_sdk))
        }

        setSort()
        setSortStyle()
        setApplicationType()

        sortChipGroup.setOnCheckedStateChangeListener { _, checkedId ->
            checkedId.forEach {
                when (it) {
                    R.id.name -> {
                        StatisticsPreferences.setSortType(SortUsageStats.NAME)
                    }
                    R.id.package_name -> {
                        StatisticsPreferences.setSortType(SortUsageStats.PACKAGE_NAME)
                    }
                    R.id.install_date -> {
                        StatisticsPreferences.setSortType(SortUsageStats.INSTALL_DATE)
                    }
                    R.id.update_date -> {
                        StatisticsPreferences.setSortType(SortUsageStats.UPDATE_DATE)
                    }
                    R.id.size -> {
                        StatisticsPreferences.setSortType(SortUsageStats.APP_SIZE)
                    }
                    R.id.target_sdk -> {
                        StatisticsPreferences.setSortType(SortUsageStats.TARGET_SDK)
                    }
                    R.id.min_sdk -> {
                        StatisticsPreferences.setSortType(SortUsageStats.MINIMUM_SDK)
                    }
                    R.id.time_used -> {
                        StatisticsPreferences.setSortType(SortUsageStats.TIME_USED)
                    }
                    R.id.data_sent -> {
                        StatisticsPreferences.setSortType(SortUsageStats.DATA_SENT)
                    }
                    R.id.data_received -> {
                        StatisticsPreferences.setSortType(SortUsageStats.DATA_RECEIVED)
                    }
                    R.id.wifi_sent -> {
                        StatisticsPreferences.setSortType(SortUsageStats.WIFI_SENT)
                    }
                    R.id.wifi_received -> {
                        StatisticsPreferences.setSortType(SortUsageStats.WIFI_RECEIVED)
                    }
                }
            }
        }

        sortStyleChipGroup.setOnCheckedStateChangeListener { _, checkedId ->
            checkedId.forEach {
                when (it) {
                    R.id.ascending -> {
                        StatisticsPreferences.setReverseSorting(false)
                    }
                    R.id.descending -> {
                        StatisticsPreferences.setReverseSorting(true)
                    }
                }
            }
        }

        applicationTypeChipGroup.setOnCheckedStateChangeListener { _, checkedId ->
            checkedId.forEach {
                when (it) {
                    R.id.both -> {
                        StatisticsPreferences.setAppsCategory(SortConstant.BOTH)
                    }
                    R.id.system -> {
                        StatisticsPreferences.setAppsCategory(SortConstant.SYSTEM)
                    }
                    R.id.user -> {
                        StatisticsPreferences.setAppsCategory(SortConstant.USER)
                    }
                }
            }
        }
    }

    private fun setSort() {
        when (StatisticsPreferences.getSortedBy()) {
            SortUsageStats.NAME -> {
                sortChipGroup.check(R.id.name)
            }
            SortUsageStats.PACKAGE_NAME -> {
                sortChipGroup.check(R.id.package_name)
            }
            SortUsageStats.INSTALL_DATE -> {
                sortChipGroup.check(R.id.install_date)
            }
            SortUsageStats.UPDATE_DATE -> {
                sortChipGroup.check(R.id.update_date)
            }
            SortUsageStats.APP_SIZE -> {
                sortChipGroup.check(R.id.size)
            }
            SortUsageStats.TARGET_SDK -> {
                sortChipGroup.check(R.id.target_sdk)
            }
            SortUsageStats.TIME_USED -> {
                sortChipGroup.check(R.id.time_used)
            }
            SortUsageStats.DATA_SENT -> {
                sortChipGroup.check(R.id.data_sent)
            }
            SortUsageStats.DATA_RECEIVED -> {
                sortChipGroup.check(R.id.data_received)
            }
            SortUsageStats.WIFI_SENT -> {
                sortChipGroup.check(R.id.wifi_sent)
            }
            SortUsageStats.WIFI_RECEIVED -> {
                sortChipGroup.check(R.id.wifi_received)
            }
        }
    }

    private fun setSortStyle() {
        if (StatisticsPreferences.isReverseSorting()) {
            sortStyleChipGroup.check(R.id.descending)
        } else {
            sortStyleChipGroup.check(R.id.ascending)
        }
    }

    private fun setApplicationType() {
        when (StatisticsPreferences.getAppsCategory()) {
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
    }

    companion object {
        fun newInstance(): UsageStatsSort {
            val args = Bundle()
            val fragment = UsageStatsSort()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showUsageStatsSort(): UsageStatsSort {
            val dialog = newInstance()
            dialog.show(this, "usage_stats_sort")
            return dialog
        }
    }
}