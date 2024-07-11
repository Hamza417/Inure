package app.simple.inure.ui.subpanels

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.adapters.tags.AdapterTaggedApps
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.CustomProgressBar
import app.simple.inure.dialogs.app.AppMenu
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.factories.subpanels.TaggedAppsViewModelFactory
import app.simple.inure.interfaces.adapters.AdapterCallbacks
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.viewmodels.panels.TagsViewModel
import app.simple.inure.viewmodels.subviewers.TagsListViewModel

class TaggedApps : ScopedFragment() {

    private lateinit var back: DynamicRippleImageButton
    private lateinit var title: TypeFaceTextView
    private lateinit var count: TypeFaceTextView
    private lateinit var loader: CustomProgressBar
    private lateinit var recyclerView: CustomVerticalRecyclerView

    private lateinit var tagsListViewModel: TagsListViewModel
    private lateinit var tagsViewModel: TagsViewModel

    private fun getCurrentTag(): String {
        return requireArguments().getString(BundleConstants.tag)!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_tagged_apps, container, false)

        back = view.findViewById(R.id.back_button)
        title = view.findViewById(R.id.tag)
        count = view.findViewById(R.id.count)
        loader = view.findViewById(R.id.loader)
        recyclerView = view.findViewById(R.id.recycler_view)

        val taggedAppsViewModelFactory = TaggedAppsViewModelFactory(requireArguments().getString(BundleConstants.tag)!!)
        tagsListViewModel = ViewModelProvider(this, taggedAppsViewModelFactory)[TagsListViewModel::class.java]
        tagsViewModel = ViewModelProvider(requireActivity())[TagsViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title.text = requireArguments().getString(BundleConstants.tag)!!

        if (tagsListViewModel.getTaggedApps().value != null) {
            postponeEnterTransition()
        } else {
            startPostponedEnterTransition()
        }

        tagsListViewModel.getTaggedApps().observe(viewLifecycleOwner) {
            count.text = getString(R.string.total_apps, it.size)
            loader.gone(animate = true)

            with(AdapterTaggedApps(it)) {
                recyclerView.adapter = this
                setOnAdapterCallbacks(object : AdapterCallbacks {

                    override fun onAppClicked(packageInfo: PackageInfo, icon: ImageView) {
                        openAppInfo(packageInfo, icon)
                    }

                    override fun onAppLongPressed(packageInfo: PackageInfo, icon: ImageView) {
                        AppMenu.newInstance(packageInfo)
                            .show(childFragmentManager, AppMenu.TAG)
                    }
                })
            }

            ItemTouchHelper(simpleItemTouchCallback).attachToRecyclerView(recyclerView)

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }
        }

        back.setOnClickListener {
            popBackStack()
        }
    }

    private val simpleItemTouchCallback: ItemTouchHelper.SimpleCallback = object
        : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
            // Remove swiped item from list and notify the RecyclerView
            val position = viewHolder.bindingAdapterPosition
            val packageName = (recyclerView.adapter as AdapterTaggedApps).getPackageInfo(position).packageName
            (recyclerView.adapter as AdapterTaggedApps).removeItem(position)

            tagsListViewModel.deleteTaggedApp(requireArguments().getString(BundleConstants.tag)!!, packageName) {
                tagsViewModel.refresh()
            }
        }
    }

    companion object {
        fun newInstance(tag: String): TaggedApps {
            val args = Bundle()
            args.putString(BundleConstants.tag, tag)
            val fragment = TaggedApps()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "tagged_apps"
    }
}
