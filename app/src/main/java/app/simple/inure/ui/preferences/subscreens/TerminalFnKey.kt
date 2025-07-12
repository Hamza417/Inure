package app.simple.inure.ui.preferences.subscreens

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.adapters.terminal.AdapterFunctionKey
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.preferences.TerminalPreferences

class TerminalFnKey : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.terminal_font_size, container, false)

        recyclerView = view.findViewById(R.id.font_size_recycler_view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        val adapterFunctionKey = AdapterFunctionKey()

        adapterFunctionKey.onError = {
            showWarning(it, false)
        }

        recyclerView.adapter = adapterFunctionKey
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            TerminalPreferences.FN_KEY -> {
                // childFragmentManager.showTerminalSpecialKeys()
                Log.i(TAG, "Fn Key changed: $key")
            }
        }
    }

    companion object {
        fun newInstance(): TerminalFnKey {
            val args = Bundle()
            val fragment = TerminalFnKey()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "TerminalFnKey"
    }
}
