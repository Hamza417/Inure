package app.simple.inure.decorations.views

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.util.ColorUtils.toHexColor
import app.simple.inure.util.ThemeUtils

@SuppressLint("SetJavaScriptEnabled")
open class CustomWebView(context: Context, attributeSet: AttributeSet) : WebView(context, attributeSet) {

    private val color: String

    init {
        settings.allowContentAccess = true
        settings.allowFileAccess = true
        settings.setSupportZoom(true)
        settings.javaScriptEnabled = true

        // TODO - Calling non-final function setBackgroundColor in constructor
        setBackgroundColor(0)

        color = AppearancePreferences.getAccentColor().toHexColor()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
                WebSettingsCompat.setAlgorithmicDarkeningAllowed(this.settings, ThemeUtils.isNightMode(resources))
            } else if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                if (ThemeUtils.isNightMode(resources)) {
                    @Suppress("DEPRECATION")
                    WebSettingsCompat.setForceDark(this.settings, WebSettingsCompat.FORCE_DARK_ON)
                }
            }
        } else {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                if (ThemeUtils.isNightMode(resources)) {
                    @Suppress("DEPRECATION")
                    WebSettingsCompat.setForceDark(this.settings, WebSettingsCompat.FORCE_DARK_ON)
                }
            }
        }

        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                view.loadUrl("javascript:document.body.style.setProperty(\"color\", \"$color\");")
            }
        }
    }
}