package app.simple.inure.dialogs.debloat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment

class DebloatFilter : ScopedBottomSheetFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_sort_debloat, container, false)

        return view
    }

    companion object {
        fun newInstance(): DebloatFilter {
            val args = Bundle()
            val fragment = DebloatFilter()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showDebloatFilter(): DebloatFilter {
            val dialog = newInstance()
            dialog.show(this, "debloat_filter")
            return dialog
        }
    }
}