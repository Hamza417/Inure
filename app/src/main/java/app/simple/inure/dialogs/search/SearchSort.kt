package app.simple.inure.dialogs.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.constants.SortConstant
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.preferences.SearchPreferences
import app.simple.inure.util.FlagUtils
import app.simple.inure.util.Sort
import com.google.android.material.chip.ChipGroup

class SearchSort : ScopedBottomSheetFragment() {

    private lateinit var sortChipGroup: ChipGroup
    private lateinit var sortStyleChipGroup: ChipGroup
    private lateinit var applicationTypeChipGroup: ChipGroup
    private lateinit var filterChipGroup: ChipGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_sort_search, container, false)

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
        sortChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            for (id in checkedIds) {
                when (id) {
                    R.id.name -> {
                        SearchPreferences.setSortStyle(Sort.NAME)
                    }
                    R.id.package_name -> {
                        SearchPreferences.setSortStyle(Sort.PACKAGE_NAME)
                    }
                    R.id.install_date -> {
                        SearchPreferences.setSortStyle(Sort.INSTALL_DATE)
                    }
                    R.id.update_date -> {
                        SearchPreferences.setSortStyle(Sort.UPDATE_DATE)
                    }
                    R.id.size -> {
                        SearchPreferences.setSortStyle(Sort.SIZE)
                    }
                    R.id.target_sdk -> {
                        SearchPreferences.setSortStyle(Sort.TARGET_SDK)
                    }
                    R.id.min_sdk -> {
                        SearchPreferences.setSortStyle(Sort.MIN_SDK)
                    }
                }
            }
        }

        when (SearchPreferences.getSortStyle()) {
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
            Sort.MIN_SDK -> {
                sortChipGroup.check(R.id.min_sdk)
            }
        }
    }

    private fun setSortStyleChipGroup() {
        if (SearchPreferences.isReverseSorting()) {
            sortStyleChipGroup.check(R.id.descending)
        } else {
            sortStyleChipGroup.check(R.id.ascending)
        }

        sortStyleChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            for (id in checkedIds) {
                when (id) {
                    R.id.ascending -> {
                        SearchPreferences.setReverseSorting(false)
                    }
                    R.id.descending -> {
                        SearchPreferences.setReverseSorting(true)
                    }
                }
            }
        }
    }

    private fun setApplicationTypeChipGroup() {
        applicationTypeChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            for (id in checkedIds) {
                when (id) {
                    R.id.both -> {
                        SearchPreferences.setAppsCategory(SortConstant.BOTH)
                    }
                    R.id.system -> {
                        SearchPreferences.setAppsCategory(SortConstant.SYSTEM)
                    }
                    R.id.user -> {
                        SearchPreferences.setAppsCategory(SortConstant.USER)
                    }
                }
            }
        }

        when (SearchPreferences.getAppsCategory()) {
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

    private fun setFilterChipGroup() {
        if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.DISABLED)) {
            filterChipGroup.check(R.id.disabled)
        }

        if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.ENABLED)) {
            filterChipGroup.check(R.id.enabled)
        }

        if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.APK)) {
            filterChipGroup.check(R.id.apk)
        }

        if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.SPLIT)) {
            filterChipGroup.check(R.id.split)
        }

        if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.UNINSTALLED)) {
            filterChipGroup.check(R.id.uninstalled)
        }

        if (FlagUtils.isFlagSet(SearchPreferences.getAppsFilter(), SortConstant.COMBINE_FLAGS)) {
            filterChipGroup.check(R.id.combine_flags)
        }

        filterChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            var sourceFlags = SearchPreferences.getAppsFilter()

            sourceFlags = if (checkedIds.contains(R.id.disabled)) {
                FlagUtils.setFlag(sourceFlags, SortConstant.DISABLED)
            } else {
                FlagUtils.unsetFlag(sourceFlags, SortConstant.DISABLED)
            }

            sourceFlags = if (checkedIds.contains(R.id.enabled)) {
                FlagUtils.setFlag(sourceFlags, SortConstant.ENABLED)
            } else {
                FlagUtils.unsetFlag(sourceFlags, SortConstant.ENABLED)
            }

            sourceFlags = if (checkedIds.contains(R.id.apk)) {
                FlagUtils.setFlag(sourceFlags, SortConstant.APK)
            } else {
                FlagUtils.unsetFlag(sourceFlags, SortConstant.APK)
            }

            sourceFlags = if (checkedIds.contains(R.id.split)) {
                FlagUtils.setFlag(sourceFlags, SortConstant.SPLIT)
            } else {
                FlagUtils.unsetFlag(sourceFlags, SortConstant.SPLIT)
            }

            sourceFlags = if (checkedIds.contains(R.id.uninstalled)) {
                FlagUtils.setFlag(sourceFlags, SortConstant.UNINSTALLED)
            } else {
                FlagUtils.unsetFlag(sourceFlags, SortConstant.UNINSTALLED)
            }

            sourceFlags = if (checkedIds.contains(R.id.combine_flags)) {
                FlagUtils.setFlag(sourceFlags, SortConstant.COMBINE_FLAGS)
            } else {
                FlagUtils.unsetFlag(sourceFlags, SortConstant.COMBINE_FLAGS)
            }

            SearchPreferences.setAppsFilter(sourceFlags)
        }
    }

    companion object {
        fun newInstance(): SearchSort {
            val args = Bundle()
            val fragment = SearchSort()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showSearchSort() {
            val dialog = newInstance()
            dialog.show(this, "search_sort")
        }
    }
}