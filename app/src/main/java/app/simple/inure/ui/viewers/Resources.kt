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
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceEditTextDynamicCorner
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.dialogs.miscellaneous.Error
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.factories.panels.PackageInfoFactory
import app.simple.inure.preferences.DevelopmentPreferences
import app.simple.inure.preferences.ResourcesPreferences
import app.simple.inure.util.FragmentHelper
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.viewers.ApkDataViewModel

class Resources : ScopedFragment() {

    private lateinit var options: DynamicRippleImageButton
    private lateinit var search: DynamicRippleImageButton
    private lateinit var title: TypeFaceTextView
    private lateinit var searchBox: TypeFaceEditTextDynamicCorner
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
        packageInfo = requireArguments().getParcelable("application_info")!!
        packageInfoFactory = PackageInfoFactory(requireActivity().application, packageInfo)
        componentsViewModel = ViewModelProvider(this, packageInfoFactory).get(ApkDataViewModel::class.java)

        searchBoxState()
        startPostponedEnterTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        componentsViewModel.getResources().observe(viewLifecycleOwner) {
            val adapterResources = AdapterResources(it, searchBox.text.toString())

            recyclerView.adapter = adapterResources

            adapterResources.setOnResourceClickListener(object : AdapterResources.ResourceCallbacks {
                override fun onResourceClicked(path: String) {
                    clearExitTransition()

                    if (DevelopmentPreferences.isWebViewXmlViewer()) {
                        FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                    XMLViewerWebView.newInstance(packageInfo, false, path),
                                                    "wv_xml")
                    } else {
                        FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                    XMLViewerTextView.newInstance(packageInfo, false, path),
                                                    "tv_xml")
                    }
                }

                override fun onResourceLongClicked(path: String) {
                    clearExitTransition()

                    FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                TextViewer.newInstance(packageInfo, path),
                                                "txt_tv_xml")
                }
            })
        }

        componentsViewModel.getError().observe(viewLifecycleOwner, {
            val e = Error.newInstance(it)
            e.show(childFragmentManager, "error_dialog")
            e.setOnErrorDialogCallbackListener(object : Error.Companion.ErrorDialogCallbacks {
                override fun onDismiss() {
                    requireActivity().onBackPressed()
                }
            })
        })

        options.setOnClickListener {

        }

        search.setOnClickListener {
            if (searchBox.text.isNullOrEmpty()) {
                ResourcesPreferences.setSearchVisibility(!ResourcesPreferences.isSearchVisible())
            } else {
                searchBox.text?.clear()
            }
        }

        searchBox.doOnTextChanged { text, _, _, _ ->
            if (searchBox.isFocused) {
                componentsViewModel.getResourceData(text.toString())
            }
        }
    }

    private fun searchBoxState() {
        if (ResourcesPreferences.isSearchVisible()) {
            search.setImageResource(R.drawable.ic_close)
            title.gone()
            searchBox.visible(true)
            searchBox.showInput()
        } else {
            search.setImageResource(R.drawable.ic_search)
            title.visible(true)
            searchBox.gone()
            searchBox.hideInput()
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            ResourcesPreferences.resourcesSearch -> {
                searchBoxState()
            }
        }
    }

    companion object {
        fun newInstance(applicationInfo: PackageInfo): Resources {
            val args = Bundle()
            args.putParcelable("application_info", applicationInfo)
            val fragment = Resources()
            fragment.arguments = args
            return fragment
        }
    }
}