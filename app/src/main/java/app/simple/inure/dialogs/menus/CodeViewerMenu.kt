package app.simple.inure.dialogs.menus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.switchview.SwitchView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.preferences.FormattingPreferences

class CodeViewerMenu : ScopedBottomSheetFragment() {

    private lateinit var showNumberOnEveryLine: SwitchView
    private lateinit var openSettings: DynamicRippleTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_menu_code_viewer, container, false)

        showNumberOnEveryLine = view.findViewById(R.id.line_number_on_every_line)
        openSettings = view.findViewById(R.id.dialog_open_apps_settings)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showNumberOnEveryLine.setChecked(FormattingPreferences.isCountingAllLines())

        showNumberOnEveryLine.setOnSwitchCheckedChangeListener {
            FormattingPreferences.setCountAllLines(it)
        }

        openSettings.setOnClickListener {
            openSettings()
        }
    }

    companion object {
        fun newInstance(): CodeViewerMenu {
            val args = Bundle()
            val fragment = CodeViewerMenu()
            fragment.arguments = args
            return fragment
        }
    }
}