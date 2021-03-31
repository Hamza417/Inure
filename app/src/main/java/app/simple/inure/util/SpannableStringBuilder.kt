package app.simple.inure.util

import android.graphics.Color
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan

object SpannableStringBuilder {
    fun buildSpannableString(s: String): SpannableString {
        val spannableString = SpannableString(s)
        spannableString.setSpan(RelativeSizeSpan(0.5f), 5, s.length, 0) // set size
        spannableString.setSpan(ForegroundColorSpan(Color.GRAY), 5, s.length, 0) // set color
        return spannableString
    }

    fun buildSpannableString(string: String, startValue: Int): SpannableString {
        val spannableString = SpannableString(string)
        spannableString.setSpan(RelativeSizeSpan(0.5f), startValue, string.length, 0) // set size
        spannableString.setSpan(ForegroundColorSpan(Color.GRAY), startValue, string.length, 0) // set color
        return spannableString
    }
}