package app.simple.inure.dialogs.notes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.interfaces.fragments.SureCallbacks

class NotesNotSavedWarning : ScopedBottomSheetFragment() {

    private lateinit var yes: DynamicRippleTextView
    private lateinit var cancel: DynamicRippleTextView

    private var sureCallbacks: SureCallbacks? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_note_not_saved, container, false)

        yes = view.findViewById(R.id.yes)
        cancel = view.findViewById(R.id.cancel)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        yes.setOnClickListener {
            sureCallbacks?.onSure()
            dismiss()
        }

        cancel.setOnClickListener {
            dismiss()
        }
    }

    fun setSureCallbacks(sureCallbacks: SureCallbacks) {
        this.sureCallbacks = sureCallbacks
    }

    companion object {
        fun newInstance(): NotesNotSavedWarning {
            val args = Bundle()
            val fragment = NotesNotSavedWarning()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showNoteNotSavedWarning(): NotesNotSavedWarning {
            val dialog = newInstance()
            dialog.show(this, "notes_not_saved_warning")
            return dialog
        }
    }
}