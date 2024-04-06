package app.simple.inure.glide.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import app.simple.inure.R
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.util.BitmapHelper.addLinearGradient
import app.simple.inure.util.BitmapHelper.toBitmap
import app.simple.inure.util.BitmapHelper.toInputStream
import app.simple.inure.util.ColorUtils
import java.io.InputStream

object GlideUtils {

    private val colorArray = intArrayOf(AppearancePreferences.getAccentColor(),
                                        ColorUtils.changeAlpha(AppearancePreferences.getAccentColor(), 0.75f))

    fun Context.getGeneratedAppIconBitmap(): Bitmap? {
        R.drawable.ic_app_icon.toBitmap(this).addLinearGradient(colorArray).toInputStream().use {
            return BitmapFactory.decodeStream(it)
        }
    }

    fun Context.getGeneratedAppIconStream(): InputStream {
        R.drawable.ic_app_icon.toBitmap(this).addLinearGradient(colorArray).toInputStream().use {
            return it
        }
    }
}
