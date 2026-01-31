package app.simple.inure.ui.panels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.fastscroll.FastScrollWebView
import app.simple.inure.decorations.fastscroll.FastScrollerBuilder
import app.simple.inure.decorations.padding.PaddingAwareNestedScrollView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.math.Extensions.percentOf
import app.simple.inure.util.LocaleUtils
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.util.NullSafety.isNull

class WebPage : ScopedFragment() {

    private lateinit var webView: FastScrollWebView
    private lateinit var scrollView: PaddingAwareNestedScrollView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_web_page_viewer, container, false)

        webView = view.findViewById(R.id.web_view)
        scrollView = view.findViewById(R.id.web_page_container)

        FastScrollerBuilder(scrollView).build()
        startPostponedEnterTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView.setOnPageFinishedListener {
            postDelayed {
                if (savedInstanceState.isNotNull()) {
                    savedInstanceState?.getFloat(BundleConstants.SCROLL_POSITION)?.let {
                        scrollView.post {
                            scrollView.scrollTo(0, (it * scrollView.getChildAt(0).height).toInt())
                        }
                    }
                }
            }
        }

        if (savedInstanceState.isNull()) {
            when (this.requireArguments().getString(BundleConstants.WEB_PAGE)) {
                getString(R.string.permissions) -> {
                    webView.loadUrl("file:///android_asset/html/required_permissions.html")
                }
                getString(R.string.credits) -> {
                    webView.loadUrl("file:///android_asset/html/credits.html")
                }
                getString(R.string.change_logs) -> {
                    webView.loadUrl("file:///android_asset/html/changelogs.html")
                }
                getString(R.string.open_source_licenses) -> {
                    webView.loadUrl("file:///android_asset/html/open_source.html")
                }
                getString(R.string.user_agreements) -> {
                    webView.loadUrl("file:///android_asset/html/gpl.html")
                }
                getString(R.string.privacy_policy) -> {
                    when (LocaleUtils.getAppLocale().language) {
                        "ar",
                        "ar-rSA" -> {
                            webView.loadUrl("file:///android_asset/l10n_html/ar/privacy.html")
                        }
                        else -> {
                            webView.loadUrl("file:///android_asset/l10n_html/en/privacy.html")
                        }
                    }
                }
                getString(R.string.translate) -> {
                    webView.loadUrl("file:///android_asset/html/translation.html")
                }
                else -> {
                    webView.loadUrl(requireArguments().getString(BundleConstants.WEB_PAGE)!!)
                }
            }
        } else {
            webView.restoreState(savedInstanceState!!)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        webView.saveState(outState)
        outState.putFloat(
                BundleConstants.SCROLL_POSITION,
                scrollView.scrollY.percentOf(
                        scrollView.getChildAt(0).height).div(100))
        super.onSaveInstanceState(outState)
    }

    companion object {
        fun newInstance(string: String): WebPage {
            val args = Bundle()
            args.putString(BundleConstants.WEB_PAGE, string)
            val fragment = WebPage()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "WebPage"
    }
}
