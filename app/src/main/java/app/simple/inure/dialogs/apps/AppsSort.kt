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
import app.simple.inure.preferences.AppsPreferences
import app.simple.inure.util.FlagUtils
import app.simple.inure.util.Sort
import com.google.android.material.chip.ChipGroup

class AppsSort : ScopedBottomSheetFragment() {

    private lateinit var sortChipGroup: ChipGroup
    private lateinit var sortStyleChipGroup: ChipGroup
    private lateinit var applicationTypeChipGroup: ChipGroup
    private lateinit var categoryChipGroup: ChipGroup
    private lateinit var filterChipGroup: ChipGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_sort_apps, container, false)

        sortChipGroup = view.findViewById(R.id.sort_chip_group)
        sortStyleChipGroup = view.findViewById(R.id.sorting_style_chip_group)
        filterChipGroup = view.findViewById(R.id.filter_chip_group)
        categoryChipGroup = view.findViewById(R.id.category_chip_group)
        applicationTypeChipGroup = view.findViewById(R.id.application_type_chip_group)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            categoryChipGroup.removeView(view.findViewById(R.id.accessibility))
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            sortChipGroup.removeView(view.findViewById(R.id.min_sdk))
        }

        setAppsCategory()
        setAppsFilter()

        when (AppsPreferences.getSortStyle()) {
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
                kotlin.runCatching { // Probably not needed
                    sortChipGroup.check(R.id.min_sdk)
                }
            }
        }

        if (AppsPreferences.isReverseSorting()) {
            sortStyleChipGroup.check(R.id.descending)
        } else {
            sortStyleChipGroup.check(R.id.ascending)
        }

        when (AppsPreferences.getAppsType()) {
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

        sortChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            for (id in checkedIds) {
                when (id) {
                    R.id.name -> {
                        AppsPreferences.setSortStyle(Sort.NAME)
                    }
                    R.id.package_name -> {
                        AppsPreferences.setSortStyle(Sort.PACKAGE_NAME)
                    }
                    R.id.install_date -> {
                        AppsPreferences.setSortStyle(Sort.INSTALL_DATE)
                    }
                    R.id.update_date -> {
                        AppsPreferences.setSortStyle(Sort.UPDATE_DATE)
                    }
                    R.id.size -> {
                        AppsPreferences.setSortStyle(Sort.SIZE)
                    }
                    R.id.target_sdk -> {
                        AppsPreferences.setSortStyle(Sort.TARGET_SDK)
                    }
                    R.id.min_sdk -> {
                        AppsPreferences.setSortStyle(Sort.MIN_SDK)
                    }
                }
            }
        }

        sortStyleChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            for (id in checkedIds) {
                when (id) {
                    R.id.ascending -> {
                        AppsPreferences.setReverseSorting(false)
                    }
                    R.id.descending -> {
                        AppsPreferences.setReverseSorting(true)
                    }
                }
            }
        }

        applicationTypeChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            for (id in checkedIds) {
                when (id) {
                    R.id.both -> {
                        AppsPreferences.setAppsType(SortConstant.BOTH)
                    }
                    R.id.system -> {
                        AppsPreferences.setAppsType(SortConstant.SYSTEM)
                    }
                    R.id.user -> {
                        AppsPreferences.setAppsType(SortConstant.USER)
                    }
                }
            }
        }

        filterChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            var sourceFlags = AppsPreferences.getAppsFilter()

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

            sourceFlags = if (checkedIds.contains(R.id.foss)) {
                FlagUtils.setFlag(sourceFlags, SortConstant.FOSS)
            } else {
                FlagUtils.unsetFlag(sourceFlags, SortConstant.FOSS)
            }

            AppsPreferences.setAppsFilter(sourceFlags)
        }

        categoryChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            var categoryFlags = SortConstant.ALL_CATEGORIES

            categoryFlags = if (checkedIds.contains(R.id.game)) {
                FlagUtils.setFlag(categoryFlags, SortConstant.CATEGORY_GAME)
            } else {
                FlagUtils.unsetFlag(categoryFlags, SortConstant.CATEGORY_GAME)
            }

            categoryFlags = if (checkedIds.contains(R.id.audio)) {
                FlagUtils.setFlag(categoryFlags, SortConstant.CATEGORY_AUDIO)
            } else {
                FlagUtils.unsetFlag(categoryFlags, SortConstant.CATEGORY_AUDIO)
            }

            categoryFlags = if (checkedIds.contains(R.id.video)) {
                FlagUtils.setFlag(categoryFlags, SortConstant.CATEGORY_VIDEO)
            } else {
                FlagUtils.unsetFlag(categoryFlags, SortConstant.CATEGORY_VIDEO)
            }

            categoryFlags = if (checkedIds.contains(R.id.image)) {
                FlagUtils.setFlag(categoryFlags, SortConstant.CATEGORY_IMAGE)
            } else {
                FlagUtils.unsetFlag(categoryFlags, SortConstant.CATEGORY_IMAGE)
            }

            categoryFlags = if (checkedIds.contains(R.id.social)) {
                FlagUtils.setFlag(categoryFlags, SortConstant.CATEGORY_SOCIAL)
            } else {
                FlagUtils.unsetFlag(categoryFlags, SortConstant.CATEGORY_SOCIAL)
            }

            categoryFlags = if (checkedIds.contains(R.id.productivity)) {
                FlagUtils.setFlag(categoryFlags, SortConstant.CATEGORY_PRODUCTIVITY)
            } else {
                FlagUtils.unsetFlag(categoryFlags, SortConstant.CATEGORY_PRODUCTIVITY)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                categoryFlags = if (checkedIds.contains(R.id.accessibility)) {
                    FlagUtils.setFlag(categoryFlags, SortConstant.CATEGORY_ACCESSIBILITY)
                } else {
                    FlagUtils.unsetFlag(categoryFlags, SortConstant.CATEGORY_ACCESSIBILITY)
                }
            }

            categoryFlags = if (checkedIds.contains(R.id.news)) {
                FlagUtils.setFlag(categoryFlags, SortConstant.CATEGORY_NEWS)
            } else {
                FlagUtils.unsetFlag(categoryFlags, SortConstant.CATEGORY_NEWS)
            }

            categoryFlags = if (checkedIds.contains(R.id.maps)) {
                FlagUtils.setFlag(categoryFlags, SortConstant.CATEGORY_MAPS)
            } else {
                FlagUtils.unsetFlag(categoryFlags, SortConstant.CATEGORY_MAPS)
            }

            categoryFlags = if (checkedIds.contains(R.id.unspecified)) {
                FlagUtils.setFlag(categoryFlags, SortConstant.CATEGORY_UNSPECIFIED)
            } else {
                FlagUtils.unsetFlag(categoryFlags, SortConstant.CATEGORY_UNSPECIFIED)
            }

            AppsPreferences.setAppsCategory(categoryFlags)
        }
    }

    private fun setAppsCategory() {
        if (FlagUtils.isFlagSet(AppsPreferences.getAppsCategory(), SortConstant.CATEGORY_GAME)) {
            categoryChipGroup.check(R.id.game)
        }

        if (FlagUtils.isFlagSet(AppsPreferences.getAppsCategory(), SortConstant.CATEGORY_AUDIO)) {
            categoryChipGroup.check(R.id.audio)
        }

        if (FlagUtils.isFlagSet(AppsPreferences.getAppsCategory(), SortConstant.CATEGORY_VIDEO)) {
            categoryChipGroup.check(R.id.video)
        }

        if (FlagUtils.isFlagSet(AppsPreferences.getAppsCategory(), SortConstant.CATEGORY_IMAGE)) {
            categoryChipGroup.check(R.id.image)
        }

        if (FlagUtils.isFlagSet(AppsPreferences.getAppsCategory(), SortConstant.CATEGORY_SOCIAL)) {
            categoryChipGroup.check(R.id.social)
        }

        if (FlagUtils.isFlagSet(AppsPreferences.getAppsCategory(), SortConstant.CATEGORY_NEWS)) {
            categoryChipGroup.check(R.id.news)
        }

        if (FlagUtils.isFlagSet(AppsPreferences.getAppsCategory(), SortConstant.CATEGORY_MAPS)) {
            categoryChipGroup.check(R.id.maps)
        }

        if (FlagUtils.isFlagSet(AppsPreferences.getAppsCategory(), SortConstant.CATEGORY_PRODUCTIVITY)) {
            categoryChipGroup.check(R.id.productivity)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (FlagUtils.isFlagSet(AppsPreferences.getAppsCategory(), SortConstant.CATEGORY_ACCESSIBILITY)) {
                categoryChipGroup.check(R.id.accessibility)
            }
        }

        if (FlagUtils.isFlagSet(AppsPreferences.getAppsCategory(), SortConstant.CATEGORY_UNSPECIFIED)) {
            categoryChipGroup.check(R.id.unspecified)
        }
    }

    private fun setAppsFilter() {
        if (FlagUtils.isFlagSet(AppsPreferences.getAppsFilter(), SortConstant.DISABLED)) {
            filterChipGroup.check(R.id.disabled)
        }

        if (FlagUtils.isFlagSet(AppsPreferences.getAppsFilter(), SortConstant.ENABLED)) {
            filterChipGroup.check(R.id.enabled)
        }

        if (FlagUtils.isFlagSet(AppsPreferences.getAppsFilter(), SortConstant.APK)) {
            filterChipGroup.check(R.id.apk)
        }

        if (FlagUtils.isFlagSet(AppsPreferences.getAppsFilter(), SortConstant.SPLIT)) {
            filterChipGroup.check(R.id.split)
        }

        if (FlagUtils.isFlagSet(AppsPreferences.getAppsFilter(), SortConstant.UNINSTALLED)) {
            filterChipGroup.check(R.id.uninstalled)
        }

        if (FlagUtils.isFlagSet(AppsPreferences.getAppsFilter(), SortConstant.FOSS)) {
            filterChipGroup.check(R.id.foss)
        }
    }

    companion object {
        fun newInstance(): AppsSort {
            val args = Bundle()
            val fragment = AppsSort()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showAppsSortDialog() {
            val dialog = newInstance()
            dialog.show(this, "apps_sort_dialog")
        }
    }
}