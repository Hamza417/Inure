package app.simple.inure.text

import android.graphics.BlurMaskFilter
import android.graphics.Typeface
import android.os.Build
import android.text.Layout
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.*
import android.widget.EditText
import app.simple.inure.R
import app.simple.inure.util.ColorUtils.resolveAttrColor
import java.util.*
import kotlin.math.roundToInt

object EditTextHelper {

    private const val bulletGap = 16
    private const val bulletRadius = 8
    private const val stripWidth = 6
    private const val blurRadius = 5f
    private const val spanUpperThreshold = 96
    private const val spanLowerThreshold = 12

    fun EditText.toBold() {
        val spans: Array<StyleSpan> = text.getSpans(selectionStart, selectionEnd, StyleSpan::class.java)
        var exists = false

        for (span in spans) {
            if (span.style == Typeface.BOLD) {
                text.removeSpan(span)
                exists = true
            }
        }

        if (!exists) {
            text.setSpan(StyleSpan(Typeface.BOLD), selectionStart, selectionEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    fun EditText.toItalics() {
        val spans: Array<StyleSpan> = text.getSpans(selectionStart, selectionEnd, StyleSpan::class.java)
        var exists = false

        for (span in spans) {
            if (span.style == Typeface.ITALIC) {
                text.removeSpan(span)
                exists = true
            }
        }

        if (!exists) {
            text.setSpan(StyleSpan(Typeface.ITALIC), selectionStart, selectionEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    fun EditText.toUnderline() {
        val spans: Array<UnderlineSpan> = text.getSpans(selectionStart, selectionEnd, UnderlineSpan::class.java)
        var exists = false

        for (span in spans) {
            text.removeSpan(span)
            exists = true
        }

        if (!exists) {
            text.setSpan(UnderlineSpan(), selectionStart, selectionEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    fun EditText.toStrikethrough() {
        val spans: Array<StrikethroughSpan> = text.getSpans(selectionStart, selectionEnd, StrikethroughSpan::class.java)
        var exists = false

        for (span in spans) {
            text.removeSpan(span)
            exists = true
        }

        if (!exists) {
            text.setSpan(StrikethroughSpan(), selectionStart, selectionEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    fun EditText.addBullet() {
        val spans: Array<BulletSpan> = text.getSpans(selectionStart, selectionEnd, BulletSpan::class.java)
        var exists = false

        for (span in spans) {
            text.removeSpan(span)
            exists = true
        }

        if (!exists) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                text.setSpan(BulletSpan(bulletGap, context.resolveAttrColor(R.attr.colorAppAccent), bulletRadius),
                             selectionStart, selectionEnd,
                             Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            } else {
                text.setSpan(BulletSpan(bulletGap, context.resolveAttrColor(R.attr.colorAppAccent)),
                             selectionStart, selectionEnd,
                             Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            }
        }
    }

    fun EditText.toSuperscript() {
        val spans: Array<SuperscriptSpan> = text.getSpans(selectionStart, selectionEnd, SuperscriptSpan::class.java)
        var exists = false

        for (span in spans) {
            text.removeSpan(span)
            exists = true
        }

        if (!exists) {
            text.setSpan(SuperscriptSpan(), selectionStart, selectionEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    fun EditText.toSubscript() {
        val spans: Array<SubscriptSpan> = text.getSpans(selectionStart, selectionEnd, SubscriptSpan::class.java)
        var exists = false

        for (span in spans) {
            text.removeSpan(span)
            exists = true
        }

        if (!exists) {
            text.setSpan(SubscriptSpan(), selectionStart, selectionEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    fun EditText.increaseTextSize() {
        val spans: Array<AbsoluteSizeSpan> = text.getSpans(selectionStart, selectionEnd, AbsoluteSizeSpan::class.java)
        var fontSize: Int = textSize.roundToInt()

        for (span in spans) {
            fontSize = if (span.size.plus(2) > spanUpperThreshold) {
                spanUpperThreshold
            } else {
                span.size.plus(2)
            }
        }

        text.setSpan(AbsoluteSizeSpan(fontSize), selectionStart, selectionEnd, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
    }

    fun EditText.decreaseTextSize() {
        val spans: Array<AbsoluteSizeSpan> = text.getSpans(selectionStart, selectionEnd, AbsoluteSizeSpan::class.java)
        var fontSize: Int = textSize.roundToInt()

        for (span in spans) {
            fontSize = if (span.size.minus(2) < spanLowerThreshold) {
                spanLowerThreshold
            } else {
                span.size.minus(2)
            }
        }

        text.setSpan(AbsoluteSizeSpan(fontSize), selectionStart, selectionEnd, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
    }

    fun EditText.highlightText(color: Int) {
        val spans: Array<BackgroundColorSpan> = text.getSpans(selectionStart, selectionEnd, BackgroundColorSpan::class.java)
        var exists = false

        for (span in spans) {
            text.removeSpan(span)
            exists = true
        }

        if (!exists) {
            text.setSpan(BackgroundColorSpan(color), selectionStart, selectionEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    fun EditText.toQuote() {
        val spans: Array<QuoteSpan> = text.getSpans(selectionStart, selectionEnd, QuoteSpan::class.java)
        var exists = false

        for (span in spans) {
            text.removeSpan(span)
            exists = true
        }

        if (!exists) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                text.setSpan(QuoteSpan(context.resolveAttrColor(R.attr.colorAppAccent), stripWidth, bulletGap),
                             selectionStart, selectionEnd,
                             Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            } else {
                text.setSpan(QuoteSpan(context.resolveAttrColor(R.attr.colorAppAccent)),
                             selectionStart, selectionEnd,
                             Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }

    fun EditText.blur() {
        val spans: Array<MaskFilterSpan> = text.getSpans(selectionStart, selectionEnd, MaskFilterSpan::class.java)
        var exists = false

        for (span in spans) {
            text.removeSpan(span)
            exists = true
        }

        if (!exists) {
            val blurMaskFilter = BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.SOLID)
            text.setSpan(MaskFilterSpan(blurMaskFilter), selectionStart, selectionEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    fun EditText.alignCenter() {
        val spans: Array<AlignmentSpan> = text.getSpans(selectionStart, selectionEnd, AlignmentSpan::class.java)
        var exists = false

        for (span in spans) {
            text.removeSpan(span)
            exists = true
        }

        if (!exists) {
            text.setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), selectionStart, selectionEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    // ************************************************************ //

    fun EditText.fixBrokenSpans(sequence: CharSequence): SpannableStringBuilder {
        val spans: Array<Objects> = text.getSpans(selectionStart, selectionEnd, Objects::class.java)
        val spannableStringBuilder = SpannableStringBuilder(sequence)

        for (span in spans) {
            text.removeSpan(span)
            spannableStringBuilder.setSpan(span, 0, sequence.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        return spannableStringBuilder
    }
}