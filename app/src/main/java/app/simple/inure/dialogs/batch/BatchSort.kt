package app.simple.inure.dialogs.batch

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.constants.SortConstant
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.preferences.BatchPreferences
import app.simple.inure.util.FlagUtils
import app.simple.inure.util.Sort
import com.google.android.material.chip.ChipGroup

class BatchSort : ScopedBottomSheetFragment() {

    private lateinit var sortChipGroup: ChipGroup
    private lateinit var sortStyleChipGroup: ChipGroup
    private lateinit var applicationTypeChipGroup: ChipGroup
    private lateinit var filterChipGroup: ChipGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_sort_batch, container, false)

        sortChipGroup = view.findViewById(R.id.sort_chip_group)
        sortStyleChipGroup = view.findViewById(R.id.sorting_style_chip_group)
        applicationTypeChipGroup = view.findViewById(R.id.application_type_chip_group)
        filterChipGroup = view.findViewById(R.id.filter_chip_group)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            sortChipGroup.removeView(view.findViewById(R.id.min_sdk))
        }

        setSortChipGroup()
        setSortStyleChipGroup()
        setSortFilterChipGroup()
        setApplicationTypeChipGroup()
    }

    private fun setApplicationTypeChipGroup() {
        when (BatchPreferences.getAppsCategory()) {
            SortConstant.SYSTEM -> {
                applicationTypeChipGroup.check(R.id.system)
            }
            SortConstant.USER -> {
                applicationTypeChipGroup.check(R.id.user)
            }
            SortConstant.BOTH -> {
                applicationTypeChipGroup.check(R.id.both)
            }
        }

        applicationTypeChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            checkedIds.forEach {
                when (it) {
                    R.id.system -> {
                        BatchPreferences.setAppsCategory(SortConstant.SYSTEM)
                    }
                    R.id.user -> {
                        BatchPreferences.setAppsCategory(SortConstant.USER)
                    }
                    R.id.both -> {
                        BatchPreferences.setAppsCategory(SortConstant.BOTH)
                    }
                }
            }
        }
    }

    private fun setSortFilterChipGroup() {
        if (FlagUtils.isFlagSet(BatchPreferences.getAppsFilter(), SortConstant.BATCH_SELECTED)) {
            filterChipGroup.check(R.id.selected)
        }

        if (FlagUtils.isFlagSet(BatchPreferences.getAppsFilter(), SortConstant.BATCH_NOT_SELECTED)) {
            filterChipGroup.check(R.id.not_selected)
        }

        if (FlagUtils.isFlagSet(BatchPreferences.getAppsFilter(), SortConstant.BATCH_DISABLED)) {
            filterChipGroup.check(R.id.disabled)
        }

        if (FlagUtils.isFlagSet(BatchPreferences.getAppsFilter(), SortConstant.BATCH_ENABLED)) {
            filterChipGroup.check(R.id.enabled)
        }

        if (FlagUtils.isFlagSet(BatchPreferences.getAppsFilter(), SortConstant.BATCH_UNINSTALLED)) {
            filterChipGroup.check(R.id.uninstalled)
        }

        if (FlagUtils.isFlagSet(BatchPreferences.getAppsFilter(), SortConstant.BATCH_FOSS)) {
            filterChipGroup.check(R.id.foss)
        }

        if (FlagUtils.isFlagSet(BatchPreferences.getAppsFilter(), SortConstant.BATCH_APK)) {
            filterChipGroup.check(R.id.apk)
        }

        if (FlagUtils.isFlagSet(BatchPreferences.getAppsFilter(), SortConstant.BATCH_SPLIT)) {
            filterChipGroup.check(R.id.split_apk)
        }

        filterChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            var flags = BatchPreferences.getAppsFilter()

            flags = if (checkedIds.contains(R.id.selected)) {
                FlagUtils.setFlag(flags, SortConstant.BATCH_SELECTED)
            } else {
                FlagUtils.unsetFlag(flags, SortConstant.BATCH_SELECTED)
            }

            flags = if (checkedIds.contains(R.id.enabled)) {
                FlagUtils.setFlag(flags, SortConstant.BATCH_ENABLED)
            } else {
                FlagUtils.unsetFlag(flags, SortConstant.BATCH_ENABLED)
            }

            flags = if (checkedIds.contains(R.id.disabled)) {
                FlagUtils.setFlag(flags, SortConstant.BATCH_DISABLED)
            } else {
                FlagUtils.unsetFlag(flags, SortConstant.BATCH_DISABLED)
            }

            flags = if (checkedIds.contains(R.id.not_selected)) {
                FlagUtils.setFlag(flags, SortConstant.BATCH_NOT_SELECTED)
            } else {
                FlagUtils.unsetFlag(flags, SortConstant.BATCH_NOT_SELECTED)
            }

            flags = if (checkedIds.contains(R.id.uninstalled)) {
                FlagUtils.setFlag(flags, SortConstant.BATCH_UNINSTALLED)
            } else {
                FlagUtils.unsetFlag(flags, SortConstant.BATCH_UNINSTALLED)
            }

            flags = if (checkedIds.contains(R.id.foss)) {
                FlagUtils.setFlag(flags, SortConstant.BATCH_FOSS)
            } else {
                FlagUtils.unsetFlag(flags, SortConstant.BATCH_FOSS)
            }

            flags = if (checkedIds.contains(R.id.apk)) {
                FlagUtils.setFlag(flags, SortConstant.BATCH_APK)
            } else {
                FlagUtils.unsetFlag(flags, SortConstant.BATCH_APK)
            }

            flags = if (checkedIds.contains(R.id.split_apk)) {
                FlagUtils.setFlag(flags, SortConstant.BATCH_SPLIT)
            } else {
                FlagUtils.unsetFlag(flags, SortConstant.BATCH_SPLIT)
            }

            BatchPreferences.setAppsFilter(flags)
        }
    }

    private fun setSortStyleChipGroup() {
        if (BatchPreferences.isReverseSorting()) {
            sortStyleChipGroup.check(R.id.descending)
        } else {
            sortStyleChipGroup.check(R.id.ascending)
        }

        sortStyleChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            checkedIds.forEach {
                when (it) {
                    R.id.ascending -> {
                        BatchPreferences.setReverseSorting(false)
                    }
                    R.id.descending -> {
                        BatchPreferences.setReverseSorting(true)
                    }
                }
            }
        }
    }

    private fun setSortChipGroup() {
        when (BatchPreferences.getSortStyle()) {
            Sort.NAME -> {
                sortChipGroup.check(R.id.name)
            }
            Sort.SIZE -> {
                sortChipGroup.check(R.id.size)
            }
            Sort.INSTALL_DATE -> {
                sortChipGroup.check(R.id.install_date)
            }
            Sort.UPDATE_DATE -> {
                sortChipGroup.check(R.id.update_date)
            }
            Sort.TARGET_SDK -> {
                sortChipGroup.check(R.id.target_sdk)
            }
            Sort.PACKAGE_NAME -> {
                sortChipGroup.check(R.id.package_name)
            }
            Sort.MIN_SDK -> {
                sortChipGroup.check(R.id.min_sdk)
            }
        }

        sortChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            checkedIds.forEach {
                when (it) {
                    R.id.name -> {
                        BatchPreferences.setSortStyle(Sort.NAME)
                    }
                    R.id.size -> {
                        BatchPreferences.setSortStyle(Sort.SIZE)
                    }
                    R.id.install_date -> {
                        BatchPreferences.setSortStyle(Sort.INSTALL_DATE)
                    }
                    R.id.update_date -> {
                        BatchPreferences.setSortStyle(Sort.UPDATE_DATE)
                    }
                    R.id.target_sdk -> {
                        BatchPreferences.setSortStyle(Sort.TARGET_SDK)
                    }
                    R.id.package_name -> {
                        BatchPreferences.setSortStyle(Sort.PACKAGE_NAME)
                    }
                    R.id.min_sdk -> {
                        BatchPreferences.setSortStyle(Sort.MIN_SDK)
                    }
                }
            }
        }
    }

    companion object {
        fun newInstance(): BatchSort {
            val args = Bundle()
            val fragment = BatchSort()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showBatchSort() {
            newInstance().show(this, "batch_sort")
        }
    }
}