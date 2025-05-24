package app.simple.inure.dialogs.configuration

import androidx.fragment.app.FragmentManager
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment

class VirusTotalAPI : ScopedBottomSheetFragment() {
    companion object {
        const val TAG = "VirusTotalAPI"
        fun newInstance(): VirusTotalAPI = VirusTotalAPI()
        fun FragmentManager.showVirusTotalAPI(): VirusTotalAPI = VirusTotalAPI()
        interface onVirusTotalAPIListener {
            fun onVirusTotalAPI() {}
        }
    }

    fun setOnVirusTotalAPIListener(listener: onVirusTotalAPIListener) {}
}