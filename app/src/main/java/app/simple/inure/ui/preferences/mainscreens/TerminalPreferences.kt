package app.simple.inure.ui.preferences.mainscreens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleRelativeLayout
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.ui.preferences.subscreens.TerminalColor
import app.simple.inure.ui.preferences.subscreens.TerminalFontSize
import app.simple.inure.util.FragmentHelper

class TerminalPreferences : ScopedFragment() {

    private lateinit var fontSize: DynamicRippleRelativeLayout
    private lateinit var color: DynamicRippleRelativeLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.preferences_terminal, container, false)

        fontSize = view.findViewById(R.id.terminal_font_size)
        color = view.findViewById(R.id.terminal_color)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        fontSize.setOnClickListener {
            clearExitTransition()
            FragmentHelper.openFragment(parentFragmentManager, TerminalFontSize.newInstance(), "font_size")
        }

        color.setOnClickListener {
            clearExitTransition()
            FragmentHelper.openFragment(parentFragmentManager, TerminalColor.newInstance(), "color")
        }
    }

    companion object {
        fun newInstance(): TerminalPreferences {
            val args = Bundle()
            val fragment = TerminalPreferences()
            fragment.arguments = args
            return fragment
        }
    }
}