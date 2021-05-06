package app.simple.inure.ui.viewers

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.adapters.details.AdapterGraphics
import app.simple.inure.decorations.corners.DynamicCornerFrameLayout
import app.simple.inure.decorations.views.CustomRecyclerView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.popups.app.PopupImageViewer
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.util.APKParser
import app.simple.inure.util.FragmentHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Graphics : ScopedFragment() {

    private lateinit var recyclerView: CustomRecyclerView
    private lateinit var applicationInfo: ApplicationInfo

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_graphics, container, false)

        recyclerView = view.findViewById(R.id.graphics_recycler_view)
        applicationInfo = requireArguments().getParcelable("application_info")!!

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startPostponedEnterTransition()

        launch {
            val adapterGraphics: AdapterGraphics

            withContext(Dispatchers.IO) {
                adapterGraphics = AdapterGraphics(applicationInfo.sourceDir, APKParser.getGraphicsFiles(applicationInfo.sourceDir))
            }

            recyclerView.adapter = adapterGraphics

            adapterGraphics.setOnResourceClickListener(object : AdapterGraphics.GraphicsCallbacks {
                override fun onGraphicsClicked(path: String, filePath: String, view: ViewGroup, xOff: Float, yOff: Float) {
                    PopupImageViewer(layoutInflater.inflate(R.layout.popup_image_viewer, DynamicCornerFrameLayout(requireContext(), null)),
                                     view, path, filePath, xOff, yOff)
                }

                override fun onGraphicsLongPressed(filePath: String) {
                    if (ConfigurationPreferences.isXmlViewerTextView()) {
                        FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                    XMLViewerTextView.newInstance(applicationInfo, false, filePath),
                                                    "tv_xml")
                    } else {
                        FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                    XMLViewerWebView.newInstance(applicationInfo, false, filePath),
                                                    "wv_xml")
                    }
                }
            })
        }
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