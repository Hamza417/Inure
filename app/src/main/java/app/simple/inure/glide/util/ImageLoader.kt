package app.simple.inure.glide.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import app.simple.inure.glide.graphics.AppGraphicsModel
import app.simple.inure.glide.icon.AppIcon
import app.simple.inure.glide.modules.GlideApp
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.ImageViewState
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import org.jetbrains.annotations.NotNull

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

    fun SubsamplingScaleImageView.loadGraphics(context: Context, @NotNull path: String, filePath: String) {
        Glide.with(context)
                .asBitmap()
                .dontTransform()
                .dontAnimate()
                .load(AppGraphicsModel(path, filePath))
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        this@loadGraphics.setImage(ImageSource.bitmap(resource).apply {
                            tilingEnabled()
                        })
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        /* no-op */
                    }
                })
    }

    fun SubsamplingScaleImageView.loadGraphics(context: Context, @NotNull path: String, filePath: String, state: ImageViewState) {
        Glide.with(context)
                .asBitmap()
                .dontTransform()
                .dontAnimate()
                .load(AppGraphicsModel(path, filePath))
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        this@loadGraphics.setImage(ImageSource.bitmap(resource).apply {
                            tilingEnabled()
                        }, state)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        /* no-op */
                    }
                })
    }
}