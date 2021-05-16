package app.simple.inure.ui.viewers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import app.simple.inure.R
import app.simple.inure.decorations.popup.PopupLinearLayout
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.views.CustomWebView
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.popups.app.PopupXmlViewer
import app.simple.inure.util.APKParser.extractManifest
import app.simple.inure.util.APKParser.getTransBinaryXml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException


class XMLViewerWebView : ScopedFragment() {

    private lateinit var manifest: CustomWebView
    private lateinit var name: TypeFaceTextView
    private lateinit var options: DynamicRippleImageButton

    private var code = ""

    private val exportManifest = registerForActivityResult(ActivityResultContracts.CreateDocument()) { uri: Uri? ->
        if (uri == null) {
            // Back button pressed.
            return@registerForActivityResult
        }
        try {
            requireContext().contentResolver.openOutputStream(uri).use { outputStream ->
                if (outputStream == null) throw IOException()
                outputStream.write(code.toByteArray())
                outputStream.flush()
                Toast.makeText(requireContext(), R.string.saved_successfully, Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), R.string.failed, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_web_viewer, container, false)

        startPostponedEnterTransition()

        manifest = view.findViewById(R.id.source_view)
        name = view.findViewById(R.id.xml_name)
        options = view.findViewById(R.id.xml_viewer_options)

        applicationInfo = requireArguments().getParcelable("application_info")!!

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            val text: String
            val name: String

            withContext(Dispatchers.Default) {
                code = if (requireArguments().getBoolean("is_manifest")) {
                    name = "AndroidManifest.xml"
                    requireArguments().getParcelable<ApplicationInfo>("application_info")?.extractManifest()!!
                } else {
                    name = requireArguments().getString("path_to_xml")!!
                    requireArguments().getParcelable<ApplicationInfo>("application_info")!!
                            .getTransBinaryXml(requireArguments().getString("path_to_xml")!!)
                }

                text = Html.escapeHtml(code)
            }

            loadSourceCode(text)
            this@XMLViewerWebView.name.text = name
        }

        options.setOnClickListener {
            val p = PopupXmlViewer(LayoutInflater.from(requireContext())
                                           .inflate(R.layout.popup_xml_options,
                                                    PopupLinearLayout(requireContext()),
                                                    false), it)

            p.setOnPopupClickedListener(object : PopupXmlViewer.PopupXmlCallbacks {
                override fun onPopupItemClicked(source: String) {
                    when (source) {
                        getString(R.string.copy) -> {
                            val clipboard: ClipboardManager? = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                            val clip = ClipData.newPlainText("xml", code)
                            clipboard?.setPrimaryClip(clip)
                        }
                        getString(R.string.save) -> {
                            val fileName: String = applicationInfo.packageName + "_" + name.text
                            exportManifest.launch(fileName)
                        }
                    }
                }
            })
        }
    }

    private fun loadSourceCode(html: String) {
        val data = String.format(
            "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3" +
                    ".org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><html xmlns=\"http://www.w3" +
                    ".org/1999/xhtml\"><head><meta http-equiv=\"Content-Type\" content=\"text/html; " +
                    "charset=utf-8\" /><p style=\"word-wrap: break-word;\"><script src=\"run_prettify.js?skin=github\"></script></head><body " +
                    "bgcolor=\"transparent\"><pre class=\"prettyprint linenums\">%s</pre></body></html>", html)
        manifest.loadDataWithBaseURL("file:///android_asset/", data, "text/html", "UTF-8", null)
    }

    companion object {
        fun newInstance(applicationInfo: ApplicationInfo?, isManifest: Boolean, pathToXml: String?): XMLViewerWebView {
            val args = Bundle()
            args.putParcelable("application_info", applicationInfo)
            args.putBoolean("is_manifest", isManifest)
            args.putString("path_to_xml", pathToXml)
            val fragment = XMLViewerWebView()
            fragment.arguments = args
            return fragment
        }
    }
}