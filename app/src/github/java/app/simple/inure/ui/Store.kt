package app.simple.inure.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.extensions.fragments.ScopedFragment

class Store : ScopedFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fullVersionCheck()

    }

    companion object {
        fun newInstance(): Store {
            val args = Bundle()
            val fragment = Store()
            fragment.arguments = args
            return fragment
        }
    }
}