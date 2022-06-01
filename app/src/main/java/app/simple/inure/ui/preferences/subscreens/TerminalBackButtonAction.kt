package app.simple.inure.ui.preferences.subscreens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.adapters.terminal.AdapterBackButtonAction
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.extensions.fragments.ScopedFragment

class TerminalBackButtonAction : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.terminal_font_size, container, false)

        recyclerView = view.findViewById(R.id.font_size_recycler_view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        recyclerView.adapter = AdapterBackButtonAction()
    }

    companion object {
        fun newInstance(): TerminalBackButtonAction {
            val args = Bundle()
            val fragment = TerminalBackButtonAction()
            fragment.arguments = args
            return fragment
        }
    }
}