package app.simple.inure.util

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat
import androidx.core.text.toSpannable
import app.simple.inure.R
import java.util.*

object StringUtils {
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
    fun String.optimizeToColoredString(context: Context, lookupIndex: String): Spannable {
        kotlin.runCatching {
            val spannable: Spannable = SpannableString(this)
            spannable.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.textSecondary)),
                              0,
                              this.lastIndexOf(lookupIndex),
                              Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            return spannable
        }.getOrElse {
            return this.toSpannable()
        }
    }
}