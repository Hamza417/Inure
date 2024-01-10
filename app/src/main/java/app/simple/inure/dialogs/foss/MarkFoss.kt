package app.simple.inure.dialogs.foss

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.views.HintsEditText
import app.simple.inure.extensions.fragments.ScopedDialogFragment

class MarkFoss : ScopedDialogFragment() {

    private lateinit var editText: HintsEditText
    private lateinit var save: DynamicRippleTextView
    private lateinit var close: DynamicRippleTextView

    private var licenses: Array<String>? = null

    var onMarkFossSaved: ((String) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_mark_foss, container, false)

        editText = view.findViewById(R.id.edit_text)
        save = view.findViewById(R.id.save)
        close = view.findViewById(R.id.close)

        licenses = resources.getStringArray(R.array.osi_approved_licenses)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editText.doOnTextChanged { text, _, _, _ ->
            if (text.toString().isNotEmpty()) {
                if (licenses?.any { it.startsWith(text.toString(), true) } == true) {
                    val remaining = licenses?.first {
                        it.startsWith(text.toString(), true)
                    }?.substring(text.toString().length)

                    editText.drawHint(text.toString(), remaining)
                    editText.finalVerificationHint = licenses?.first { it.startsWith(text.toString(), true) } ?: ""
                } else {
                    editText.clearHint()
                }
            } else {
                editText.clearHint()
            }
        }

        save.setOnClickListener {
            if (editText.text.toString().isNotEmpty()) {
                val license = licenses?.firstOrNull { it.startsWith(editText.text.toString(), true) } ?: ""
                if (license.isNotEmpty()) {
                    onMarkFossSaved?.invoke(license)
                    dismiss()
                } else {
                    onMarkFossSaved?.invoke(editText.text.toString())
                    dismiss()
                }
            } else {
                editText.error = "Please enter a valid license"
            }
        }

        close.setOnClickListener {
            dismiss()
        }
    }

    companion object {
        fun newInstance(): MarkFoss {
            val args = Bundle()
            val fragment = MarkFoss()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showMarkFossDialog(): MarkFoss {
            val dialog = newInstance()
            dialog.show(this, "mark_foss_dialog")
            return dialog
        }
    }
}