package app.simple.inure.decorations.views

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.webkit.WebView
import android.widget.Toast
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature

open class CustomWebView(context: Context, attributeSet: AttributeSet) : WebView(context, attributeSet) {
    init {
        settings.allowContentAccess = true
        settings.allowFileAccess = true
        settings.setSupportZoom(true)

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