package app.simple.inure.dialogs.apps

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.constants.SortConstant
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.preferences.MainPreferences
import app.simple.inure.util.Sort
import com.google.android.material.chip.ChipGroup

class AppsSort : ScopedBottomSheetFragment() {

    private lateinit var sortChipGroup: ChipGroup
    private lateinit var sortStyleChipGroup: ChipGroup
    private lateinit var filterChipGroup: ChipGroup
    private lateinit var applicationTypeChipGroup: ChipGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_apps_sort, container, false)

        sortChipGroup = view.findViewById(R.id.sort_chip_group)
        sortStyleChipGroup = view.findViewById(R.id.sorting_style_chip_group)
        filterChipGroup = view.findViewById(R.id.filter_chip_group)
        applicationTypeChipGroup = view.findViewById(R.id.application_type_chip_group)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when (MainPreferences.getSortStyle()) {
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
        }

        if (MainPreferences.isReverseSorting()) {
            sortStyleChipGroup.check(R.id.descending)
        } else {
            sortStyleChipGroup.check(R.id.ascending)
        }

        when (MainPreferences.getAppsCategory()) {
            SortConstant.BOTH -> {
                filterChipGroup.check(R.id.all)
            }
            SortConstant.SYSTEM -> {
                filterChipGroup.check(R.id.system)
            }
            SortConstant.USER -> {
                filterChipGroup.check(R.id.user)
            }
        }

        when (MainPreferences.getApplicationType()) {
            SortConstant.ALL -> {
                applicationTypeChipGroup.check(R.id.all)
            }
            SortConstant.DISABLED -> {
                applicationTypeChipGroup.check(R.id.disabled)
            }
            SortConstant.ENABLED -> {
                applicationTypeChipGroup.check(R.id.enabled)
            }
            SortConstant.APK -> {
                applicationTypeChipGroup.check(R.id.apk)
            }
            SortConstant.SPLITS -> {
                applicationTypeChipGroup.check(R.id.split)
            }
        }

        sortChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            Log.d("AppsSort", "Checked: $checkedIds")
            for (id in checkedIds) {
                Log.d("AppsSort", "Checked: $id")
                when (id) {
                    R.id.name -> {
                        MainPreferences.setSortStyle(Sort.NAME)
                    }
                    R.id.package_name -> {
                        MainPreferences.setSortStyle(Sort.PACKAGE_NAME)
                    }
                    R.id.install_date -> {
                        MainPreferences.setSortStyle(Sort.INSTALL_DATE)
                    }
                    R.id.update_date -> {
                        MainPreferences.setSortStyle(Sort.UPDATE_DATE)
                    }
                    R.id.size -> {
                        MainPreferences.setSortStyle(Sort.SIZE)
                    }
                }
            }
        }

        sortStyleChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            for (id in checkedIds) {
                when (id) {
                    R.id.ascending -> {
                        MainPreferences.setReverseSorting(false)
                    }
                    R.id.descending -> {
                        MainPreferences.setReverseSorting(true)
                    }
                }
            }
        }

        filterChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            for (id in checkedIds) {
                when (id) {
                    R.id.both -> {
                        MainPreferences.setAppsCategory(SortConstant.BOTH)
                    }
                    R.id.system -> {
                        MainPreferences.setAppsCategory(SortConstant.SYSTEM)
                    }
                    R.id.user -> {
                        MainPreferences.setAppsCategory(SortConstant.USER)
                    }
                }
            }
        }

        applicationTypeChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            for (id in checkedIds) {
                when (id) {
                    R.id.all -> {
                        MainPreferences.setApplicationType(SortConstant.ALL)
                    }
                    R.id.disabled -> {
                        MainPreferences.setApplicationType(SortConstant.DISABLED)
                    }
                    R.id.enabled -> {
                        MainPreferences.setApplicationType(SortConstant.ENABLED)
                    }
                    R.id.apk -> {
                        MainPreferences.setApplicationType(SortConstant.APK)
                    }
                    R.id.split -> {
                        MainPreferences.setApplicationType(SortConstant.SPLITS)
                    }
                }
            }
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