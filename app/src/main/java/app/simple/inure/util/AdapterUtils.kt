package app.simple.inure.util

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.text.Spannable
import android.text.style.TextAppearanceSpan
import android.widget.TextView
import androidx.core.text.toSpannable
import app.simple.inure.preferences.AppearancePreferences
import java.util.*

object AdapterUtils {
    fun searchHighlighter(textView: TextView, searchKeyword: String) {
        val string = textView.text.toSpannable()
        val startPos = string.toString().lowercase(Locale.getDefault()).indexOf(searchKeyword.lowercase(Locale.getDefault()))
        val endPos = startPos + searchKeyword.length

        if (startPos != -1) {
            val colorKeyword = ColorStateList(arrayOf(intArrayOf()), intArrayOf(AppearancePreferences.getAccentColor()))
            val highlightSpan = TextAppearanceSpan(null, Typeface.NORMAL, -1, colorKeyword, null)
            string.setSpan(highlightSpan, startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        textView.text = string
    }

    fun searchHighlighter(textView: TextView, searchKeyword: String, ignoreCasing: Boolean) {
        val string: Spannable = textView.text.toSpannable()

        val startPos = if (ignoreCasing) {
            string.toString().lowercase(Locale.getDefault()).indexOf(searchKeyword.lowercase(Locale.getDefault()))
        } else {
            string.toString().indexOf(searchKeyword)
        }

        val endPos = startPos + searchKeyword.length

        if (startPos != -1) {
            val colorKeyword = ColorStateList(arrayOf(intArrayOf()), intArrayOf(AppearancePreferences.getAccentColor()))
            val highlightSpan = TextAppearanceSpan(null, Typeface.NORMAL, -1, colorKeyword, null)
            string.setSpan(highlightSpan, startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        textView.text = string
    }
}