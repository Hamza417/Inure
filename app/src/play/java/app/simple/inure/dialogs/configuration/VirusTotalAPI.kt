package app.simple.inure.dialogs.configuration

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentManager
import app.simple.inure.extensions.fragments.ScopedDialogFragment

class VirusTotalAPI : ScopedDialogFragment() {

    companion object {
        const val TAG = "VirusTotalAPI"

        fun newInstance(): VirusTotalAPI {
            return VirusTotalAPI()
        }

        fun FragmentManager.showVirusTotalAPI() {
            val dialog = newInstance()
            if (findFragmentByTag(TAG) == null) {
                dialog.show(this, TAG)
            }
        }
    }
}