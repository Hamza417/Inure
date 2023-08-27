package app.simple.inure.dialogs.tags

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.decorations.corners.DynamicCornerEditText
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.fragments.ScopedDialogFragment
import app.simple.inure.themes.manager.ThemeManager
import app.simple.inure.util.TextViewUtils.doOnTextChanged
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible

class AddTag : ScopedDialogFragment() {

    private lateinit var count: TypeFaceTextView
    private lateinit var editText: DynamicCornerEditText
    private lateinit var close: DynamicRippleTextView
    private lateinit var add: DynamicRippleTextView

    var onTag: ((String) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_add_tag, container, false)

        count = view.findViewById(R.id.count)
        editText = view.findViewById(R.id.edit_text)
        close = view.findViewById(R.id.close)
        add = view.findViewById(R.id.add)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (editText.text?.isEmpty() == true) {
            add.gone()
            this.count.text = String.format("%d/30", 0)
            count.setTextColor(ColorStateList.valueOf(Color.RED))
        } else {
            add.visible(animate = false)
            this.count.text = String.format("%d/30", editText.text!!.length)
            if (editText.text!!.length > 2) {
                count.setTextColor(ColorStateList.valueOf(
                        ThemeManager.theme.textViewTheme.secondaryTextColor))
            } else {
                count.setTextColor(ColorStateList.valueOf(Color.RED))
            }
        }

        editText.doOnTextChanged { text, _, _, _ ->
            count.text = String.format("%d/30", text!!.length)
            if (text.isNotEmpty()) {
                if (text.length > 2) {
                    add.visible(animate = true)
                    count.setTextColor(ColorStateList.valueOf(
                            ThemeManager.theme.textViewTheme.secondaryTextColor))
                } else {
                    add.gone(animate = true)
                    count.setTextColor(ColorStateList.valueOf(Color.RED))
                }
            } else {
                add.gone(animate = false)
            }
        }

        add.setOnClickListener {
            onTag?.invoke(editText.text.toString())
            dismiss()
        }

        close.setOnClickListener {
            dismiss()
        }
    }

    companion object {
        private const val TAG = "AddTag"

        fun newInstance(): AddTag {
            val args = Bundle()
            val fragment = AddTag()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showAddTagDialog(): AddTag {
            val dialog = newInstance()
            dialog.show(this, TAG)
            return dialog
        }
    }
}