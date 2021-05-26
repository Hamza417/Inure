package app.simple.inure.ui.viewers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.lifecycle.lifecycleScope
import app.simple.inure.R
import app.simple.inure.decorations.popup.PopupLinearLayout
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.views.TypeFaceEditText
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.exception.StringTooLargeException
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.popups.app.PopupXmlViewer
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.util.APKParser.extractManifest
import app.simple.inure.util.APKParser.getTransBinaryXml
import app.simple.inure.util.ColorUtils.resolveAttrColor
import app.simple.inure.util.ViewUtils.makeGoAway
import app.simple.inure.util.ViewUtils.makeInvisible
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.regex.Matcher
import java.util.regex.Pattern

class XMLViewerTextView : ScopedFragment() {

    private val quotations: Pattern = Pattern.compile("\"([^\"]*)\"", Pattern.MULTILINE)

    private val tags = Pattern.compile(
        "\\B<\\w+\\b(?![.])(?<!:NULL)" + // <xml.yml.zml
                "|\\B\\<\\/\\w+(?=\\S*['-])([a-zA-Z'-]+>)" + // </xml-yml>
                "|\\B\\<\\/\\w+(?=\\S*['-])([a-zA-Z'-]+)" + // </xml-yml
                "|\\B</\\w+>" + // </xml>
                "|\\B</\\w+" + // </xml
                "|\\B<\\w+\\/>" + // <xml/>
                "|\\B<\\w+>" +  // <xml>
                "|\\B<\\w+" +  // <xml
                "|\\B\\?\\w+" + // ?xml
                "|\\?\\>" + // ?>
                "|\\>" + // >
                "|\\B<" + // <
                "|\\/>", // />
        Pattern.MULTILINE or Pattern.CASE_INSENSITIVE)

    private var formattedContent: SpannableString? = null
    private lateinit var text: TypeFaceEditText
    private lateinit var name: TypeFaceTextView
    private lateinit var progress: ProgressBar
    private lateinit var options: DynamicRippleImageButton

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

        applicationInfo = requireArguments().getParcelable("application_info")!!

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startPostponedEnterTransition()

        viewLifecycleOwner.lifecycleScope.launch {
            runCatching {
                val name: String
                val code: String

                withContext(Dispatchers.Default) {
                    delay(500) // Lets the animations finish first
                    code = if (requireArguments().getBoolean("is_manifest")) {
                        name = "AndroidManifest.xml"
                        applicationInfo.extractManifest()!!
                    } else {
                        name = requireArguments().getString("path_to_xml")!!
                        applicationInfo.getTransBinaryXml(requireArguments().getString("path_to_xml")!!)
                    }

                    if (code.length >= 150000 && !ConfigurationPreferences.isLoadingLargeStrings()) {
                        throw StringTooLargeException("String size ${code.length} is too big to render without freezing the app")
                    }

                    formattedContent = SpannableString(code)
                    val matcher: Matcher = tags.matcher(code)
                    while (matcher.find()) {
                        formattedContent!!.setSpan(ForegroundColorSpan(Color.parseColor("#2980B9")), matcher.start(),
                                                   matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                    matcher.usePattern(quotations)
                    while (matcher.find()) {
                        formattedContent!!.setSpan(ForegroundColorSpan(requireContext().resolveAttrColor(R.attr.colorAppAccent)),
                                                   matcher.start(), matcher.end(),
                                                   Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }

                this@XMLViewerTextView.text.setText(formattedContent)
                this@XMLViewerTextView.name.text = name
                progress.makeInvisible()

            }.getOrElse {
                this@XMLViewerTextView.text.setText(it.stackTraceToString())
                this@XMLViewerTextView.text.setTextColor(Color.RED)
                this@XMLViewerTextView.name.text = getString(R.string.error)
                progress.makeInvisible()
                options.makeGoAway()
            }
        }

        options.setOnClickListener {
            val p = PopupXmlViewer(LayoutInflater.from(requireContext())
                                           .inflate(R.layout.popup_xml_options,
                                                    PopupLinearLayout(requireContext()),
                                                    true), it)

            p.setOnPopupClickedListener(object : PopupXmlViewer.PopupXmlCallbacks {
                override fun onPopupItemClicked(source: String) {
                    when (source) {
                        getString(R.string.copy) -> {
                            val clipboard: ClipboardManager? = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                            val clip = ClipData.newPlainText("xml", text.text.toString())
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

    override fun onDestroy() {
        text.setText("")
        super.onDestroy()
    }

    companion object {
        fun newInstance(applicationInfo: ApplicationInfo?, isManifest: Boolean, pathToXml: String?): XMLViewerTextView {
            val args = Bundle()
            args.putParcelable("application_info", applicationInfo)
            args.putBoolean("is_manifest", isManifest)
            args.putString("path_to_xml", pathToXml)
            val fragment = XMLViewerTextView()
            fragment.arguments = args
            return fragment
        }
    }
}
