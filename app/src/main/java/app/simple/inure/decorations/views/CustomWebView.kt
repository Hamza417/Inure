package app.simple.inure.decorations.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.AttributeSet
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.postDelayed
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import app.simple.inure.interfaces.fragments.WebviewCallbacks
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.themes.manager.ThemeUtils
import app.simple.inure.util.ColorUtils.toHexColor
import app.simple.inure.util.ViewUtils.invisible
import app.simple.inure.util.ViewUtils.visible

@SuppressLint("SetJavaScriptEnabled")
open class CustomWebView(context: Context, attributeSet: AttributeSet) : WebView(context, attributeSet) {

    private val color: String
    private var webviewCallbacks: WebviewCallbacks? = null

    init {
        settings.allowContentAccess = true
        settings.allowFileAccess = true
        settings.setSupportZoom(true)
        settings.javaScriptEnabled = true
        webChromeClient = WebChromeClient()

        invisible(animate = false)

        // TODO - Calling non-final function setBackgroundColor in constructor
        @Suppress("LeakingThis")
        setBackgroundColor(0)

        color = AppearancePreferences.getAccentColor().toHexColor()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
                WebSettingsCompat.setAlgorithmicDarkeningAllowed(this.settings, true)
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
                super.onPageFinished(view, url)
                if (ThemeUtils.isNightMode(resources)) {
                    view.evaluateJavascript("CssLoader.loadDarkCss()") {
                        view.loadUrl("javascript:document.body.style.setProperty(\"color\", \"$color\");")
                        post {
                            webviewCallbacks?.onPageLoadFinished()
                        }

                        postDelayed(DELAY) {
                            visible(animate = false)
                        }
                    }
                } else {
                    view.evaluateJavascript("CssLoader.loadLightCss()") {
                        view.loadUrl("javascript:document.body.style.setProperty(\"color\", \"$color\");")
                        post {
                            webviewCallbacks?.onPageLoadFinished()
                        }

                        postDelayed(DELAY) {
                            visible(animate = false)
                        }
                    }
                }
            }

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url.toString()
                if (url.contains("asset/")) {
                    view.loadUrl(url)
                } else {
                    val intent = Intent(Intent.ACTION_VIEW, request.url)
                    context.startActivity(intent)
                }
                return true
            }
        }
    }

    fun setOnPageFinishedListener(webviewCallbacks: WebviewCallbacks) {
        this.webviewCallbacks = webviewCallbacks
    }

    companion object {
        private const val TAG = "CustomWebView"
        private const val DELAY = 300L
    }
}
