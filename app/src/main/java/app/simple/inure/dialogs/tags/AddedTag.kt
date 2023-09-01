package app.simple.inure.dialogs.tags

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import app.simple.inure.R
import app.simple.inure.adapters.tags.AdapterTaggedIcons
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomHorizontalRecyclerView
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.viewmodels.panels.TagsViewModel

class AddedTag : ScopedBottomSheetFragment() {

    private lateinit var tag: TypeFaceTextView
    private lateinit var total: TypeFaceTextView
    private lateinit var recyclerView: CustomHorizontalRecyclerView
    private lateinit var close: DynamicRippleTextView

    private var tagsViewModel: TagsViewModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_batch_tag_added, container, false)

        tag = view.findViewById(R.id.tag)
        total = view.findViewById(R.id.total)
        recyclerView = view.findViewById(R.id.apps)
        close = view.findViewById(R.id.close)

        tagsViewModel = ViewModelProvider(requireActivity())[TagsViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tagsViewModel?.getTags()?.observe(viewLifecycleOwner) { tags ->
            val tag = tags.find {
                requireArguments().getString(BundleConstants.tag) == it.tag
            }

            this.tag.text = requireArguments().getString(BundleConstants.tag)
            total.text = requireContext().getString(R.string.total_apps, (tag?.packages?.split(",")?.size ?: 0).toString())

            recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            recyclerView.adapter = AdapterTaggedIcons(tag?.packages?.split(",") ?: listOf())
        }

        close.setOnClickListener {
            dismiss()
        }
    }

    companion object {
        fun newInstance(tag: String): AddedTag {
            val args = Bundle()
            val fragment = AddedTag()
            args.putString(BundleConstants.tag, tag)
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showAddedApps(tag: String): AddedTag {
            val dialog = newInstance(tag)
            dialog.show(this, "AddedTag")
            return dialog
        }
    }
}