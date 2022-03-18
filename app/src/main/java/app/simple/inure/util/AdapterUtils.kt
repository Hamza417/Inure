package app.simple.inure.util

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.text.Spannable
import android.text.style.TextAppearanceSpan
import android.widget.TextView
import androidx.core.text.toSpannable
import app.simple.inure.R
import app.simple.inure.util.ColorUtils.resolveAttrColor
import java.util.*

object AdapterUtils {
    fun searchHighlighter(textView: TextView, searchKeyword: String) {
        val string = textView.text.toSpannable()
        val startPos = string.toString().lowercase(Locale.getDefault()).indexOf(searchKeyword.lowercase(Locale.getDefault()))
        val endPos = startPos + searchKeyword.length

        if (startPos != -1) {
            val colorKeyword = ColorStateList(arrayOf(intArrayOf()), intArrayOf(textView.context.resolveAttrColor(R.attr.colorAppAccent)))
            val highlightSpan = TextAppearanceSpan(null, Typeface.NORMAL, -1, colorKeyword, null)
            string.setSpan(highlightSpan, startPos, endPos, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        }
        textView.text = string
    }

    fun searchHighlighter(textView: TextView, searchKeyword: String, ignoreCasing: Boolean) {
        val string = textView.text.toSpannable()

        val startPos = if (ignoreCasing) {
            string.toString().lowercase(Locale.getDefault()).indexOf(searchKeyword.lowercase(Locale.getDefault()))
        } else {
            string.toString().indexOf(searchKeyword)
        }

        val endPos = startPos + searchKeyword.length

        if (startPos != -1) {
            val colorKeyword = ColorStateList(arrayOf(intArrayOf()), intArrayOf(textView.context.resolveAttrColor(R.attr.colorAppAccent)))
            val highlightSpan = TextAppearanceSpan(null, Typeface.NORMAL, -1, colorKeyword, null)
            string.setSpan(highlightSpan, startPos, endPos, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        }

        textView.text = string
    }
}