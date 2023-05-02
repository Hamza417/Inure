package app.simple.inure.ui.installer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.extensions.fragments.ScopedFragment
import java.io.File

class Changes : ScopedFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.installer_fragment_difference, container, false)

        return view
    }

    companion object {
        fun newInstance(file: File): Changes {
            val args = Bundle()
            val fragment = Changes()
            fragment.arguments = args
            return fragment
        }
    }
}