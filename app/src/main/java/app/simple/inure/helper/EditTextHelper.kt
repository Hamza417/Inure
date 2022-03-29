package app.simple.inure.helper

import android.graphics.Typeface
import android.text.Spannable
import android.text.style.TextAppearanceSpan
import android.widget.EditText
import androidx.core.text.toSpannable

object EditTextHelper {
    fun EditText.toBold() {
        var min = 0
        var max: Int = text.length

        if (isFocused) {
            val selStart: Int = selectionStart
            val selEnd: Int = selectionEnd
            min = 0.coerceAtLeast(selStart.coerceAtMost(selEnd))
            max = 0.coerceAtLeast(selStart.coerceAtLeast(selEnd))
        }

        val string = text.toString().subSequence(min, max).toSpannable()

        val highlightSpan = TextAppearanceSpan(null, Typeface.BOLD, -1, textColors, null)
        string.setSpan(highlightSpan, min, max, Spannable.SPAN_INCLUSIVE_INCLUSIVE)

        setText(string)
    }
}