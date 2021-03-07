package app.simple.inure.glide.util

import android.content.Context
import android.widget.ImageView
import app.simple.inure.glide.icon.AppIcon
import app.simple.inure.glide.modules.GlideApp
import org.jetbrains.annotations.NotNull

object AppIconExtensions {
    /**
     * Loads app icon asynchronously
     *
     * @param context context of the given environment
     * @param packageName is package id of the app whose icon needs to be loaded
     */
    fun ImageView.loadAppIcon(@NotNull context: Context, packageName: String) {
        GlideApp.with(this).load(AppIcon(context, packageName)).into(this)
    }
}