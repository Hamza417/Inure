package app.simple.inure.glide.util

import android.content.pm.ActivityInfo
import android.content.pm.ProviderInfo
import android.content.pm.ServiceInfo
import android.net.Uri
import android.widget.ImageView
import androidx.annotation.DrawableRes
import app.simple.inure.glide.activities.ActivityIconModel
import app.simple.inure.glide.apkIcon.ApkIcon
import app.simple.inure.glide.drawable.DrawableModel
import app.simple.inure.glide.graphics.AppGraphicsModel
import app.simple.inure.glide.icon.AppIcon
import app.simple.inure.glide.providers.ProviderIconModel
import app.simple.inure.glide.services.ServiceIconModel
import app.simple.inure.glide.svg.SVG
import app.simple.inure.glide.transformation.BlurShadow
import app.simple.inure.glide.transformation.Padding
import app.simple.inure.preferences.AppearancePreferences
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import java.io.File

object ImageLoader {
    /**
     * Loads app icon asynchronously
     *
     * @param packageName is package id of the app whose icon needs to be loaded
     */
    fun ImageView.loadAppIcon(packageName: String, enabled: Boolean, file: File? = null) {
        Glide.with(this)
            .asBitmap()
            .load(AppIcon(this.context, packageName, enabled, file))
            .into(this)
    }

    /**
     * Loads app icon asynchronously
     *
     * @param file
     */
    fun ImageView.loadAppIcon(file: File) {
        Glide.with(this)
            .asBitmap()
            .load(ApkIcon(this.context, file))
            .into(this)
    }

    /**
     * Loads app graphics asynchronously
     *
     * @param path - of the apk file or ApplicationInfo.sourceDir
     * @param filePath - path of the raster file inside the zip/apk file
     */
    fun ImageView.loadGraphics(path: String, filePath: String) {
        Glide.with(this)
            .asBitmap()
            .load(AppGraphicsModel(path, filePath))
            .into(this)
    }

    fun ImageView.loadIconFromActivityInfo(activityInfo: ActivityInfo) {
        Glide.with(this)
            .asBitmap()
            .load(ActivityIconModel(activityInfo, this.context))
            .into(this)
    }

    fun ImageView.loadIconFromServiceInfo(serviceInfo: ServiceInfo) {
        Glide.with(this.context)
            .asBitmap()
            .load(ServiceIconModel(serviceInfo, this.context))
            .into(this)
    }

    fun ImageView.loadIconFromProviderInfo(providerInfo: ProviderInfo) {
        Glide.with(this)
            .asBitmap()
            .load(ProviderIconModel(providerInfo, this.context))
            .into(this)
    }

    fun ImageView.loadAPKIcon(file: File) {
        Glide.with(this)
            .asBitmap()
            .load(ApkIcon(this.context, file))
            .into(this)
    }

    fun ImageView.loadAPKIcon(path: String) {
        Glide.with(this)
            .asBitmap()
            .load(ApkIcon(this.context, File(path)))
            .into(this)
    }

    fun ImageView.loadSvg(uri: Uri) {
        Glide.with(this)
            .asBitmap()
            .load(SVG(context, uri))
            .into(this)
    }

    /**
     * Loads drawable asynchronously
     */
    fun ImageView.loadDrawable(@DrawableRes res: Int) {
        Glide.with(this)
            .asBitmap()
            .transform(RoundedCorners(AppearancePreferences.getCornerRadius()
                                          .toInt().coerceAtLeast(1)),
                       Padding(BlurShadow.DEFAULT_SHADOW_SIZE.toInt()),
                       BlurShadow(context)
                           .setElevation(25F)
                           .setBlurRadius(BlurShadow.DEFAULT_SHADOW_SIZE))
            .load(DrawableModel(res, this.context))
            .into(this)
    }
}
