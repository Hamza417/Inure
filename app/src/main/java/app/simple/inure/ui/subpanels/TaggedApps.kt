package app.simple.inure.ui.subpanels

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.analytics.AnalyticsDataAdapter
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.CustomProgressBar
import app.simple.inure.dialogs.menus.AppsMenu
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.factories.subpanels.TaggedAppsViewModelFactory
import app.simple.inure.interfaces.adapters.AdapterCallbacks
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.viewmodels.subviewers.TagsListViewModel

class TaggedApps : ScopedFragment() {

    private lateinit var back: DynamicRippleImageButton
    private lateinit var title: TypeFaceTextView
    private lateinit var loader: CustomProgressBar
    private lateinit var recyclerView: CustomVerticalRecyclerView

    private lateinit var taggedAppsViewModel: TagsListViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_tagged_apps, container, false)

        back = view.findViewById(R.id.back_button)
        title = view.findViewById(R.id.tag)
        loader = view.findViewById(R.id.loader)
        recyclerView = view.findViewById(R.id.recycler_view)

        val taggedAppsViewModelFactory = TaggedAppsViewModelFactory(requireArguments().getString(BundleConstants.tag)!!)
        taggedAppsViewModel = ViewModelProvider(this, taggedAppsViewModelFactory)[TagsListViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title.text = requireArguments().getString(BundleConstants.tag)!!

        if (taggedAppsViewModel.getTaggedApps().value != null) {
            postponeEnterTransition()
        } else {
            startPostponedEnterTransition()
        }

        taggedAppsViewModel.getTaggedApps().observe(viewLifecycleOwner) {
            loader.gone(animate = true)
            val taggedAppsAdapter = AnalyticsDataAdapter(it)

            taggedAppsAdapter.setOnAdapterCallbacks(object : AdapterCallbacks {
                override fun onAppClicked(packageInfo: PackageInfo, icon: ImageView) {
                    openAppInfo(packageInfo, icon)
                }

                override fun onAppLongPressed(packageInfo: PackageInfo, icon: ImageView) {
                    AppsMenu.newInstance(packageInfo)
                        .show(childFragmentManager, "apps_menu")
                }
            })

            recyclerView.adapter = taggedAppsAdapter

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }
        }

        back.setOnClickListener {
            popBackStack()
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
    }
}