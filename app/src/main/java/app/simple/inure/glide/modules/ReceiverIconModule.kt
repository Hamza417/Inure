package app.simple.inure.glide.modules

import android.content.Context
import android.graphics.Bitmap
import app.simple.inure.glide.receivers.ReceiverIconLoader
import app.simple.inure.glide.receivers.ReceiverIconModel
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.LibraryGlideModule

@GlideModule
class ReceiverIconModule : LibraryGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.append(ReceiverIconModel::class.java, Bitmap::class.java, ReceiverIconLoader.Factory())
    }
}