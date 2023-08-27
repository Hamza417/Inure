package app.simple.inure.ui.panels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.ui.AdapterTags
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.viewmodels.panels.TagsViewModel

class Tags : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView

    private var tagsViewModel: TagsViewModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_tags, container, false)

        recyclerView = view.findViewById(R.id.tags_recycler_view)

        tagsViewModel = ViewModelProvider(requireActivity())[TagsViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fullVersionCheck()
        postponeEnterTransition()

        tagsViewModel?.getTags()?.observe(viewLifecycleOwner) {
            val adapter = AdapterTags(it)
            recyclerView.adapter = adapter

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }
        }
    }

    companion object {
        fun newInstance(): Tags {
            val args = Bundle()
            val fragment = Tags()
            fragment.arguments = args
            return fragment
        }
    }
}