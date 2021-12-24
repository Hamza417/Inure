package app.simple.inure.ui.preferences.mainscreens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.switchview.SwitchView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.preferences.AccessibilityPreferences

class AccessibilityScreen : ScopedFragment() {

    private lateinit var highlight: SwitchView
    private lateinit var divider: SwitchView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_accessibility, container, false)

        highlight = view.findViewById(R.id.highlight_switch)
        divider = view.findViewById(R.id.list_divider_switch)

        startPostponedEnterTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        highlight.setChecked(AccessibilityPreferences.isHighlightMode())
        divider.setChecked(AccessibilityPreferences.isDividerEnabled())

        highlight.setOnSwitchCheckedChangeListener {
            AccessibilityPreferences.setHighlightMode(it)
        }

        divider.setOnSwitchCheckedChangeListener {
            AccessibilityPreferences.setDivider(it)
        }
    }

    companion object {
        fun newInstance(): AccessibilityScreen {
            val args = Bundle()
            val fragment = AccessibilityScreen()
            fragment.arguments = args
            return fragment
        }
    }
}