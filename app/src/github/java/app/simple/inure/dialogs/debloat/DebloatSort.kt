package app.simple.inure.dialogs.debloat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.constants.DebloatSortConstants
import app.simple.inure.constants.SortConstant
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.preferences.DebloatPreferences
import app.simple.inure.util.FlagUtils
import com.google.android.material.chip.ChipGroup

class DebloatSort : ScopedBottomSheetFragment() {

    private lateinit var sortChipGroup: ChipGroup
    private lateinit var sortingStyleChipGroup: ChipGroup
    private lateinit var applicationTypeChipGroup: ChipGroup
    private lateinit var listChipGroup: ChipGroup
    private lateinit var methodChipGroup: ChipGroup
    private lateinit var stateChipGroup: ChipGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_sort_debloat, container, false)

        sortChipGroup = view.findViewById(R.id.sort_chip_group)
        sortingStyleChipGroup = view.findViewById(R.id.sorting_style_chip_group)
        applicationTypeChipGroup = view.findViewById(R.id.application_type_chip_group)
        listChipGroup = view.findViewById(R.id.list_chip_group)
        methodChipGroup = view.findViewById(R.id.method_chip_group)
        stateChipGroup = view.findViewById(R.id.state_chip_group)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setSortChipStates()
        setSortingStyleChipStates()
        setApplicationTypeChipStates()
        setListChipStates()
        setMethodChipStates()
        setStateChipStates()

        sortChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            checkedIds.forEach {
                when (it) {
                    R.id.name -> {
                        DebloatPreferences.setSortBy(app.simple.inure.sort.DebloatSort.SORT_BY_NAME)
                    }
                    R.id.package_name -> {
                        DebloatPreferences.setSortBy(app.simple.inure.sort.DebloatSort.SORT_BY_PACKAGE_NAME)
                    }
                }
            }
        }

        sortingStyleChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            checkedIds.forEach {
                when (it) {
                    R.id.ascending -> {
                        DebloatPreferences.setSortingStyle(SortConstant.ASCENDING)
                    }
                    R.id.descending -> {
                        DebloatPreferences.setSortingStyle(SortConstant.DESCENDING)
                    }
                }
            }
        }

        applicationTypeChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            checkedIds.forEach {
                when (it) {
                    R.id.both -> {
                        DebloatPreferences.setApplicationType(SortConstant.BOTH)
                    }
                    R.id.system -> {
                        DebloatPreferences.setApplicationType(SortConstant.SYSTEM)
                    }
                    R.id.user -> {
                        DebloatPreferences.setApplicationType(SortConstant.USER)
                    }
                }
            }
        }

        listChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            var sourceFlags = DebloatPreferences.getListType()

            sourceFlags = if (checkedIds.contains(R.id.aosp)) {
                FlagUtils.setFlag(sourceFlags, DebloatSortConstants.AOSP)
            } else {
                FlagUtils.unsetFlag(sourceFlags, DebloatSortConstants.AOSP)
            }

            sourceFlags = if (checkedIds.contains(R.id.carrier)) {
                FlagUtils.setFlag(sourceFlags, DebloatSortConstants.CARRIER)
            } else {
                FlagUtils.unsetFlag(sourceFlags, DebloatSortConstants.CARRIER)
            }

            sourceFlags = if (checkedIds.contains(R.id.google)) {
                FlagUtils.setFlag(sourceFlags, DebloatSortConstants.GOOGLE)
            } else {
                FlagUtils.unsetFlag(sourceFlags, DebloatSortConstants.GOOGLE)
            }

            sourceFlags = if (checkedIds.contains(R.id.misc)) {
                FlagUtils.setFlag(sourceFlags, DebloatSortConstants.MISC)
            } else {
                FlagUtils.unsetFlag(sourceFlags, DebloatSortConstants.MISC)
            }

            sourceFlags = if (checkedIds.contains(R.id.oem)) {
                FlagUtils.setFlag(sourceFlags, DebloatSortConstants.OEM)
            } else {
                FlagUtils.unsetFlag(sourceFlags, DebloatSortConstants.OEM)
            }

            sourceFlags = if (checkedIds.contains(R.id.pending)) {
                FlagUtils.setFlag(sourceFlags, DebloatSortConstants.PENDING)
            } else {
                FlagUtils.unsetFlag(sourceFlags, DebloatSortConstants.PENDING)
            }

            sourceFlags = if (checkedIds.contains(R.id.unlisted_list)) {
                FlagUtils.setFlag(sourceFlags, DebloatSortConstants.UNLISTED_LIST)
            } else {
                FlagUtils.unsetFlag(sourceFlags, DebloatSortConstants.UNLISTED_LIST)
            }

            DebloatPreferences.setListType(sourceFlags)
        }

        methodChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            var sourceFlags = DebloatPreferences.getRemovalType()

            sourceFlags = if (checkedIds.contains(R.id.recommended)) {
                FlagUtils.setFlag(sourceFlags, DebloatSortConstants.RECOMMENDED)
            } else {
                FlagUtils.unsetFlag(sourceFlags, DebloatSortConstants.RECOMMENDED)
            }

            sourceFlags = if (checkedIds.contains(R.id.advanced)) {
                FlagUtils.setFlag(sourceFlags, DebloatSortConstants.ADVANCED)
            } else {
                FlagUtils.unsetFlag(sourceFlags, DebloatSortConstants.ADVANCED)
            }

            sourceFlags = if (checkedIds.contains(R.id.expert)) {
                FlagUtils.setFlag(sourceFlags, DebloatSortConstants.EXPERT)
            } else {
                FlagUtils.unsetFlag(sourceFlags, DebloatSortConstants.EXPERT)
            }

            sourceFlags = if (checkedIds.contains(R.id.unsafe)) {
                FlagUtils.setFlag(sourceFlags, DebloatSortConstants.UNSAFE)
            } else {
                FlagUtils.unsetFlag(sourceFlags, DebloatSortConstants.UNSAFE)
            }

            sourceFlags = if (checkedIds.contains(R.id.unlisted)) {
                FlagUtils.setFlag(sourceFlags, DebloatSortConstants.UNLISTED)
            } else {
                FlagUtils.unsetFlag(sourceFlags, DebloatSortConstants.UNLISTED)
            }

            DebloatPreferences.setRemovalType(sourceFlags)
        }

        stateChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            var sourceFlags = DebloatPreferences.getState()

            sourceFlags = if (checkedIds.contains(R.id.enabled)) {
                FlagUtils.setFlag(sourceFlags, DebloatSortConstants.ENABLED)
            } else {
                FlagUtils.unsetFlag(sourceFlags, DebloatSortConstants.ENABLED)
            }

            sourceFlags = if (checkedIds.contains(R.id.disabled)) {
                FlagUtils.setFlag(sourceFlags, DebloatSortConstants.DISABLED)
            } else {
                FlagUtils.unsetFlag(sourceFlags, DebloatSortConstants.DISABLED)
            }

            sourceFlags = if (checkedIds.contains(R.id.uninstalled)) {
                FlagUtils.setFlag(sourceFlags, DebloatSortConstants.UNINSTALLED)
            } else {
                FlagUtils.unsetFlag(sourceFlags, DebloatSortConstants.UNINSTALLED)
            }

            DebloatPreferences.setState(sourceFlags)
        }
    }

    private fun setSortChipStates() {
        when (DebloatPreferences.getSortBy()) {
            app.simple.inure.sort.DebloatSort.SORT_BY_NAME -> sortChipGroup.check(R.id.name)
            app.simple.inure.sort.DebloatSort.SORT_BY_PACKAGE_NAME -> sortChipGroup.check(R.id.package_name)
        }
    }

    private fun setSortingStyleChipStates() {
        when (DebloatPreferences.getSortingStyle()) {
            SortConstant.ASCENDING -> sortingStyleChipGroup.check(R.id.ascending)
            SortConstant.DESCENDING -> sortingStyleChipGroup.check(R.id.descending)
        }
    }

    private fun setApplicationTypeChipStates() {
        when (DebloatPreferences.getApplicationType()) {
            SortConstant.BOTH -> applicationTypeChipGroup.check(R.id.both)
            SortConstant.SYSTEM -> applicationTypeChipGroup.check(R.id.system)
            SortConstant.USER -> applicationTypeChipGroup.check(R.id.user)
        }
    }

    private fun setListChipStates() {
        if (FlagUtils.isFlagSet(DebloatPreferences.getListType(), DebloatSortConstants.AOSP)) {
            listChipGroup.check(R.id.aosp)
        }

        if (FlagUtils.isFlagSet(DebloatPreferences.getListType(), DebloatSortConstants.CARRIER)) {
            listChipGroup.check(R.id.carrier)
        }

        if (FlagUtils.isFlagSet(DebloatPreferences.getListType(), DebloatSortConstants.GOOGLE)) {
            listChipGroup.check(R.id.google)
        }

        if (FlagUtils.isFlagSet(DebloatPreferences.getListType(), DebloatSortConstants.MISC)) {
            listChipGroup.check(R.id.misc)
        }

        if (FlagUtils.isFlagSet(DebloatPreferences.getListType(), DebloatSortConstants.OEM)) {
            listChipGroup.check(R.id.oem)
        }

        if (FlagUtils.isFlagSet(DebloatPreferences.getListType(), DebloatSortConstants.PENDING)) {
            listChipGroup.check(R.id.pending)
        }

        if (FlagUtils.isFlagSet(DebloatPreferences.getListType(), DebloatSortConstants.UNLISTED_LIST)) {
            listChipGroup.check(R.id.unlisted_list)
        }
    }

    private fun setMethodChipStates() {
        if (FlagUtils.isFlagSet(DebloatPreferences.getRemovalType(), DebloatSortConstants.RECOMMENDED)) {
            methodChipGroup.check(R.id.recommended)
        }

        if (FlagUtils.isFlagSet(DebloatPreferences.getRemovalType(), DebloatSortConstants.ADVANCED)) {
            methodChipGroup.check(R.id.advanced)
        }

        if (FlagUtils.isFlagSet(DebloatPreferences.getRemovalType(), DebloatSortConstants.EXPERT)) {
            methodChipGroup.check(R.id.expert)
        }

        if (FlagUtils.isFlagSet(DebloatPreferences.getRemovalType(), DebloatSortConstants.UNSAFE)) {
            methodChipGroup.check(R.id.unsafe)
        }

        if (FlagUtils.isFlagSet(DebloatPreferences.getRemovalType(), DebloatSortConstants.UNLISTED)) {
            methodChipGroup.check(R.id.unlisted)
        }
    }

    private fun setStateChipStates() {
        if (FlagUtils.isFlagSet(DebloatPreferences.getState(), DebloatSortConstants.ENABLED)) {
            stateChipGroup.check(R.id.enabled)
        }

        if (FlagUtils.isFlagSet(DebloatPreferences.getState(), DebloatSortConstants.DISABLED)) {
            stateChipGroup.check(R.id.disabled)
        }

        if (FlagUtils.isFlagSet(DebloatPreferences.getState(), DebloatSortConstants.UNINSTALLED)) {
            stateChipGroup.check(R.id.uninstalled)
        }
    }

    companion object {
        fun newInstance(): DebloatSort {
            val args = Bundle()
            val fragment = DebloatSort()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showDebloatFilter(): DebloatSort {
            val dialog = newInstance()
            dialog.show(this, "debloat_filter")
            return dialog
        }
    }
}