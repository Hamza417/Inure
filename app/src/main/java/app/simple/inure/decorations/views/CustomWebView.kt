package app.simple.inure.decorations.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import app.simple.inure.R
import app.simple.inure.util.ColorUtils.resolveAttrColor
import app.simple.inure.util.ColorUtils.toHexColor

@SuppressLint("SetJavaScriptEnabled")
open class CustomWebView(context: Context, attributeSet: AttributeSet) : WebView(context, attributeSet) {

    private val color: String

    init {
        settings.allowContentAccess = true
        settings.allowFileAccess = true
        settings.setSupportZoom(true)
        settings.javaScriptEnabled = true

        color = context.resolveAttrColor(R.attr.colorAppAccent).toHexColor()

        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            if (context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
                WebSettingsCompat.setForceDark(this.settings, WebSettingsCompat.FORCE_DARK_ON)
            }
        }

        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                view.loadUrl(
                        "javascript:document.body.style.setProperty(\"color\", \"$color\");"
                )
            }
        }
    }
}