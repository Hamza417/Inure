package app.simple.inure.util

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.TextAppearanceSpan
import android.widget.TextView
import app.simple.inure.R
import app.simple.inure.util.ColorUtils.resolveAttrColor
import java.util.*

object AdapterUtils {
    fun searchHighlighter(textView: TextView, context: Context, searchKeyword: String) {
        val string = textView.text.toString()
        val sb = SpannableStringBuilder(string)
        val startPos = string.lowercase(Locale.getDefault()).indexOf(searchKeyword.lowercase(Locale.getDefault()))
        val endPos = startPos + searchKeyword.length

        if (startPos != -1) {
            val colorKeyword = ColorStateList(arrayOf(intArrayOf()), intArrayOf(context.resolveAttrColor(R.attr.colorAppAccent)))
            val highlightSpan = TextAppearanceSpan(null, Typeface.NORMAL, -1, colorKeyword, null)
            sb.setSpan(highlightSpan, startPos, endPos, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        }
        textView.text = sb
    }
}