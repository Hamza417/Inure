package app.simple.inure.dialogs.tags

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.adapters.viewers.AdapterTags
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.corners.DynamicCornerEditText
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.fragments.ScopedDialogFragment
import app.simple.inure.models.Tag
import app.simple.inure.themes.manager.ThemeManager
import app.simple.inure.util.ParcelUtils.parcelable
import app.simple.inure.util.TextViewUtils.doOnTextChanged
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible

class EditTag : ScopedDialogFragment() {

    private lateinit var count: TypeFaceTextView
    private lateinit var editText: DynamicCornerEditText
    private lateinit var cancel: DynamicRippleTextView
    private lateinit var update: DynamicRippleTextView

    private var tag: Tag? = null

    private var adapterTags: AdapterTags? = null
    private var inputFilter: InputFilter? = null

    var onTag: ((Tag) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_edit_tag, container, false)

        count = view.findViewById(R.id.count)
        editText = view.findViewById(R.id.edit_text)
        cancel = view.findViewById(R.id.cancel)
        update = view.findViewById(R.id.update)

        tag = requireArguments().parcelable(BundleConstants.TAG)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        inputFilter = InputFilter { source, _, _, _, _, _ ->
            val string = source.toString()
            if (string.trim().matches(Regex("[\\p{L}\\d_.-]+"))) {
                source.trim()
            } else {
                ""
            }
        }

        editText.filters = arrayOf(inputFilter)

        if (editText.text?.isEmpty() == true) {
            update.gone()
            this.count.text = String.format("%d/${resources.getInteger(R.integer.tag_character_limit)}", 0)
            count.setTextColor(ColorStateList.valueOf(Color.RED))
        } else {
            update.visible(animate = false)
            this.count.text = String.format("%d/${resources.getInteger(R.integer.tag_character_limit)}", editText.text!!.length)
            if (editText.text!!.length > 2) {
                count.setTextColor(ColorStateList.valueOf(
                        ThemeManager.theme.textViewTheme.secondaryTextColor))
            } else {
                count.setTextColor(ColorStateList.valueOf(Color.RED))
            }
        }

        editText.doOnTextChanged { text, _, _, _ ->
            count.text = String.format("%d/${resources.getInteger(R.integer.tag_character_limit)}", text!!.length)
            if (text.isNotEmpty()) {
                if (text.length > 2) {
                    adapterTags?.highlightedTag = text.toString()
                    update.visible(animate = true)
                    count.setTextColor(ColorStateList.valueOf(
                            ThemeManager.theme.textViewTheme.secondaryTextColor))
                } else {
                    update.gone(animate = true)
                    count.setTextColor(ColorStateList.valueOf(Color.RED))
                }
            } else {
                update.gone(animate = false)
                count.setTextColor(ColorStateList.valueOf(Color.RED))
            }
        }

        update.setOnClickListener {
            tag?.tag = editText.text.toString()
            onTag?.invoke(tag!!)
            dismiss()
        }

        cancel.setOnClickListener {
            dismiss()
        }
    }

    companion object {
        private const val TAG = "EditTag"

        fun newInstance(tag: Tag): EditTag {
            val args = Bundle()
            args.putParcelable(BundleConstants.TAG, tag)
            val fragment = EditTag()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showEditTagDialog(tag: Tag): EditTag {
            val fragment = newInstance(tag)
            try {
                fragment.show(this, TAG)
            } catch (e: IllegalStateException) {
                e.printStackTrace()
                val transaction = beginTransaction()
                transaction.setReorderingAllowed(true)
                transaction.add(fragment, TAG)
                transaction.commitAllowingStateLoss()
            }

            return fragment
        }
    }
}
