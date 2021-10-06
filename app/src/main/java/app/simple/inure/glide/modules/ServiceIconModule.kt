package app.simple.inure.glide.modules

import android.content.Context
import android.graphics.Bitmap
import app.simple.inure.glide.services.ServiceIconLoader
import app.simple.inure.glide.services.ServiceIconModel
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.LibraryGlideModule

@GlideModule
class ServiceIconModule : LibraryGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.append(ServiceIconModel::class.java, Bitmap::class.java, ServiceIconLoader.Factory())
    }
}