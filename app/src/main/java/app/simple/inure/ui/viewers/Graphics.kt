package app.simple.inure.ui.viewers

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.details.AdapterGraphics
import app.simple.inure.decorations.popup.PopupFrameLayout
import app.simple.inure.decorations.views.CustomRecyclerView
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.popups.viewers.PopupImageViewer
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.util.APKParser
import app.simple.inure.util.FragmentHelper
import app.simple.inure.viewmodels.factory.ApplicationInfoFactory
import app.simple.inure.viewmodels.viewers.ApkDataViewModel

class Graphics : ScopedFragment() {

    private lateinit var recyclerView: CustomRecyclerView
    private lateinit var total: TypeFaceTextView
    private lateinit var componentsViewModel: ApkDataViewModel
    private lateinit var applicationInfoFactory: ApplicationInfoFactory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_graphics, container, false)

        recyclerView = view.findViewById(R.id.graphics_recycler_view)
        total = view.findViewById(R.id.total)
        applicationInfo = requireArguments().getParcelable("application_info")!!
        applicationInfoFactory = ApplicationInfoFactory(requireActivity().application, applicationInfo)
        componentsViewModel = ViewModelProvider(this, applicationInfoFactory).get(ApkDataViewModel::class.java)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startPostponedEnterTransition()

        componentsViewModel.getGraphics().observe(viewLifecycleOwner, {
            val adapterGraphics = AdapterGraphics(applicationInfo.sourceDir, APKParser.getGraphicsFiles(applicationInfo.sourceDir))

            recyclerView.adapter = adapterGraphics
            total.text = getString(R.string.total, adapterGraphics.list.size)

            adapterGraphics.setOnResourceClickListener(object : AdapterGraphics.GraphicsCallbacks {
                override fun onGraphicsClicked(path: String, filePath: String, view: ViewGroup, xOff: Float, yOff: Float) {

                    val popupFrameLayout = PopupFrameLayout(requireContext())

                    popupFrameLayout.apply {
                        minimumWidth = resources.getDimensionPixelSize(R.dimen.popup_image_viewer_dimension)
                        minimumHeight = minimumWidth
                    }

                    PopupImageViewer(layoutInflater.inflate(R.layout.popup_image_viewer, popupFrameLayout),
                                     view, path, filePath, xOff, yOff)
                }

                override fun onGraphicsLongPressed(filePath: String) {
                    if (ConfigurationPreferences.isXmlViewerTextView()) {
                        exitTransition = null
                        FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                    XMLViewerTextView.newInstance(applicationInfo, false, filePath),
                                                    "tv_xml")
                    } else {
                        exitTransition = null
                        FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                    XMLViewerWebView.newInstance(applicationInfo, false, filePath),
                                                    "wv_xml")
                    }
                }
            })
        })
    }

    companion object {
        fun newInstance(applicationInfo: ApplicationInfo): Graphics {
            val args = Bundle()
            args.putParcelable("application_info", applicationInfo)
            val fragment = Graphics()
            fragment.arguments = args
            return fragment
        }
    }
}