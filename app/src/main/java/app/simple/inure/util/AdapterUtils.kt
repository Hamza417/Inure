package app.simple.inure.util

import SideBySideDrawable
import android.content.pm.PackageInfo
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Typeface
import android.text.Spannable
import android.text.style.TextAppearanceSpan
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.toSpannable
import app.simple.inure.R
import app.simple.inure.apk.parsers.FOSSParser
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.singletons.TrackerTags
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
        setStrikeThru(packageInfo.applicationInfo.enabled)

        val isFOSS = FOSSParser.isPackageFOSS(packageInfo)
        val isTracking = TrackerTags.isPackageTracked(packageInfo.packageName)
        val isBloat = packageInfo.isPackageBloat()

        when {
            isFOSS -> {
                when {
                    isTracking -> {
                        when {
                            isBloat -> {
                                // Handle case where all three conditions are true
                                val d1 = SideBySideDrawable(context, R.drawable.ic_radiation_nuclear_12dp, R.drawable.ic_open_source_12dp)
                                val d2 = SideBySideDrawable(d1, ContextCompat.getDrawable(context, R.drawable.ic_recycling_12dp)!!)
                                val newColorFilter = PorterDuffColorFilter(AppearancePreferences.getAccentColor(), PorterDuff.Mode.SRC_IN)
                                d2.colorFilter = newColorFilter
                                setRightDrawable(d2)
                            }
                            else -> {
                                // Handle case where isFOSS and isTracking are true, but isBloat is false
                                val sideBySideDrawable = SideBySideDrawable(context, R.drawable.ic_radiation_nuclear_12dp, R.drawable.ic_open_source_12dp)
                                val newColorFilter = PorterDuffColorFilter(AppearancePreferences.getAccentColor(), PorterDuff.Mode.SRC_IN)
                                sideBySideDrawable.colorFilter = newColorFilter
                                setRightDrawable(sideBySideDrawable)
                            }
                        }
                    }
                    else -> {
                        when {
                            isBloat -> {
                                // Handle case where isFOSS and isBloat are true, but isTracking is false
                                val d1 = SideBySideDrawable(context, R.drawable.ic_open_source_12dp, R.drawable.ic_recycling_12dp)
                                val newColorFilter = PorterDuffColorFilter(AppearancePreferences.getAccentColor(), PorterDuff.Mode.SRC_IN)
                                d1.colorFilter = newColorFilter
                                setRightDrawable(d1)
                            }
                            else -> {
                                // Handle case where only isFOSS is true
                                setFOSSIcon(isFOSS = true)
                            }
                        }
                    }
                }
            }
            else -> {
                when {
                    isTracking -> {
                        when {
                            isBloat -> {
                                // Handle case where isTracking and isBloat are true, but isFOSS is false
                                val d1 = SideBySideDrawable(context, R.drawable.ic_radiation_nuclear_12dp, R.drawable.ic_recycling_12dp)
                                val newColorFilter = PorterDuffColorFilter(AppearancePreferences.getAccentColor(), PorterDuff.Mode.SRC_IN)
                                d1.colorFilter = newColorFilter
                                setRightDrawable(d1)
                            }
                            else -> {
                                // Handle case where only isTracking is true
                                setTrackingIcon(isTracker = true)
                            }
                        }
                    }
                    else -> {
                        when {
                            isBloat -> {
                                // Handle case where only isBloat is true
                                setBloatIcon(isBloat = true)
                            }
                            else -> {
                                // Handle case where all three conditions are false
                            }
                        }
                    }
                }
            }
        }
    }
}
