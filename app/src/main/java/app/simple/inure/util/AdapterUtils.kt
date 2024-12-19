package app.simple.inure.util

import android.content.pm.PackageInfo
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Typeface
import android.text.Spannable
import android.text.style.TextAppearanceSpan
import android.widget.TextView
import androidx.core.text.toSpannable
import app.simple.inure.R
import app.simple.inure.apk.parsers.FOSSParser
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.ui.SideBySideDrawable
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.singletons.TrackerTags
import app.simple.inure.util.NullSafety.isNull
import app.simple.inure.utils.DebloatUtils.isPackageBloat
import java.util.Locale

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

    fun TypeFaceTextView.setAppVisualStates(packageInfo: PackageInfo) {
        setStrikeThru(packageInfo.safeApplicationInfo.enabled)

        val isFOSS = FOSSParser.isPackageFOSS(packageInfo)
        val isTracking = TrackerTags.isPackageTracked(packageInfo.packageName)
        val isBloat = packageInfo.isPackageBloat()
        // val isStopped = packageInfo.isAppStopped()
        // val isLaunchable = PackageUtils.isAppLaunchable(context, packageInfo.packageName)

        val drawableList = mutableListOf<Int>()
        if (isFOSS) drawableList.add(R.drawable.ic_open_source_12dp)
        if (isTracking) drawableList.add(R.drawable.ic_radiation_nuclear_12dp)
        if (isBloat) drawableList.add(R.drawable.ic_recycling_12dp)
        // if (isStopped) drawableList.add(R.drawable.ic_stopped_12dp)
        // if (isLaunchable) drawableList.add(R.drawable.ic_launch_12dp)

        if (drawableList.isNotEmpty()) {
            var sideBySideDrawable: SideBySideDrawable? = null

            drawableList.forEach {
                sideBySideDrawable = if (sideBySideDrawable.isNull()) {
                    SideBySideDrawable(context, it)
                } else {
                    SideBySideDrawable(context, sideBySideDrawable!!, it)
                }
            }

            sideBySideDrawable?.let {
                it.colorFilter = PorterDuffColorFilter(AppearancePreferences.getAccentColor(), PorterDuff.Mode.SRC_IN)
                setRightDrawable(it)
            }
        } else {
            /* no-op */
        }
    }
}
