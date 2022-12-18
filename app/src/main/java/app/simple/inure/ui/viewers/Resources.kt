package app.simple.inure.ui.viewers

import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.details.AdapterResources
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.extensions.fragments.SearchBarScopedFragment
import app.simple.inure.factories.panels.PackageInfoFactory
import app.simple.inure.preferences.DevelopmentPreferences
import app.simple.inure.preferences.ResourcesPreferences
import app.simple.inure.viewmodels.viewers.ApkDataViewModel

class Resources : SearchBarScopedFragment() {

    private lateinit var options: DynamicRippleImageButton
    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var componentsViewModel: ApkDataViewModel
    private lateinit var packageInfoFactory: PackageInfoFactory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_resources, container, false)

        options = view.findViewById(R.id.resources_option_btn)
        search = view.findViewById(R.id.resources_search_btn)
        searchBox = view.findViewById(R.id.resources_search)
        title = view.findViewById(R.id.resources_title)
        recyclerView = view.findViewById(R.id.resources_recycler_view)

        packageInfoFactory = PackageInfoFactory(packageInfo)
        componentsViewModel = ViewModelProvider(this, packageInfoFactory)[ApkDataViewModel::class.java]

        searchBoxState(animate = false, ResourcesPreferences.isSearchVisible())
        startPostponedEnterTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        componentsViewModel.getResources().observe(viewLifecycleOwner) {
            val adapterResources = AdapterResources(it, searchBox.text.toString().trim())

            recyclerView.adapter = adapterResources

            adapterResources.setOnResourceClickListener(object : AdapterResources.ResourceCallbacks {
                override fun onResourceClicked(path: String) {
                    if (DevelopmentPreferences.get(DevelopmentPreferences.isWebViewXmlViewer)) {
                        openFragmentSlide(XMLViewerWebView.newInstance(packageInfo, false, path), "wv_xml")
                    } else {
                        openFragmentSlide(XMLViewerTextView.newInstance(packageInfo, false, path), "tv_xml")
                    }
                }

                override fun onResourceLongClicked(path: String) {
                    openFragmentSlide(Text.newInstance(packageInfo, path), "txt_tv_xml")
                }
            })

            searchBox.doOnTextChanged { text, _, _, _ ->
                if (searchBox.isFocused) {
                    componentsViewModel.getResourceData(text.toString().trim())
                }
            }
        }

        componentsViewModel.getError().observe(viewLifecycleOwner) {
            showError(it)
        }

        componentsViewModel.notFound.observe(viewLifecycleOwner) {
            showWarning(R.string.no_resource_found)
        }

        options.setOnClickListener {

        }

        search.setOnClickListener {
            if (searchBox.text.isNullOrEmpty()) {
                ResourcesPreferences.setSearchVisibility(!ResourcesPreferences.isSearchVisible())
            } else {
                searchBox.text?.clear()
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            ResourcesPreferences.resourcesSearch -> {
                searchBoxState(animate = true, ResourcesPreferences.isSearchVisible())
            }
        }
    }

    companion object {
        fun newInstance(applicationInfo: PackageInfo): Resources {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, applicationInfo)
            val fragment = Resources()
            fragment.arguments = args
            return fragment
        }
    }
}