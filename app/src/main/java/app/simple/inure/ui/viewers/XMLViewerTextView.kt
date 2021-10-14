package app.simple.inure.ui.viewers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.fastscroll.FastScrollerBuilder
import app.simple.inure.decorations.padding.PaddingAwareNestedScrollView
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.views.CustomProgressBar
import app.simple.inure.decorations.views.TypeFaceEditText
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.dialogs.miscellaneous.ErrorPopup
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.popups.app.PopupXmlViewer
import app.simple.inure.util.ColorUtils.resolveAttrColor
import app.simple.inure.util.ViewUtils.invisible
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.factory.XmlDataFactory
import app.simple.inure.viewmodels.viewers.XMLViewerData
import java.io.IOException

class XMLViewerTextView : ScopedFragment() {

    private lateinit var text: TypeFaceEditText
    private lateinit var name: TypeFaceTextView
    private lateinit var progress: CustomProgressBar
    private lateinit var options: DynamicRippleImageButton
    private lateinit var scrollView: PaddingAwareNestedScrollView

    private lateinit var componentsViewModel: XMLViewerData
    private lateinit var applicationInfoFactory: XmlDataFactory

    private val exportManifest = registerForActivityResult(CreateDocument()) { uri: Uri? ->
        if (uri == null) {
            // Back button pressed.
            return@registerForActivityResult
        }
        try {
            requireContext().contentResolver.openOutputStream(uri).use { outputStream ->
                if (outputStream == null) throw IOException()
                outputStream.write(text.text.toString().toByteArray())
                outputStream.flush()
                Toast.makeText(requireContext(), R.string.saved_successfully, Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), R.string.failed, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_xml_viewer, container, false)

        text = view.findViewById(R.id.text_viewer)
        name = view.findViewById(R.id.xml_name)
        progress = view.findViewById(R.id.xml_loader)
        options = view.findViewById(R.id.xml_viewer_options)
        scrollView = view.findViewById(R.id.xml_nested_scroll_view)

        packageInfo = requireArguments().getParcelable(BundleConstants.packageInfo)!!

        applicationInfoFactory = XmlDataFactory(packageInfo, requireArguments().getBoolean(BundleConstants.isManifest),
                                                requireArguments().getString(BundleConstants.pathToXml)!!,
                                                requireActivity().application,
                                                requireContext().resolveAttrColor(R.attr.colorAppAccent))

        componentsViewModel = ViewModelProvider(this, applicationInfoFactory).get(XMLViewerData::class.java)

        FastScrollerBuilder(scrollView).useMd2Style().build()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startPostponedEnterTransition()

        componentsViewModel.getSpanned().observe(viewLifecycleOwner, {
            text.setText(it)
            name.text = requireArguments().getString("path_to_xml")!!
            progress.hide()
            options.visible()
        })

        componentsViewModel.getError().observe(viewLifecycleOwner, {
            progress.invisible()
            val e = ErrorPopup.newInstance(it)
            e.show(childFragmentManager, "error_dialog")
            e.setOnErrorDialogCallbackListener(object : ErrorPopup.Companion.ErrorDialogCallbacks {
                override fun onDismiss() {
                    requireActivity().onBackPressed()
                }
            })
        })

        options.setOnClickListener {
            PopupXmlViewer(it).setOnPopupClickedListener(object : PopupXmlViewer.PopupXmlCallbacks {
                override fun onPopupItemClicked(source: String) {
                    when (source) {
                        getString(R.string.copy) -> {
                            val clipboard: ClipboardManager? = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                            val clip = ClipData.newPlainText("xml", text.text.toString())
                            clipboard?.setPrimaryClip(clip)
                        }
                        getString(R.string.save) -> {
                            val fileName: String = packageInfo.packageName + "_" + name.text
                            exportManifest.launch(fileName)
                        }
                    }
                }
            })
        }
    }

    override fun onDestroy() {
        text.setText("")
        super.onDestroy()
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo, isManifest: Boolean, pathToXml: String?): XMLViewerTextView {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            args.putBoolean(BundleConstants.isManifest, isManifest)
            args.putString(BundleConstants.pathToXml, pathToXml)
            val fragment = XMLViewerTextView()
            fragment.arguments = args
            return fragment
        }
    }
}
