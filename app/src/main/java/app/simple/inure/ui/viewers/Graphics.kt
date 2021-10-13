package app.simple.inure.ui.viewers

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.details.AdapterGraphics
import app.simple.inure.apk.parsers.APKParser
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.views.CustomVerticalRecyclerView
import app.simple.inure.dialogs.miscellaneous.ErrorPopup
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.popups.viewers.PopupGraphicsMenu
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.util.FragmentHelper
import app.simple.inure.viewmodels.factory.PackageInfoFactory
import app.simple.inure.viewmodels.viewers.ApkDataViewModel

class Graphics : ScopedFragment() {

    private lateinit var options: DynamicRippleImageButton
    private lateinit var recyclerView: CustomVerticalRecyclerView
    private var adapterGraphics: AdapterGraphics? = null
    private lateinit var componentsViewModel: ApkDataViewModel
    private lateinit var packageInfoFactory: PackageInfoFactory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_graphics, container, false)

        options = view.findViewById(R.id.graphics_options)
        recyclerView = view.findViewById(R.id.graphics_recycler_view)
        packageInfo = requireArguments().getParcelable("application_info")!!
        packageInfoFactory = PackageInfoFactory(requireActivity().application, packageInfo)
        componentsViewModel = ViewModelProvider(this, packageInfoFactory).get(ApkDataViewModel::class.java)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startPostponedEnterTransition()

        componentsViewModel.getGraphics().observe(viewLifecycleOwner, {
            adapterGraphics = AdapterGraphics(packageInfo.applicationInfo.sourceDir, APKParser.getGraphicsFiles(packageInfo.applicationInfo.sourceDir))

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

        componentsViewModel.getError().observe(viewLifecycleOwner, {
            val e = ErrorPopup.newInstance(it)
            e.show(childFragmentManager, "error_dialog")
            e.setOnErrorDialogCallbackListener(object : ErrorPopup.Companion.ErrorDialogCallbacks {
                override fun onDismiss() {
                    requireActivity().onBackPressed()
                }
            })
        })

        options.setOnClickListener {
            PopupGraphicsMenu(it)
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