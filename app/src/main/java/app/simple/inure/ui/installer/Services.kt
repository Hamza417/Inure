package app.simple.inure.ui.installer

import app.simple.inure.extensions.fragments.ScopedFragment

class Services : ScopedFragment() {

    override fun setupBackPressedDispatcher() {
        /* no-op */
    }

    companion object {
        fun newInstance(): Services {
            return Services()
        }

        private const val TAG = "Services"
    }
}
