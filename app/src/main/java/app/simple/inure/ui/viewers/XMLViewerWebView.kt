package app.simple.inure.ui.viewers

import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.views.CustomWebView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.util.APKParser.extractManifest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class XMLViewerWebView : ScopedFragment() {

    private lateinit var manifest: CustomWebView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_web_viewer, container, false)

        startPostponedEnterTransition()

        manifest = view.findViewById(R.id.source_view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        launch {
            val text: String

            withContext(Dispatchers.Default) {
                text = Html.escapeHtml(requireArguments().getParcelable<ApplicationInfo>("application_info")?.extractManifest()!!)
            }

            //println(text)

            loadSourceCode(text)
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
        fun newInstance(applicationInfo: ApplicationInfo): XMLViewerWebView {
            val args = Bundle()
            args.putParcelable("application_info", applicationInfo)
            val fragment = XMLViewerWebView()
            fragment.arguments = args
            return fragment
        }
    }
}