package app.simple.inure.decorations.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.AssetManager
import android.content.res.Configuration
import android.net.Uri
import android.util.AttributeSet
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import java.io.IOException
import java.io.InputStream

@SuppressLint("SetJavaScriptEnabled")
class WebViewXMLViewer(context: Context, attributeSet: AttributeSet) : WebView(context, attributeSet) {
    init {
        settings.apply {
            useWideViewPort = false
            setSupportZoom(true)
            domStorageEnabled = true
            allowContentAccess = true
            allowFileAccess = true
            javaScriptEnabled = true
            defaultTextEncodingName = "UTF-8"
            builtInZoomControls = true
            displayZoomControls = false
            layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
        }

        setBackgroundColor(0)
        webChromeClient = WebChromeClient()

        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            if (context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
                @Suppress("DEPRECATION")
                WebSettingsCompat.setForceDark(this.settings, WebSettingsCompat.FORCE_DARK_ON)
            }
        } else {
            Toast.makeText(context, "If you are having trouble viewing this make sure you are using the latest WebView", Toast.LENGTH_LONG).show()
        }
    }

    fun enableWithWebClient() {
        this.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                val stream: InputStream? = inputStreamForAndroidResource(request.url.toString())
                return if (stream != null) {
                    WebResourceResponse("text/javascript", "UTF-8", stream)
                } else {
                    super.shouldInterceptRequest(view, request)
                }

            }

            private fun inputStreamForAndroidResource(url: String): InputStream? {
                val modifiedUrl: String
                val androidAssets = "file:///android_asset/"
                if (url.contains(androidAssets)) {
                    modifiedUrl = url.replaceFirst(androidAssets.toRegex(), "")
                    try {
                        val assets: AssetManager = context.assets
                        val uri: Uri = Uri.parse(modifiedUrl)
                        return assets.open(uri.path!!, AssetManager.ACCESS_STREAMING)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                return null
            }
        }
    }
}
