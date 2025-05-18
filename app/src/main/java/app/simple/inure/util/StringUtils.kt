package app.simple.inure.util

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.core.text.toSpannable
import app.simple.inure.constants.Extensions
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.themes.manager.ThemeManager
import java.io.InputStream
import java.nio.charset.Charset
import java.util.Locale

object StringUtils {

    fun emptyString(): String {
        return ""
    }

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

    fun String.containsAny(chars: CharArray): Boolean {
        for (char in chars) {
            if (this.contains(char)) {
                return true
            }
        }

        return false
    }

    /**
     * This function is solely used for coloring the path
     * strings in the format of a/y/z and the last index
     * of "/" is used.
     *
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

    fun CharSequence.optimizeToColoredString(lookupIndex: String): Spannable {
        return this.toString().optimizeToColoredString(lookupIndex)
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
    fun String.highlightExtensions(isHighlighted: Boolean): Spannable {
        if (isHighlighted) {
            kotlin.runCatching {
                val spannable: Spannable = SpannableString(this)
                spannable.setSpan(ForegroundColorSpan(AppearancePreferences.getAccentColor()),
                                  this.lastIndexOf("."),
                                  this.length,
                                  Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                return spannable
            }.getOrElse {
                return this.toSpannable()
            }
        } else {
            return this.toSpannable()
        }
    }

    @Deprecated("This require hardcoding for every extension, use app accent color instead.")
    private fun getExtensionHardcodedColors(path: String): Int {
        kotlin.runCatching {
            return Color.parseColor(Extensions.imageExtensionColors[path.substring(path.lastIndexOf(".") + 1)])
        }.onFailure {
            return Color.parseColor(Extensions.nonImageFileExtensionColors[path.substring(path.lastIndexOf(".") + 1)])
        }

        return AppearancePreferences.getAccentColor()
    }

    @Suppress("unused")
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

    /**
     * Convert [InputStream] object to [String] data
     */
    fun InputStream.readTextSafely(charset: Charset = Charsets.UTF_8): String {
        return this.bufferedReader(charset).use {
            it.readText()
        }
    }

    /**
     * Check if a string ends with any of the given strings.
     * @param strings The strings to check.
     * @param ignoreCase Whether to ignore case when comparing strings.
     * @return True if the string ends with any of the given strings.
     * @see String.endsWith
     * @see String.endsWithAny
     */
    fun String.endsWithAny(vararg strings: String, ignoreCase: Boolean = true): Boolean {
        for (string in strings) {
            if (endsWith(string, ignoreCase)) {
                return true
            }
        }
        return false
    }

    fun StringBuilder.appendLineSafely(string: String) {
        if (isNotEmpty()) {
            append("\n$string")
        } else {
            append(string)
        }
    }

    fun StringBuilder.appendFlag(string: String?) {
        if (isNullOrEmpty()) {
            append(string)
        } else {
            append(" | $string")
        }
    }

    fun String.fetchLinks(): List<String> {
        val links = mutableListOf<String>()
        val regex = Regex("""\b(?:https?|ftp)://\S+\b""")
        val matcher = regex.findAll(this)

        for (match in matcher) {
            links.add(match.value)
        }

        return links
    }

    fun String.emptyToString(string: String): String {
        return this.ifEmpty {
            string
        }
    }

    fun String?.whenEmptyOrNull(default: String): String {
        return this?.ifEmpty { default } ?: default
    }
}
