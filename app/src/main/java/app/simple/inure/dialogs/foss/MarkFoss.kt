package app.simple.inure.dialogs.foss

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment

class MarkFoss : ScopedBottomSheetFragment() {

    private lateinit var editText: TypeFaceTextView
    private lateinit var save: DynamicRippleTextView
    private lateinit var close: DynamicRippleTextView

    var OnMarkFossSaved: ((String) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_mark_foss, container, false)

        editText = view.findViewById(R.id.edit_text)
        save = view.findViewById(R.id.save)
        close = view.findViewById(R.id.close)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        save.setOnClickListener {
            val license = editText.text.toString()
            if (license.isNotEmpty()) {
                OnMarkFossSaved?.invoke(license)
                dismiss()
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