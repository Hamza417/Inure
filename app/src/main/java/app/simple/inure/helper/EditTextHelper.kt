package app.simple.inure.helper

import android.graphics.Typeface
import android.text.Spannable
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.widget.EditText

object EditTextHelper {
    fun EditText.toBold() {
        text.setSpan(StyleSpan(Typeface.BOLD), selectionStart, selectionEnd, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
    }

    fun EditText.toItalics() {
        text.setSpan(StyleSpan(Typeface.ITALIC), selectionStart, selectionEnd, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
    }

    fun EditText.toUnderline() {
        text.setSpan(UnderlineSpan(), selectionStart, selectionEnd, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
    }
}