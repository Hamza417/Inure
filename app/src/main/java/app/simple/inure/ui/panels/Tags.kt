package app.simple.inure.ui.panels

import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import app.simple.inure.R
import app.simple.inure.activities.app.MainActivity
import app.simple.inure.adapters.ui.AdapterTags
import app.simple.inure.constants.BottomMenuConstants
import app.simple.inure.constants.Misc
import app.simple.inure.constants.ShortcutConstants
import app.simple.inure.constants.Warnings
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.dialogs.tags.AutoTag
import app.simple.inure.dialogs.tags.AutoTag.Companion.showAutoTag
import app.simple.inure.dialogs.tags.TagsMenu
import app.simple.inure.dialogs.tags.TagsMenu.Companion.showTagsMenu
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.models.Tag
import app.simple.inure.popups.tags.PopupTagsMenu
import app.simple.inure.ui.subpanels.TaggedApps
import app.simple.inure.util.StatusBarHeight
import app.simple.inure.viewmodels.panels.TagsViewModel

class Tags : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private var tagsViewModel: TagsViewModel? = null

    private var spanCount = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_tags, container, false)

        recyclerView = view.findViewById(R.id.tags_recycler_view)
        tagsViewModel = ViewModelProvider(requireActivity())[TagsViewModel::class.java]

        when {
            StatusBarHeight.isLandscape(requireContext()) -> {
                spanCount = if (StatusBarHeight.isTablet(requireContext())) {
                    Misc.FOUR
                } else {
                    Misc.THREE
                }
            }
            else -> {
                spanCount = if (StatusBarHeight.isTablet(requireContext())) {
                    Misc.THREE
                } else {
                    Misc.TWO
                }
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fullVersionCheck()
        postponeEnterTransition()

        tagsViewModel?.getTags()?.observe(viewLifecycleOwner) {
            hideLoader()

            val adapter = AdapterTags(it, object : AdapterTags.Companion.TagsCallback {
                override fun onTagClicked(tag: Tag) {
                    openFragmentSlide(TaggedApps.newInstance(tag.tag), TaggedApps.TAG)
                }

                override fun onTagLongClicked(tag: Tag) {
                    PopupTagsMenu(requireView(), object : PopupTagsMenu.Companion.TagsMenuCallback {
                        override fun onOpenClicked() {
                            openFragmentSlide(TaggedApps.newInstance(tag.tag), TaggedApps.TAG)
                        }

                        override fun onDeleteClicked() {
                            onSure {
                                (recyclerView.adapter as AdapterTags).removeTag(tag)
                                tagsViewModel?.deleteTag(tag)
                            }
                        }

                        override fun onCreateShortcutClicked() {
                            val intent = Intent(requireContext(), MainActivity::class.java).apply {
                                action = ShortcutConstants.TAGGED_APPS_ACTION
                                putExtra(ShortcutConstants.TAGGED_APPS_EXTRA, tag.tag)
                                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                            }

                            val shortcut = ShortcutInfoCompat.Builder(requireContext(), tag.tag)
                                .setShortLabel(tag.tag)
                                .setActivity(ComponentName(requireContext(), MainActivity::class.java))
                                .setIcon(IconCompat.createWithResource(requireContext(), R.drawable.sc_tags))
                                .setIntent(intent)
                                .build()

                            ShortcutManagerCompat.requestPinShortcut(requireContext(), shortcut, null)
                        }
                    })
                }
            })

            if (recyclerView.layoutManager == null || recyclerView.layoutManager is LinearLayoutManager) {
                recyclerView.layoutManager = StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL).apply {
                    gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
                }
            }

            recyclerView.adapter = adapter

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }

            bottomRightCornerMenu?.initBottomMenuWithRecyclerView(BottomMenuConstants.getGenericBottomMenuItems(), recyclerView) { id, _ ->
                when (id) {
                    R.drawable.ic_settings -> {
                        childFragmentManager.showTagsMenu().setOnTagsMenuCallback(object : TagsMenu.Companion.TagsMenuCallback {
                            override fun onAutoTag() {
                                childFragmentManager.showAutoTag().setAutoTagCallback(object : AutoTag.Companion.AutoTagCallback {
                                    override fun onAutoTag(tags: Long) {
                                        if (tags != 0L) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                showLoader(manualOverride = true)
                                                tagsViewModel?.autoTag(tags)
                                            }
                                        } else {
                                            showWarning(Warnings.EMPTY_FLAGS, false)
                                        }
                                    }
                                })
                            }
                        })
                    }
                    R.drawable.ic_search -> {
                        openFragmentSlide(Search.newInstance(true), Search.TAG)
                    }
                    R.drawable.ic_refresh -> {
                        showLoader(true)
                        tagsViewModel?.refresh()
                    }
                }
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

        const val TAG = "Tags"
    }
}
