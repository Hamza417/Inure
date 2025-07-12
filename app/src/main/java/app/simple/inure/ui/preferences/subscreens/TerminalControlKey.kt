package app.simple.inure.ui.preferences.subscreens

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.adapters.terminal.AdapterControlKey
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.preferences.TerminalPreferences

class TerminalControlKey : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.terminal_font_size, container, false)

        recyclerView = view.findViewById(R.id.font_size_recycler_view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        val adapterControlKey = AdapterControlKey()

        adapterControlKey.onError = {
            showWarning(it, false)
        }

        recyclerView.adapter = adapterControlKey
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            TerminalPreferences.CONTROL_KEY -> {
                Log.i(TerminalFnKey.Companion.TAG, "Ctrl Key changed: $key")
            }
        }
    }

    companion object {
        fun newInstance(): TerminalControlKey {
            val args = Bundle()
            val fragment = TerminalControlKey()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "TerminalControlKey"
    }
}
