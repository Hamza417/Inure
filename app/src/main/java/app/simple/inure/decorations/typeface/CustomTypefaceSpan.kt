package app.simple.inure.decorations.typeface

import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.TypefaceSpan

class CustomTypefaceSpan(private val customTypeface: Typeface) : TypefaceSpan("") {
    override fun updateDrawState(ds: TextPaint) {
        applyCustomTypeFace(ds, customTypeface)
    }
    override fun updateMeasureState(paint: TextPaint) {
        applyCustomTypeFace(paint, customTypeface)
    }
    private fun applyCustomTypeFace(paint: Paint, tf: Typeface) {
        paint.typeface = tf
    }
}