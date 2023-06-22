package app.simple.inure.dialogs.boot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.constants.SortConstant
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.preferences.BootManagerPreferences
import app.simple.inure.util.FlagUtils
import app.simple.inure.util.SortBootManager
import com.google.android.material.chip.ChipGroup

class BootManagerSort : ScopedBottomSheetFragment() {

    private lateinit var sortChipGroup: ChipGroup
    private lateinit var sortStyleChipGroup: ChipGroup
    private lateinit var applicationTypeChipGroup: ChipGroup
    private lateinit var filterChipGroup: ChipGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_sort_boot_manager, container, false)

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

    private fun setSortChipGroup() {
        when (BootManagerPreferences.getSortStyle()) {
            SortBootManager.NAME -> {
                sortChipGroup.check(R.id.name)
            }
            SortBootManager.SIZE -> {
                sortChipGroup.check(R.id.size)
            }
            SortBootManager.PACKAGE_NAME -> {
                sortChipGroup.check(R.id.package_name)
            }
            SortBootManager.INSTALL_DATE -> {
                sortChipGroup.check(R.id.install_date)
            }
            SortBootManager.UPDATE_DATE -> {
                sortChipGroup.check(R.id.update_date)
            }
            SortBootManager.TARGET_SDK -> {
                sortChipGroup.check(R.id.target_sdk)
            }
        }

        sortChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            checkedIds.forEach {
                when (it) {
                    R.id.name -> {
                        BootManagerPreferences.setSortStyle(SortBootManager.NAME)
                    }
                    R.id.size -> {
                        BootManagerPreferences.setSortStyle(SortBootManager.SIZE)
                    }
                    R.id.package_name -> {
                        BootManagerPreferences.setSortStyle(SortBootManager.PACKAGE_NAME)
                    }
                    R.id.install_date -> {
                        BootManagerPreferences.setSortStyle(SortBootManager.INSTALL_DATE)
                    }
                    R.id.update_date -> {
                        BootManagerPreferences.setSortStyle(SortBootManager.UPDATE_DATE)
                    }
                    R.id.target_sdk -> {
                        BootManagerPreferences.setSortStyle(SortBootManager.TARGET_SDK)
                    }
                }
            }
        }
    }

    private fun setSortStyleChipGroup() {
        if (BootManagerPreferences.isSortingReversed()) {
            sortStyleChipGroup.check(R.id.descending)
        } else {
            sortStyleChipGroup.check(R.id.ascending)
        }

        sortStyleChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            checkedIds.forEach {
                when (it) {
                    R.id.ascending -> {
                        BootManagerPreferences.setSortingReversed(false)
                    }
                    R.id.descending -> {
                        BootManagerPreferences.setSortingReversed(true)
                    }
                }
            }
        }
    }

    private fun setApplicationTypeChipGroup() {
        when (BootManagerPreferences.getAppsCategory()) {
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
                        BootManagerPreferences.setAppsCategory(SortConstant.SYSTEM)
                    }
                    R.id.user -> {
                        BootManagerPreferences.setAppsCategory(SortConstant.USER)
                    }
                    R.id.both -> {
                        BootManagerPreferences.setAppsCategory(SortConstant.BOTH)
                    }
                }
            }
        }
    }

    private fun setFilterChipGroup() {
        if (FlagUtils.isFlagSet(BootManagerPreferences.getFilter(), SortConstant.BOOT_ENABLED)) {
            filterChipGroup.check(R.id.enabled)
        }

        if (FlagUtils.isFlagSet(BootManagerPreferences.getFilter(), SortConstant.BOOT_DISABLED)) {
            filterChipGroup.check(R.id.disabled)
        }

        filterChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            var flags = BootManagerPreferences.getFilter()

            flags = if (checkedIds.contains(R.id.enabled)) {
                FlagUtils.setFlag(flags, SortConstant.BOOT_ENABLED)
            } else {
                FlagUtils.unsetFlag(flags, SortConstant.BOOT_ENABLED)
            }

            flags = if (checkedIds.contains(R.id.disabled)) {
                FlagUtils.setFlag(flags, SortConstant.BOOT_DISABLED)
            } else {
                FlagUtils.unsetFlag(flags, SortConstant.BOOT_DISABLED)
            }

            BootManagerPreferences.setFilter(flags)
        }
    }

    companion object {
        fun newInstance(): BootManagerSort {
            val args = Bundle()
            val fragment = BootManagerSort()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showBootManagerSort() {
            newInstance().show(this, "boot_manager_sort")
        }
    }
}