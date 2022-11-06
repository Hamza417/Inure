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
import app.simple.inure.constants.MimeConstants
import app.simple.inure.decorations.fastscroll.FastScrollerBuilder
import app.simple.inure.decorations.padding.PaddingAwareNestedScrollView
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.XmlWebView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.factories.panels.TextViewViewModelFactory
import app.simple.inure.popups.app.PopupXmlViewer
import app.simple.inure.viewmodels.viewers.TextViewerViewModel
import java.io.IOException

class HtmlViewer : ScopedFragment() {

    private lateinit var scrollView: PaddingAwareNestedScrollView
    private lateinit var html: XmlWebView
    private lateinit var path: TypeFaceTextView
    private lateinit var options: DynamicRippleImageButton

    private lateinit var textViewerViewModel: TextViewerViewModel
    private lateinit var textViewViewModelFactory: TextViewViewModelFactory

    private var htmlTxt: String = ""

    private val exportText = registerForActivityResult(CreateDocument(MimeConstants.htmlType)) { uri: Uri? ->
        if (uri == null) {
            // Back button pressed.
            return@registerForActivityResult
        }
        try {
            requireContext().contentResolver.openOutputStream(uri).use { outputStream ->
                if (outputStream == null) throw IOException()
                outputStream.write(htmlTxt.toByteArray())
                outputStream.flush()
                Toast.makeText(requireContext(), R.string.saved_successfully, Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), R.string.failed, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_html_viewer, container, false)

        scrollView = view.findViewById(R.id.html_viewer_scroll_view)
        html = view.findViewById(R.id.html_viewer)
        path = view.findViewById(R.id.html_name)
        options = view.findViewById(R.id.html_viewer_options)

        textViewViewModelFactory = TextViewViewModelFactory(
                packageInfo,
                requireArguments().getString("path")!!,
        )

        textViewerViewModel = ViewModelProvider(this, textViewViewModelFactory)[TextViewerViewModel::class.java]

        path.text = requireArguments().getString("path")!!

        FastScrollerBuilder(scrollView).setupAesthetics().build()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startPostponedEnterTransition()

        textViewerViewModel.getText().observe(viewLifecycleOwner) {
            runCatching {
                htmlTxt = it
                html.loadData(it, "text/html", "UTF-8")
            }.getOrElse {
                showError(it.stackTraceToString())
            }
        }

        options.setOnClickListener {
            val p = PopupXmlViewer(it)

            p.setOnPopupClickedListener(object : PopupXmlViewer.PopupXmlCallbacks {
                override fun onPopupItemClicked(source: String) {
                    when (source) {
                        getString(R.string.copy) -> {
                            val clipboard: ClipboardManager? = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                            val clip = ClipData.newPlainText("xml", htmlTxt)
                            clipboard?.setPrimaryClip(clip)
                        }
                        getString(R.string.save) -> {
                            val fileName: String = packageInfo.packageName + "_" + path.text
                            exportText.launch(fileName)
                        }
                    }
                }
            })
        }
    }

    companion object {
        fun newInstance(applicationInfo: PackageInfo, path: String): HtmlViewer {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, applicationInfo)
            args.putString("path", path)
            val fragment = HtmlViewer()
            fragment.arguments = args
            return fragment
        }
    }
}
