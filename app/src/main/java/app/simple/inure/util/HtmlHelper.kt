package app.simple.inure.util

import android.os.Build
import android.text.Html
import android.text.Spanned
import org.w3c.dom.Document
import org.w3c.tidy.Tidy
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.util.*

object HtmlHelper {
    fun fromHtml(str: String): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(formatString(str), Html.FROM_HTML_MODE_LEGACY)
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(formatString(str))
        }
    }

    private fun formatString(str: String): String {
        return String.format(Locale.getDefault(), "%s", str)
    }

    fun prettyPrintHTML(rawHTML: String): String {
        val tidy = Tidy()
        tidy.xhtml = true
        tidy.indentContent = true
        tidy.printBodyOnly = true
        tidy.tidyMark = false

        // HTML to DOM
        val htmlDOM: Document = tidy.parseDOM(ByteArrayInputStream(rawHTML.toByteArray()), null)

        // Pretty Print
        val out: OutputStream = ByteArrayOutputStream()
        tidy.pprint(htmlDOM, out)
        return out.toString()
    }
}
