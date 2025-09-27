package app.simple.inure.dialogs.tags

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.viewers.AdapterTags
import app.simple.inure.decorations.corners.DynamicCornerEditText
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.TagsRecyclerView
import app.simple.inure.extensions.fragments.ScopedDialogFragment
import app.simple.inure.themes.manager.ThemeManager
import app.simple.inure.util.TextViewUtils.doOnTextChanged
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.panels.TagsViewModel

class AddTag : ScopedDialogFragment() {

    private lateinit var count: TypeFaceTextView
    private lateinit var editText: DynamicCornerEditText
    private lateinit var existingTags: TagsRecyclerView
    private lateinit var close: DynamicRippleTextView
    private lateinit var add: DynamicRippleTextView

    private var tagsViewModel: TagsViewModel? = null
    private var adapterTags: AdapterTags? = null
    private var inputFilter: InputFilter? = null

    var onTag: ((String) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_add_tag, container, false)

        count = view.findViewById(R.id.count)
        editText = view.findViewById(R.id.edit_text)
        existingTags = view.findViewById(R.id.existing_tags)
        close = view.findViewById(R.id.close)
        add = view.findViewById(R.id.add)

        tagsViewModel = ViewModelProvider(requireActivity())[TagsViewModel::class.java]

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
            add.gone()
            this.count.text = String.format("%d/${resources.getInteger(R.integer.tag_character_limit)}", 0)
            count.setTextColor(ColorStateList.valueOf(Color.RED))
        } else {
            add.visible(animate = false)
            this.count.text = String.format("%d/${resources.getInteger(R.integer.tag_character_limit)}", editText.text!!.length)
            if (editText.text!!.length > 2) {
                count.setTextColor(ColorStateList.valueOf(
                        ThemeManager.theme.textViewTheme.secondaryTextColor))
            } else {
                count.setTextColor(ColorStateList.valueOf(Color.RED))
            }
        }

        tagsViewModel!!.getTagNames().observe(viewLifecycleOwner) {
            existingTags.visible(animate = false)
            adapterTags = AdapterTags(it, false).apply {
                setOnTagCallbackListener(object : AdapterTags.Companion.TagsCallback {
                    override fun onTagClicked(tag: String) {
                        if (tag.isNotEmpty()) {
                            editText.setText(tag)
                            editText.setSelection(tag.length)
                        }
                    }

                    override fun onTagLongClicked(tag: String) {
                        //                        PopupTagMenu(requireView(), object : PopupTagMenu.Companion.TagsMenuCallback {
                        //                            override fun onDeleteClicked() {
                        //                                tagsViewModel?.removeTag(tag, packageInfo) {
                        //                                    this@apply.removeTag(tag)
                        //                                }
                        //                            }
                        //                        })
                    }

                    override fun onAddClicked() {
                        editText.text!!.clear()
                        editText.showInput()
                    }
                })
            }

            existingTags.adapter = adapterTags
        }

        editText.doOnTextChanged { text, _, _, _ ->
            count.text = String.format("%d/${resources.getInteger(R.integer.tag_character_limit)}", text!!.length)
            if (text.isNotEmpty()) {
                if (text.length > 2) {
                    adapterTags?.highlightedTag = text.toString()
                    add.visible(animate = true)
                    count.setTextColor(ColorStateList.valueOf(
                            ThemeManager.theme.textViewTheme.secondaryTextColor))
                } else {
                    add.gone(animate = true)
                    count.setTextColor(ColorStateList.valueOf(Color.RED))
                }
            } else {
                add.gone(animate = false)
                count.setTextColor(ColorStateList.valueOf(Color.RED))
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
            val fragment = newInstance()
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
