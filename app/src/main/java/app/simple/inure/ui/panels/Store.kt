package app.simple.inure.ui.panels

import android.os.Bundle
import android.view.View
import android.widget.Toast
import app.simple.inure.extensions.fragments.ScopedFragment

/**
 * This is a placeholder fragment for the store panel
 */
class Store : ScopedFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Toast.makeText(requireContext(), "This is an empty class!!", Toast.LENGTH_SHORT).show()
    }
}