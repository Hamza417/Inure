package app.simple.inure.glide.util

import android.content.Context
import android.widget.ImageView
import app.simple.inure.decorations.views.IconView
import app.simple.inure.glide.graphics.AppGraphicsModel
import app.simple.inure.glide.icon.AppIcon
import app.simple.inure.glide.modules.GlideApp
import org.jetbrains.annotations.NotNull
import java.nio.file.Path

object ImageLoader {
    /**
     * Loads app icon asynchronously
     *
     * @param context context of the given environment
     * @param packageName is package id of the app whose icon needs to be loaded
     */
    fun ImageView.loadAppIcon(@NotNull context: Context, packageName: String) {
        GlideApp.with(this)
                .asBitmap()
                .load(AppIcon(context, packageName))
                .into(this)
    }

    /**
     * Loads app graphics asynchronously
     *
     * @param path - of the apk file or ApplicationInfo.sourceDir
     * @param filePath - path of the raster file inside the zip/apk file
     */
    fun ImageView.loadGraphics(@NotNull path: String, filePath: String) {
        GlideApp.with(this)
                .asBitmap()
                .load(AppGraphicsModel(path, filePath))
                .into(this)
    }
}