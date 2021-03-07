package app.simple.inure.glide.modules

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import app.simple.inure.R
import app.simple.inure.glide.icon.AppIcon
import app.simple.inure.glide.icon.AppIconLoader
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions

@GlideModule
class AppIconModule : AppGlideModule() {
    override fun isManifestParsingEnabled(): Boolean {
        return false
    }

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setDefaultTransitionOptions(Drawable::class.java, DrawableTransitionOptions.withCrossFade())
        builder.setDefaultRequestOptions(
                RequestOptions()
                        .format(DecodeFormat.PREFER_ARGB_8888)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .fallback(R.drawable.ic_app_icon)
                        .error(R.drawable.ic_app_icon)
        )
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.append(AppIcon::class.java, Drawable::class.java, AppIconLoader.Factory())
    }
}