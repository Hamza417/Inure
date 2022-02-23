package app.simple.inure.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.viewmodels.TextDataViewModel

// UI class
class UIController : ScopedFragment() {

    private val textDataViewModel: TextDataViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState) // Let the super class execute first

        textDataViewModel.getText().observe(viewLifecycleOwner) { text ->
            /**
             * Observer block for text data, once the data
             * is posted it will be received here to be
             * assigned/shown to the view object.
             */
            Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
        }

    }
}