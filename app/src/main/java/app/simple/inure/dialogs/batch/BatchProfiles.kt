package app.simple.inure.dialogs.batch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment

class BatchProfiles : ScopedBottomSheetFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_batch_profiles, container, false)

        return view
    }

    companion object {
        fun newInstance(): BatchProfiles {
            val args = Bundle()
            val fragment = BatchProfiles()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showBatchProfiles(): BatchProfiles {
            val dialog = newInstance()
            dialog.show(this, "batch_profiles")
            return dialog
        }
    }
}