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
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceEditTextSearch
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.CustomVerticalRecyclerView
import app.simple.inure.dialogs.miscellaneous.ErrorPopup
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.popups.viewers.PopupGraphicsMenu
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.preferences.GraphicsPreferences
import app.simple.inure.util.FragmentHelper
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.factory.PackageInfoFactory
import app.simple.inure.viewmodels.viewers.GraphicsViewModel

class Graphics : ScopedFragment() {

    private lateinit var options: DynamicRippleImageButton
    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var search: DynamicRippleImageButton
    private lateinit var title: TypeFaceTextView
    private lateinit var searchBox: TypeFaceEditTextSearch
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
        packageInfo = requireArguments().getParcelable("application_info")!!
        packageInfoFactory = PackageInfoFactory(requireActivity().application, packageInfo)
        graphicsViewModel = ViewModelProvider(this, packageInfoFactory).get(GraphicsViewModel::class.java)

        searchBoxState()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startPostponedEnterTransition()

        graphicsViewModel.getGraphics().observe(viewLifecycleOwner, {
            adapterGraphics = AdapterGraphics(packageInfo.applicationInfo.sourceDir, it, searchBox.text.toString())
            recyclerView.adapter = adapterGraphics

            adapterGraphics!!.setOnResourceClickListener(object : AdapterGraphics.GraphicsCallbacks {
                override fun onGraphicsClicked(path: String, filePath: String, view: ViewGroup, xOff: Float, yOff: Float) {
                    FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                ImageViewer.newInstance(packageInfo.applicationInfo.sourceDir, filePath),
                                                "image_viewer")
                }

                override fun onGraphicsLongPressed(filePath: String) {
                    if (ConfigurationPreferences.isXmlViewerTextView()) {
                        FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                    XMLViewerTextView.newInstance(packageInfo, false, filePath),
                                                    "tv_xml")
                    } else {
                        FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                    XMLViewerWebView.newInstance(packageInfo, false, filePath),
                                                    "wv_xml")
                    }
                }
            })
        })

        graphicsViewModel.getError().observe(viewLifecycleOwner, {
            with(ErrorPopup.newInstance(it)) {
                setOnErrorDialogCallbackListener(object : ErrorPopup.Companion.ErrorDialogCallbacks {
                    override fun onDismiss() {
                        requireActivity().onBackPressed()
                    }
                })

                show(childFragmentManager, "error_dialog")
            }
        })

        options.setOnClickListener {
            PopupGraphicsMenu(it)
        }

        search.setOnClickListener {
            GraphicsPreferences.setSearchVisibility(!GraphicsPreferences.isSearchVisible())
        }

        searchBox.doOnTextChanged { text, _, _, _ ->
            if (searchBox.isFocused) {
                graphicsViewModel.getGraphicsData(text.toString())
            }
        }
    }

    private fun searchBoxState() {
        if (GraphicsPreferences.isSearchVisible()) {
            search.setImageResource(R.drawable.ic_close)
            title.gone()
            searchBox.visible(true)
        } else {
            search.setImageResource(R.drawable.ic_search)
            title.visible(true)
            searchBox.gone()
        }

        searchBox.toggleInput()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            GraphicsPreferences.graphicsSearch -> {
                searchBoxState()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        adapterGraphics?.unregister()
    }

    companion object {
        fun newInstance(applicationInfo: PackageInfo): Graphics {
            val args = Bundle()
            args.putParcelable("application_info", applicationInfo)
            val fragment = Graphics()
            fragment.arguments = args
            return fragment
        }
    }
}