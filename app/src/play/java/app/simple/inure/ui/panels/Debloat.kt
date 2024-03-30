package app.simple.inure.ui.panels

import android.os.Bundle
import android.view.View
import app.simple.inure.extensions.fragments.ScopedFragment

class Debloat : ScopedFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showWarning("This feature is not available in your version of the app")
    }

    companion object {
        fun newInstance() = Debloat()

        const val TAG = "Debloat"
    }
}
