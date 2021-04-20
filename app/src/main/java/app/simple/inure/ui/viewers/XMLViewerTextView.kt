package app.simple.inure.ui.viewers

import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import app.simple.inure.R
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.util.APKParser.extractManifest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import java.io.ByteArrayInputStream
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

class XMLViewerTextView : ScopedFragment() {

    private lateinit var text: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_text_viewer, container, false)

        startPostponedEnterTransition()

        text = view.findViewById(R.id.text_viewer)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        launch {
            val text: String

            withContext(Dispatchers.Default) {
                text = requireArguments().getParcelable<ApplicationInfo>("application_info")?.extractManifest()!!
            }

            this@XMLViewerTextView.text.text = text
        }
    }

    override fun onPreferencesChanged(sharedPreferences: SharedPreferences?, key: String?) {

    }

    companion object {
        fun newInstance(applicationInfo: ApplicationInfo): XMLViewerTextView {
            val args = Bundle()
            args.putParcelable("application_info", applicationInfo)
            val fragment = XMLViewerTextView()
            fragment.arguments = args
            return fragment
        }
    }
}