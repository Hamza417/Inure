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
import app.simple.inure.adapters.details.AdapterGraphics
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.extensions.fragments.SearchBarScopedFragment
import app.simple.inure.factories.panels.PackageInfoFactory
import app.simple.inure.popups.viewers.PopupGraphicsMenu
import app.simple.inure.preferences.DevelopmentPreferences
import app.simple.inure.preferences.GraphicsPreferences
import app.simple.inure.util.NullSafety.isNull
import app.simple.inure.viewmodels.viewers.GraphicsViewModel

class Graphics : SearchBarScopedFragment() {

    private lateinit var options: DynamicRippleImageButton
    private lateinit var recyclerView: CustomVerticalRecyclerView
    private var adapterGraphics: AdapterGraphics? = null
    private lateinit var graphicsViewModel: GraphicsViewModel
    private lateinit var packageInfoFactory: PackageInfoFactory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_graphics, container, false)

        options = view.findViewById(R.id.graphics_options)
        recyclerView = view.findViewById(R.id.graphics_recycler_view)
        search = view.findViewById(R.id.graphics_search_btn)
        searchBox = view.findViewById(R.id.graphics_search)
        title = view.findViewById(R.id.graphics_title)

        packageInfoFactory = PackageInfoFactory(packageInfo)
        graphicsViewModel = ViewModelProvider(this, packageInfoFactory)[GraphicsViewModel::class.java]

        searchBoxState(animate = false, GraphicsPreferences.isSearchVisible())
        startPostponedEnterTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        graphicsViewModel.getGraphics().observe(viewLifecycleOwner) {
            if (recyclerView.adapter.isNull()) {
                adapterGraphics = AdapterGraphics(packageInfo.applicationInfo.sourceDir, it, searchBox.text.toString().trim())
                recyclerView.adapter = adapterGraphics

                adapterGraphics!!.setOnResourceClickListener(object : AdapterGraphics.GraphicsCallbacks {
                    override fun onGraphicsClicked(path: String, filePath: String, view: ViewGroup, xOff: Float, yOff: Float) {
                        openFragmentSlide(ImageViewer.newInstance(packageInfo.applicationInfo.sourceDir, filePath), "image_viewer")
                    }

                    override fun onGraphicsLongPressed(filePath: String) {
                        if (DevelopmentPreferences.get(DevelopmentPreferences.isWebViewXmlViewer)) {
                            openFragmentSlide(XMLViewerWebView.newInstance(packageInfo, false, filePath), "wv_xml")
                        } else {
                            openFragmentSlide(XMLViewerTextView.newInstance(packageInfo, false, filePath), "tv_xml")
                        }
                    }
                })
            } else {
                adapterGraphics?.updateData(it, keyword = searchBox.text.toString())
            }

            searchBox.doOnTextChanged { text, _, _, _ ->
                if (searchBox.isFocused) {
                    graphicsViewModel.keyword = text.toString().trim()
                }
            }
        }

        graphicsViewModel.getError().observe(viewLifecycleOwner) {
            showError(it)
        }

        graphicsViewModel.notFound.observe(viewLifecycleOwner) {
            showWarning(R.string.no_graphics_found)
        }

        options.setOnClickListener {
            PopupGraphicsMenu(it)
        }

        search.setOnClickListener {
            if (searchBox.text.isNullOrEmpty()) {
                GraphicsPreferences.setSearchVisibility(!GraphicsPreferences.isSearchVisible())
            } else {
                searchBox.text?.clear()
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            GraphicsPreferences.graphicsSearch -> {
                searchBoxState(animate = true, GraphicsPreferences.isSearchVisible())
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        adapterGraphics?.unregister()
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo): Graphics {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            val fragment = Graphics()
            fragment.arguments = args
            return fragment
        }
    }
}