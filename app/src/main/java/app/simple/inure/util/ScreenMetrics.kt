package app.simple.inure.util

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics
import android.util.Size
import android.view.WindowManager
import android.view.WindowMetrics
import app.simple.inure.R

object ScreenMetrics {

    /**
     * Returns screen size in pixels.
     */
    @Suppress("DEPRECATION")
    fun getScreenSize(context: Context): Size {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics: WindowMetrics = context.getSystemService(WindowManager::class.java).currentWindowMetrics
            Size(metrics.bounds.width(), metrics.bounds.height())
        } else {
            val display = context.getSystemService(WindowManager::class.java).defaultDisplay
            val metrics = if (display != null) {
                DisplayMetrics().also { display.getRealMetrics(it) }
            } else {
                Resources.getSystem().displayMetrics
            }
            Size(metrics.widthPixels, metrics.heightPixels)
        }
    }

    fun getScreenDensity(context: Context): String {
        return when (context.resources.displayMetrics.densityDpi) {
            DisplayMetrics.DENSITY_LOW -> {
                "ldpi"
            }
            DisplayMetrics.DENSITY_140 -> {
                "ldpi - mdpi"
            }
            DisplayMetrics.DENSITY_MEDIUM -> {
                "mdpi"
            }
            DisplayMetrics.DENSITY_180,
            DisplayMetrics.DENSITY_200,
            DisplayMetrics.DENSITY_220 -> {
                "mdpi - hdpi"
            }
            DisplayMetrics.DENSITY_HIGH -> {
                "hdpi"
            }
            DisplayMetrics.DENSITY_260,
            DisplayMetrics.DENSITY_280,
            DisplayMetrics.DENSITY_300 -> {
                "hdpi - xhdpi"
            }
            DisplayMetrics.DENSITY_XHIGH -> {
                "xhdpi"
            }
            DisplayMetrics.DENSITY_340,
            DisplayMetrics.DENSITY_360,
            DisplayMetrics.DENSITY_400,
            DisplayMetrics.DENSITY_420,
            DisplayMetrics.DENSITY_440 -> {
                "xhdpi - xxhdpi"
            }
            DisplayMetrics.DENSITY_XXHIGH -> {
                "xxhdpi"
            }
            DisplayMetrics.DENSITY_560,
            DisplayMetrics.DENSITY_600 -> {
                "xxhdpi - xxxhdpi"
            }
            DisplayMetrics.DENSITY_XXXHIGH -> {
                "xxxhdpi"
            }
            DisplayMetrics.DENSITY_TV -> {
                "tvdpi"
            }
            else -> context.getString(R.string.unknown)
        }
    }

    /**
     * Classify screen into three main classes: large, normal, small
     */
    fun getScreenClass(context: Context): String {
        return when (context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) {
            Configuration.SCREENLAYOUT_SIZE_LARGE -> context.resources.getString(R.string.large)
            Configuration.SCREENLAYOUT_SIZE_NORMAL -> context.resources.getString(R.string.normal)
            Configuration.SCREENLAYOUT_SIZE_SMALL -> context.resources.getString(R.string.small)
            Configuration.SCREENLAYOUT_SIZE_XLARGE -> context.resources.getString(R.string.extra_large)
            else -> context.resources.getString(R.string.unknown)
        }
    }

    fun getRefreshRate(context: Context): Float {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // TODO - fix display context for android 11
            @Suppress("deprecation")
            context.getSystemService(WindowManager::class.java).defaultDisplay.refreshRate
        } else {
            @Suppress("deprecation")
            context.getSystemService(WindowManager::class.java).defaultDisplay.refreshRate
        }
    }

    fun getOrientation(context: Context): String {
        return when (context.resources.configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> context.resources.getString(R.string.landscape)
            Configuration.ORIENTATION_PORTRAIT -> context.resources.getString(R.string.portrait)
            else -> context.resources.getString(R.string.unknown)
        }
    }
}