package app.simple.inure.dialogs.notes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.switchview.SwitchView
import app.simple.inure.extension.fragments.ScopedBottomSheetFragment
import app.simple.inure.preferences.NotesPreferences

class NotesEditorMenu : ScopedBottomSheetFragment() {

    private lateinit var htmlSpans: SwitchView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_notes_editor_settings, container, false)

        htmlSpans = view.findViewById(R.id.html_spans)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        htmlSpans.setChecked(NotesPreferences.areHTMLSpans())

        htmlSpans.setOnSwitchCheckedChangeListener {
            NotesPreferences.setHTMLSpans(it)
        }
    }

    companion object {
        fun newInstance(): NotesEditorMenu {
            val args = Bundle()
            val fragment = NotesEditorMenu()
            fragment.arguments = args
            return fragment
        }
    }
}