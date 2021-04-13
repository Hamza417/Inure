package app.simple.inure.decorations.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.AssetManager
import android.content.res.Configuration
import android.net.Uri
import android.util.AttributeSet
import android.webkit.*
import android.widget.Toast
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import java.io.IOException
import java.io.InputStream


@SuppressLint("SetJavaScriptEnabled")
class CustomWebView(context: Context, attributeSet: AttributeSet) : WebView(context, attributeSet) {
    init {

        val webViewClient: WebViewClient = object : WebViewClient() {

            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                println(request)
                val stream: InputStream? = inputStreamForAndroidResource(request.url.toString())
                return if (stream != null) {
                    println("Not null")
                    WebResourceResponse("text/javascript", "UTF-8", stream)
                } else {
                    println("null")
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

                        println(assets.open(uri.path!!, AssetManager.ACCESS_STREAMING))
                        return assets.open(uri.path!!, AssetManager.ACCESS_STREAMING)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                return null
            }
        }

        this.setBackgroundColor(0)
        this.webViewClient = webViewClient
        this.webChromeClient = WebChromeClient()
        this.settings.useWideViewPort = true
        this.settings.setSupportZoom(true)
        this.settings.domStorageEnabled = true
        this.settings.allowContentAccess = true
        this.settings.allowFileAccess = true
        this.settings.javaScriptEnabled = true
        this.settings.defaultTextEncodingName = "UTF-8"

        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            if (context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
                WebSettingsCompat.setForceDark(this.settings, WebSettingsCompat.FORCE_DARK_ON)
            }
        } else {
            Toast.makeText(context, "If you are having trouble viewing this make sure you are using the latest WebView", Toast.LENGTH_LONG).show()
        }
    }
}
