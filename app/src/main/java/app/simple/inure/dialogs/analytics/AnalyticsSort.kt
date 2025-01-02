package app.simple.inure.dialogs.analytics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.constants.SortConstant
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.preferences.AnalyticsPreferences
import com.google.android.material.chip.ChipGroup

class AnalyticsSort : ScopedBottomSheetFragment() {

    private lateinit var applicationTypeChipGroup: ChipGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_sort_analytics, container, false)

        applicationTypeChipGroup = view.findViewById(R.id.application_type_chip_group)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setApplicationType()

        applicationTypeChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            for (id in checkedIds) {
                when (id) {
                    R.id.both -> {
                        AnalyticsPreferences.setApplicationType(SortConstant.BOTH)
                    }
                    R.id.system -> {
                        AnalyticsPreferences.setApplicationType(SortConstant.SYSTEM)
                    }
                    R.id.user -> {
                        AnalyticsPreferences.setApplicationType(SortConstant.USER)
                    }
                }
            }
        }
    }

    private fun setApplicationType() {
        when (AnalyticsPreferences.getApplicationType()) {
            SortConstant.BOTH -> applicationTypeChipGroup.check(R.id.both)
            SortConstant.SYSTEM -> applicationTypeChipGroup.check(R.id.system)
            SortConstant.USER -> applicationTypeChipGroup.check(R.id.user)
        }
    }

    companion object {
        const val TAG = "AnalyticsSort"

        fun newInstance(): AnalyticsSort {
            val args = Bundle()
            val fragment = AnalyticsSort()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showAnalyticsSort() {
            newInstance().show(this, TAG)
        }
    }
}