package app.simple.inure.dialogs.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.views.XmlWebView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment

class HtmlViewer : ScopedBottomSheetFragment() {

    private lateinit var webView: XmlWebView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_html, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView = view.findViewById(R.id.web_view)

        if (this.arguments != null) {
            when (this.requireArguments().getString("source")) {
                getString(R.string.permissions) -> {
                    webView.loadUrl("file:///android_asset/html/required_permissions.html")
                }
            }
        }
    }

    companion object {
        fun newInstance(string: String): HtmlViewer {
            val args = Bundle()
            args.putString("source", string)
            val fragment = HtmlViewer()
            fragment.arguments = args
            return fragment
        }
    }
}
