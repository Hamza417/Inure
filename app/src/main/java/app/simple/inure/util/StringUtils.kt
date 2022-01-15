package app.simple.inure.util

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.core.text.toSpannable
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.themes.manager.ThemeManager
import java.util.*

object StringUtils {

    /**
     * Capitalized the first letter of any [String]
     */
    fun String.capitalizeFirstLetter(): String {
        return try {
            this.substring(0, 1).uppercase(Locale.ROOT) + this.substring(1)
        } catch (e: IndexOutOfBoundsException) {
            this
        }
    }

    /**
     * This function is solely used for coloring the path
     * strings in the format of a/y/z and the last index
     * of "/" is used.
     *
     * @param context used for fetching text color resource
     * @param lookupIndex [String] that needs to be looked for
     *                    conversion
     *
     * In case the string does not contain any slashes or is
     * null string or anything. This will return
     * back the normal [Spannable] string.
     */
    fun String.optimizeToColoredString(lookupIndex: String): Spannable {
        kotlin.runCatching {
            val spannable: Spannable = SpannableString(this)
            spannable.setSpan(ForegroundColorSpan(ThemeManager.theme.textViewTheme.secondaryTextColor),
                              0,
                              this.lastIndexOf(lookupIndex),
                              Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            return spannable
        }.getOrElse {
            return this.toSpannable()
        }
    }

    /**
     * Change string color to app's accent color.
     *
     * @return [Spannable]
     */
    fun String.applyAccentColor(): Spannable {
        val spannable: Spannable = SpannableString(this)
        spannable.setSpan(ForegroundColorSpan(AppearancePreferences.getAccentColor()),
                          0,
                          this.length,
                          Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        return spannable
    }

    /**
     * Change string color to app's accent color.
     *
     * @return [Spannable]
     */
    fun String.applySecondaryTextColor(): Spannable {
        val spannable: Spannable = SpannableString(this)
        spannable.setSpan(ForegroundColorSpan(ThemeManager.theme.textViewTheme.secondaryTextColor),
                          0,
                          this.length,
                          Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannable
    }

    /**
     * Highlights the specified extension with a color
     * to make them and easily recognizable in a longer
     * lists
     *
     * String should be a path containing proper extension
     * in a expected format like this <path>.extension else
     * the default string will be returned without any
     * spanning.
     *
     * @return [Spannable]
     */
    fun Spannable.highlightExtensions(): Spannable {
        kotlin.runCatching {
            val spannable: Spannable = SpannableString(this)
            spannable.setSpan(ForegroundColorSpan(getExtensionHardcodedColors(this.toString())),
                              this.lastIndexOf("."),
                              this.length,
                              Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            return spannable
        }.getOrElse {
            return this.toSpannable()
        }
    }

    private fun getExtensionHardcodedColors(path: String): Int {
        return when {
            path.endsWith(".json") -> Color.parseColor("#3498db")
            path.endsWith(".css") -> Color.parseColor("#af7ac5")
            path.endsWith(".html") -> Color.parseColor("#48c9b0")
            path.endsWith(".properties") -> Color.parseColor("#f4d03f")
            path.endsWith(".js") -> Color.parseColor("#99a3a4")
            path.endsWith(".tsv") -> Color.parseColor("#af7ac5")
            path.endsWith(".txt") -> Color.parseColor("#D35400")
            path.endsWith(".proto") -> Color.parseColor("#e59866")
            path.endsWith(".java") -> Color.parseColor("#e74c3c")
            path.endsWith(".bin") -> Color.parseColor("#28b463")
            path.endsWith(".ttf") -> Color.parseColor("#1f618d")
            path.endsWith(".md") -> Color.parseColor("#2e4053")
            path.endsWith(".pdf") -> Color.parseColor("#b03a2e")
            path.endsWith(".svg") -> Color.parseColor("#45b39d")
            path.endsWith(".png") -> Color.parseColor("#F5B041")
            path.endsWith(".jpg") -> Color.parseColor("#e67e22")
            path.endsWith(".jpeg") -> Color.parseColor("#707b7c")
            path.endsWith(".gif") -> Color.parseColor("#808b96")
            path.endsWith(".webp") -> Color.parseColor("#196f3d")
            path.endsWith(".ini") -> Color.parseColor("#d68910")
            path.endsWith(".version") -> Color.parseColor("#05b4c1")
            else -> Color.BLACK
        }
    }

    fun checkStringBuilderEnd(builder: StringBuilder) {
        val length = builder.length
        if (length > 2) builder.delete(builder.length - 2, builder.length)
    }

    fun StringBuilder.createString(string: String) {
        if (isNotEmpty()) {
            append(" | $string")
        } else {
            append(string)
        }
    }
}