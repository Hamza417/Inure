package app.simple.inure.ui.installer

import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment

class Services : ScopedBottomSheetFragment() {

    companion object {
        fun newInstance(): Services {
            return Services()
        }

        private const val TAG = "Services"
    }
}