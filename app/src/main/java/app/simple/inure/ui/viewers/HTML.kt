package app.simple.inure.ui.viewers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Bundle
import android.os.TransactionTooLargeException
import android.util.Base64
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
import app.simple.inure.decorations.views.CustomWebView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.factories.panels.TextViewViewModelFactory
import app.simple.inure.popups.viewers.PopupXmlViewer
import app.simple.inure.viewmodels.viewers.TextViewerViewModel
import java.io.IOException
import java.nio.charset.Charset

class HTML : ScopedFragment() {

    private lateinit var scrollView: PaddingAwareNestedScrollView
    private lateinit var html: CustomWebView
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
                outputStream.write(htmlTxt.toByteArray(Charset.defaultCharset()))
                outputStream.flush()
                outputStream.close()
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
                requireArguments().getString(BundleConstants.PATH)!!,
                requireArguments().getBoolean(BundleConstants.IS_RAW),
        )

        textViewerViewModel = ViewModelProvider(this, textViewViewModelFactory)[TextViewerViewModel::class.java]

        path.text = requireArguments().getString(BundleConstants.PATH)!!

        FastScrollerBuilder(scrollView).setupAesthetics().build()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        textViewerViewModel.getText().observe(viewLifecycleOwner) {
            runCatching {
                htmlTxt = it
                val encodedHtml: String = Base64.encodeToString(it.encodeToByteArray(), Base64.NO_PADDING)
                html.loadData(encodedHtml, MimeConstants.htmlType, "base64")
            }.getOrElse {
                showError(it.stackTraceToString())
            }
        }

        options.setOnClickListener {
            PopupXmlViewer(it).setOnPopupClickedListener(object : PopupXmlViewer.PopupXmlCallbacks {
                override fun onPopupItemClicked(source: String) {
                    when (source) {
                        getString(R.string.copy) -> {
                            try {
                                val clipboard: ClipboardManager? = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                                val clip = ClipData.newPlainText("xml", htmlTxt)
                                clipboard?.setPrimaryClip(clip)
                            } catch (e: TransactionTooLargeException) {
                                showWarning("Text is too large to copy", goBack = false)
                            }
                        }
                        getString(R.string.export) -> {
                            val name = with(path.text.toString()) {
                                substring(lastIndexOf("/") + 1, length)
                            }

                            exportText.launch(name)
                        }
                    }
                }
            })
        }
    }

    companion object {

        /**
         * @param packageInfo: PackageInfo of the app
         * @param path: path of the file to be opened
         * @param isRaw: true if the file is specified directly from the
         *               app and not needed to be fetched from the [PackageInfo],
         *               If true, [packageInfo] can be null however it's recommended
         *               to pass the empty [PackageInfo] object
         */
        fun newInstance(packageInfo: PackageInfo, path: String, isRaw: Boolean = false): HTML {
            val args = Bundle()
            args.putParcelable(BundleConstants.PACKAGE_INFO, packageInfo)
            args.putString(BundleConstants.PATH, path)
            args.putBoolean(BundleConstants.IS_RAW, isRaw)
            val fragment = HTML()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "html_viewer"
    }
}
