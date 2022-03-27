package app.simple.inure.dialogs.menus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.switchview.SwitchView
import app.simple.inure.extension.fragments.ScopedBottomSheetFragment
import app.simple.inure.preferences.NotesPreferences

class NotesMenu : ScopedBottomSheetFragment() {

    private lateinit var expandedNotes: SwitchView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_notes_menu, container, false)

        expandedNotes = view.findViewById(R.id.expanded_notes)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        expandedNotes.setChecked(NotesPreferences.areNotesExpanded())

        expandedNotes.setOnSwitchCheckedChangeListener {
            NotesPreferences.setExpandedNotes(it)
        }
    }

    companion object {
        fun newInstance(): NotesMenu {
            val args = Bundle()
            val fragment = NotesMenu()
            fragment.arguments = args
            return fragment
        }
    }
}