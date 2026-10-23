package app.simple.inure.dialogs.apps

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.constants.SortConstant
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.preferences.UntaggedPreferences
import app.simple.inure.util.Sort
import com.google.android.material.chip.ChipGroup

class UntaggedSort : ScopedBottomSheetFragment() {

    private lateinit var sortChipGroup: ChipGroup
    private lateinit var sortStyleChipGroup: ChipGroup
    private lateinit var applicationTypeChipGroup: ChipGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_sort_untagged_apps, container, false)

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

        when (UntaggedPreferences.getSortStyle()) {
            Sort.NAME -> sortChipGroup.check(R.id.name)
            Sort.PACKAGE_NAME -> sortChipGroup.check(R.id.package_name)
            Sort.INSTALL_DATE -> sortChipGroup.check(R.id.install_date)
            Sort.UPDATE_DATE -> sortChipGroup.check(R.id.update_date)
            Sort.SIZE -> sortChipGroup.check(R.id.size)
            Sort.TARGET_SDK -> sortChipGroup.check(R.id.target_sdk)
            Sort.MIN_SDK -> kotlin.runCatching { sortChipGroup.check(R.id.min_sdk) }
        }

        if (UntaggedPreferences.isReverseSorting()) {
            sortStyleChipGroup.check(R.id.descending)
        } else {
            sortStyleChipGroup.check(R.id.ascending)
        }

        when (UntaggedPreferences.getAppsType()) {
            SortConstant.BOTH -> applicationTypeChipGroup.check(R.id.both)
            SortConstant.SYSTEM -> applicationTypeChipGroup.check(R.id.system)
            SortConstant.USER -> applicationTypeChipGroup.check(R.id.user)
        }

        sortChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            for (id in checkedIds) {
                when (id) {
                    R.id.name -> UntaggedPreferences.setSortStyle(Sort.NAME)
                    R.id.package_name -> UntaggedPreferences.setSortStyle(Sort.PACKAGE_NAME)
                    R.id.install_date -> UntaggedPreferences.setSortStyle(Sort.INSTALL_DATE)
                    R.id.update_date -> UntaggedPreferences.setSortStyle(Sort.UPDATE_DATE)
                    R.id.size -> UntaggedPreferences.setSortStyle(Sort.SIZE)
                    R.id.target_sdk -> UntaggedPreferences.setSortStyle(Sort.TARGET_SDK)
                    R.id.min_sdk -> UntaggedPreferences.setSortStyle(Sort.MIN_SDK)
                }
            }
        }

        sortStyleChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            for (id in checkedIds) {
                when (id) {
                    R.id.ascending -> UntaggedPreferences.setReverseSorting(false)
                    R.id.descending -> UntaggedPreferences.setReverseSorting(true)
                }
            }
        }

        applicationTypeChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            for (id in checkedIds) {
                when (id) {
                    R.id.both -> UntaggedPreferences.setAppsType(SortConstant.BOTH)
                    R.id.system -> UntaggedPreferences.setAppsType(SortConstant.SYSTEM)
                    R.id.user -> UntaggedPreferences.setAppsType(SortConstant.USER)
                }
            }
        }
    }

    companion object {
        fun FragmentManager.showUntaggedSortDialog() {
            UntaggedSort().show(this, "untagged_sort")
        }
    }
}
