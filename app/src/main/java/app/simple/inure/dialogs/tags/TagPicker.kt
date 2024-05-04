package app.simple.inure.dialogs.tags

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.viewers.AdapterTags
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.views.TagsRecyclerView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.models.Tag
import app.simple.inure.util.ArrayUtils.toArrayList
import app.simple.inure.viewmodels.panels.TagsViewModel

class TagPicker : ScopedBottomSheetFragment() {

    private lateinit var tagsRecyclerView: TagsRecyclerView
    private lateinit var close: DynamicRippleTextView

    private lateinit var tagsViewModel: TagsViewModel
    private lateinit var adapterTags: AdapterTags

    private var tagPickerCallbacks: TagPickerCallbacks? = null
    private var tags: ArrayList<Tag>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_tag_picker, container, false)

        tagsRecyclerView = view.findViewById(R.id.tags)
        close = view.findViewById(R.id.close)

        tagsViewModel = ViewModelProvider(requireActivity())[TagsViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tagsViewModel.getTags().observe(viewLifecycleOwner) { tags ->
            this.tags = tags
            val names = tags.map { it.tag }.toArrayList()

            adapterTags = AdapterTags(names, false)
            tagsRecyclerView.adapter = adapterTags

            adapterTags.setOnTagCallbackListener(object : AdapterTags.Companion.TagsCallback {
                override fun onTagClicked(tag: String) {
                    tags.find { it.tag == tag }?.let {
                        tagPickerCallbacks?.onTagPicked(it).also {
                            dismiss()
                        }
                    }
                }

                override fun onTagLongClicked(tag: String) {
                    /* no-op */
                }

                override fun onAddClicked() {
                    /* no-op */
                }
            })
        }
    }

    fun setTagPickerCallbacks(tagPickerCallbacks: TagPickerCallbacks) {
        this.tagPickerCallbacks = tagPickerCallbacks
    }

    companion object {
        fun newInstance(): TagPicker {
            val args = Bundle()
            val fragment = TagPicker()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showTagPicker(): TagPicker {
            val dialog = newInstance()
            dialog.show(this, TAG)
            return dialog
        }

        interface TagPickerCallbacks {
            fun onTagPicked(tag: Tag)
        }

        private const val TAG = "TagPicker"
    }
}
