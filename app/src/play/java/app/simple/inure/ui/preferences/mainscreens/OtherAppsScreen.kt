package app.simple.inure.ui.preferences.mainscreens

import android.os.Bundle
import app.simple.inure.extensions.fragments.ScopedFragment

class OtherAppsScreen : ScopedFragment() {

    companion object {
        fun newInstance(): OtherAppsScreen {
            val args = Bundle()
            val fragment = OtherAppsScreen()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "OtherAppsScreen"
    }
}