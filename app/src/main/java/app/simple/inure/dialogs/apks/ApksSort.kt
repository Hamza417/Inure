package app.simple.inure.dialogs.apks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.constants.SortConstant
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.preferences.ApkBrowserPreferences
import app.simple.inure.util.FlagUtils
import app.simple.inure.util.SortApks
import com.google.android.material.chip.ChipGroup

class ApksSort : ScopedBottomSheetFragment() {

    private lateinit var sortChipGroup: ChipGroup
    private lateinit var sortStyleChipGroup: ChipGroup
    private lateinit var filterChipGroup: ChipGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_sort_apks, container, false)

        sortChipGroup = view.findViewById(R.id.sort_chip_group)
        sortStyleChipGroup = view.findViewById(R.id.sorting_style_chip_group)
        filterChipGroup = view.findViewById(R.id.filter_chip_group)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSortChipGroup()
        setupSortStyleChipGroup()
        setupFilterChipGroup()
    }

    private fun setupSortChipGroup() {
        when (ApkBrowserPreferences.getSortStyle()) {
            SortApks.NAME -> {
                sortChipGroup.check(R.id.name)
            }
            SortApks.SIZE -> {
                sortChipGroup.check(R.id.size)
            }
            SortApks.DATE -> {
                sortChipGroup.check(R.id.date)
            }
        }

        sortChipGroup.setOnCheckedStateChangeListener { _, checkedId ->
            checkedId.forEach { id ->
                when (id) {
                    R.id.name -> {
                        ApkBrowserPreferences.setSortStyle(SortApks.NAME)
                    }
                    R.id.size -> {
                        ApkBrowserPreferences.setSortStyle(SortApks.SIZE)
                    }
                    R.id.date -> {
                        ApkBrowserPreferences.setSortStyle(SortApks.DATE)
                    }
                }
            }
        }
    }

    private fun setupSortStyleChipGroup() {
        if (ApkBrowserPreferences.isReverseSorting()) {
            sortStyleChipGroup.check(R.id.descending)
        } else {
            sortStyleChipGroup.check(R.id.ascending)
        }

        sortStyleChipGroup.setOnCheckedStateChangeListener { _, checkedId ->
            checkedId.forEach { id ->
                when (id) {
                    R.id.ascending -> {
                        ApkBrowserPreferences.setReverseSorting(false)
                    }
                    R.id.descending -> {
                        ApkBrowserPreferences.setReverseSorting(true)
                    }
                }
            }
        }
    }

    private fun setupFilterChipGroup() {
        if (FlagUtils.isFlagSet(ApkBrowserPreferences.getApkFilter(), SortConstant.APKS_APK)) {
            filterChipGroup.check(R.id.apk_)
        }

        if (FlagUtils.isFlagSet(ApkBrowserPreferences.getApkFilter(), SortConstant.APKS_APKS)) {
            filterChipGroup.check(R.id.apks)
        }

        if (FlagUtils.isFlagSet(ApkBrowserPreferences.getApkFilter(), SortConstant.APKS_APKM)) {
            filterChipGroup.check(R.id.apkm)
        }

        if (FlagUtils.isFlagSet(ApkBrowserPreferences.getApkFilter(), SortConstant.APKS_XAPK)) {
            filterChipGroup.check(R.id.xapk)
        }

        if (FlagUtils.isFlagSet(ApkBrowserPreferences.getApkFilter(), SortConstant.APKS_HIDDEN)) {
            filterChipGroup.check(R.id.hidden)
        }

        filterChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            var flags = ApkBrowserPreferences.getApkFilter()

            flags = if (checkedIds.contains(R.id.apk_)) {
                FlagUtils.setFlag(flags, SortConstant.APKS_APK)
            } else {
                FlagUtils.unsetFlag(flags, SortConstant.APKS_APK)
            }

            flags = if (checkedIds.contains(R.id.apks)) {
                FlagUtils.setFlag(flags, SortConstant.APKS_APKS)
            } else {
                FlagUtils.unsetFlag(flags, SortConstant.APKS_APKS)
            }

            flags = if (checkedIds.contains(R.id.apkm)) {
                FlagUtils.setFlag(flags, SortConstant.APKS_APKM)
            } else {
                FlagUtils.unsetFlag(flags, SortConstant.APKS_APKM)
            }

            flags = if (checkedIds.contains(R.id.xapk)) {
                FlagUtils.setFlag(flags, SortConstant.APKS_XAPK)
            } else {
                FlagUtils.unsetFlag(flags, SortConstant.APKS_XAPK)
            }

            flags = if (checkedIds.contains(R.id.hidden)) {
                FlagUtils.setFlag(flags, SortConstant.APKS_HIDDEN)
            } else {
                FlagUtils.unsetFlag(flags, SortConstant.APKS_HIDDEN)
            }

            ApkBrowserPreferences.setApkFilter(flags)
        }
    }

    companion object {
        fun newInstance(): ApksSort {
            val args = Bundle()
            val fragment = ApksSort()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showApksSort() {
            ApksSort().show(this, "apks_sort")
        }
    }
}