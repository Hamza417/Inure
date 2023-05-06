package app.simple.inure.text

import android.graphics.BlurMaskFilter
import android.graphics.Typeface
import android.os.Build
import android.text.Layout
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.*
import android.widget.EditText
import app.simple.inure.preferences.AppearancePreferences
import java.util.*
import kotlin.math.roundToInt

object EditTextHelper {

    private const val bulletGap = 16
    private const val bulletRadius = 8
    private const val stripWidth = 6
    private const val blurRadius = 5f
    private const val spanUpperThreshold = 96
    private const val spanLowerThreshold = 12

    private var leftSpace: Int = 0
    private var rightSpace: Int = 0
    private var cursorPosition: Int = 0

    fun EditText.toBold() {
        selectTheCurrentWord()

        val spans: Array<StyleSpan> = text.getSpans(leftSpace, rightSpace, StyleSpan::class.java)
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

        // Remove the selection
        setSelection(cursorPosition, cursorPosition)
    }

    fun EditText.toItalics() {
        selectTheCurrentWord()

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

        // Remove the selection
        setSelection(cursorPosition, cursorPosition)
    }

    fun EditText.toUnderline() {
        selectTheCurrentWord()

        val spans: Array<UnderlineSpan> = text.getSpans(selectionStart, selectionEnd, UnderlineSpan::class.java)
        var exists = false

        for (span in spans) {
            text.removeSpan(span)
            exists = true
        }

        if (!exists) {
            text.setSpan(UnderlineSpan(), selectionStart, selectionEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        // Remove the selection
        setSelection(cursorPosition, cursorPosition)
    }

    fun EditText.toStrikethrough() {
        selectTheCurrentWord()

        val spans: Array<StrikethroughSpan> = text.getSpans(selectionStart, selectionEnd, StrikethroughSpan::class.java)
        var exists = false

        for (span in spans) {
            text.removeSpan(span)
            exists = true
        }

        if (!exists) {
            text.setSpan(StrikethroughSpan(), selectionStart, selectionEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        // Remove the selection
        setSelection(cursorPosition, cursorPosition)
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
                text.setSpan(BulletSpan(bulletGap, AppearancePreferences.getAccentColor(), bulletRadius),
                             selectionStart, selectionEnd,
                             Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            } else {
                text.setSpan(BulletSpan(bulletGap, AppearancePreferences.getAccentColor()),
                             selectionStart, selectionEnd,
                             Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }

    fun EditText.toSuperscript() {
        selectTheCurrentWord()

        val spans: Array<SuperscriptSpan> = text.getSpans(selectionStart, selectionEnd, SuperscriptSpan::class.java)
        var exists = false

        for (span in spans) {
            text.removeSpan(span)
            exists = true
        }

        if (!exists) {
            text.setSpan(SuperscriptSpan(), selectionStart, selectionEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        // Remove the selection
        setSelection(cursorPosition, cursorPosition)
    }

    fun EditText.toSubscript() {
        selectTheCurrentWord()

        val spans: Array<SubscriptSpan> = text.getSpans(selectionStart, selectionEnd, SubscriptSpan::class.java)
        var exists = false

        for (span in spans) {
            text.removeSpan(span)
            exists = true
        }

        if (!exists) {
            text.setSpan(SubscriptSpan(), selectionStart, selectionEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        // Remove the selection
        setSelection(cursorPosition, cursorPosition)
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

        text.setSpan(AbsoluteSizeSpan(fontSize), selectionStart, selectionEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
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

        text.setSpan(AbsoluteSizeSpan(fontSize), selectionStart, selectionEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    fun EditText.highlightText(color: Int) {
        selectTheCurrentWord()

        val spans: Array<BackgroundColorSpan> = text.getSpans(selectionStart, selectionEnd, BackgroundColorSpan::class.java)
        var exists = false

        for (span in spans) {
            text.removeSpan(span)
            exists = true
        }

        if (!exists) {
            text.setSpan(BackgroundColorSpan(color), selectionStart, selectionEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        // Remove the selection
        setSelection(cursorPosition, cursorPosition)
    }

    fun EditText.toQuote() {
        wrapTheSentenceInQuotes()
        selectTheCurrentSentence()

        val spans: Array<QuoteSpan> = text.getSpans(selectionStart, selectionEnd, QuoteSpan::class.java)
        var exists = false

        for (span in spans) {
            text.removeSpan(span)
            exists = true
        }

        if (!exists) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                text.setSpan(QuoteSpan(AppearancePreferences.getAccentColor(), stripWidth, bulletGap),
                             selectionStart, selectionEnd,
                             Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            } else {
                text.setSpan(QuoteSpan(AppearancePreferences.getAccentColor()),
                             selectionStart, selectionEnd,
                             Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

        // Remove the selection
        setSelection(cursorPosition, cursorPosition)
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
            text.setSpan(MaskFilterSpan(blurMaskFilter), selectionStart, selectionEnd, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
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
            text.setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), selectionStart, selectionEnd, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        }
    }

    // ************************************************************ //

    fun EditText.fixBrokenSpans(sequence: CharSequence): SpannableStringBuilder {
        val spans: Array<Objects> = text.getSpans(selectionStart, selectionEnd, Objects::class.java)
        val spannableStringBuilder = SpannableStringBuilder(sequence)

        for (span in spans) {
            text.removeSpan(span)
            spannableStringBuilder.setSpan(span, 0, sequence.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        }

        return spannableStringBuilder
    }

    fun EditText.selectTheCurrentWord() {
        cursorPosition = selectionStart

        /**
         * Find the first space on the left side of the cursor
         */
        leftSpace = selectionStart

        if (if (leftSpace > 0) text[leftSpace - 1] != ' ' else false) {
            while (leftSpace > 0 && text[leftSpace - 1] != ' ') {
                leftSpace--
            }
        }

        /**
         * Find the first space on the right side of the cursor
         */
        rightSpace = selectionEnd

        if (if (rightSpace < text.length) text[rightSpace] != ' ' else false) {
            while (rightSpace < text.length && text[rightSpace] != ' ') {
                rightSpace++
            }
        }

        // Select the word
        setSelection(leftSpace, rightSpace)
    }

    fun EditText.selectTheCurrentSentence() {
        cursorPosition = selectionStart

        /**
         * Find the first space on the left side of the cursor
         */
        leftSpace = selectionStart

        /**
         * If the cursor is not at the beginning of the text
         * and the character on the left side of the cursor is not a period
         * then find the first period on the left side of the cursor
         * and set the left space to the character after the period.
         */
        if (if (leftSpace > 0) text[leftSpace - 1] != '.' else false) {
            while (leftSpace > 0 && text[leftSpace - 1] != '.') {
                leftSpace--
            }

            leftSpace++
        }

        /**
         * Find the first space on the right side of the cursor
         */
        rightSpace = selectionEnd

        /**
         * If the cursor is not at the end of the text
         * and the character on the right side of the cursor is not a period
         * then find the first period on the right side of the cursor
         * and set the right space to the character before the period.
         */
        if (if (rightSpace < text.length) text[rightSpace] != '.' else false) {
            while (rightSpace < text.length && text[rightSpace] != '.') {
                rightSpace++
            }

            rightSpace--
        }

        // Select the sentence
        setSelection(leftSpace, rightSpace)
    }

    fun EditText.wrapTheSentenceInQuotes() {
        cursorPosition = selectionStart

        /**
         * Find the first space on the left side of the cursor
         */
        leftSpace = selectionStart

        /**
         * If the cursor is not at the beginning of the text
         * and the character on the left side of the cursor is not a period
         * then find the first period on the left side of the cursor
         * and set the left space to the character after the period.
         *
         * Also check if it's not a quote, if it is then set the left
         * space to the character after the quote.
         */
        if (if (leftSpace > 0) text[leftSpace - 1] != '.' else false) {
            while (leftSpace > 0 && text[leftSpace - 1] != '.' && text[leftSpace - 1] != '"') {
                leftSpace--
            }

            try {
                if (text[leftSpace - 1] == '"') {
                    leftSpace++
                } else {
                    leftSpace++
                }
            } catch (e: java.lang.IndexOutOfBoundsException) {
                leftSpace++
            }
        }

        /**
         * Find the first space on the right side of the cursor
         */
        rightSpace = selectionEnd

        /**
         * If the cursor is not at the end of the text
         * and the character on the right side of the cursor is not a period
         * then find the first period on the right side of the cursor
         * and set the right space to the character before the period.
         *
         * Also check if it's not a quote, if it is then set the right
         * space to the character before the quote.
         */
        if (if (rightSpace < text.length) text[rightSpace] != '.' else false) {
            while (rightSpace < text.length && text[rightSpace] != '.' && text[rightSpace] != '"') {
                rightSpace++
            }

            try {
                if (text[rightSpace] == '"') {
                    rightSpace--
                } else {
                    rightSpace--
                }
            } catch (e: java.lang.IndexOutOfBoundsException) {
                rightSpace--
            }
        }

        // Remove the quotes if the sentence is already wrapped in quotes
        try {
            if (text[leftSpace - 1] == '"' && text[rightSpace + 1] == '"') {
                text.delete(leftSpace - 1, leftSpace)
                text.delete(rightSpace, rightSpace + 1)

                // Remove the selection
                setSelection(rightSpace - 1, rightSpace - 1)

                // Move the cursor to the last position
                setSelection(cursorPosition - 1, cursorPosition - 1)

                return
            }
        } catch (e: java.lang.IndexOutOfBoundsException) {
            return
        }

        // Select the sentence
        setSelection(leftSpace, rightSpace)

        // Wrap the sentence in quotes
        text.insert(leftSpace - 1, "\"")
        text.insert(rightSpace + 1, "\"")

        // Remove the selection
        setSelection(rightSpace + 2, rightSpace + 2)

        // Move the cursor to the last position
        setSelection(cursorPosition + 2, cursorPosition + 2)
    }
}