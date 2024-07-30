package app.simple.inure.ui.installer

import android.view.ViewGroup
import app.simple.inure.extensions.fragments.ScopedFragment

class Services : ScopedFragment() {

    override fun setupBackPressedDispatcher() {
        /* no-op */
    }

    override fun setupBackPressedCallback(view: ViewGroup) {
        /* no-op */
    }

    companion object {
        fun newInstance(): Services {
            return Services()
        }

        private const val TAG = "Services"
    }
}
