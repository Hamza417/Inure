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
        val pattern = searchKeyword.lowercase().toRegex()
        val spannable = textView.text.toSpannable()
        val matcher = pattern.toPattern().matcher(spannable.toString().lowercase(Locale.getDefault()).toSpannable())

        while (matcher.find()) {
            val colorKeyword = ColorStateList(arrayOf(intArrayOf()), intArrayOf(AppearancePreferences.getAccentColor()))
            val highlightSpan = TextAppearanceSpan(null, Typeface.BOLD, -1, colorKeyword, null)
            spannable.setSpan(highlightSpan, matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        textView.text = spannable
    }

    fun searchHighlighter(textView: TextView, searchKeyword: String, ignoreCasing: Boolean) {
        val pattern = if (ignoreCasing) {
            searchKeyword.lowercase().toRegex()
        } else {
            searchKeyword.toRegex()
        }

        val spannable = textView.text.toSpannable()

        val matcher = if (ignoreCasing) {
            pattern.toPattern().matcher(spannable.toString().lowercase(Locale.getDefault()).toSpannable())
        } else {
            pattern.toPattern().matcher(spannable.toString().toSpannable())
        }

        while (matcher.find()) {
            val colorKeyword = ColorStateList(arrayOf(intArrayOf()), intArrayOf(AppearancePreferences.getAccentColor()))
            val highlightSpan = TextAppearanceSpan(null, Typeface.BOLD, -1, colorKeyword, null)
            spannable.setSpan(highlightSpan, matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        textView.text = spannable
    }
}