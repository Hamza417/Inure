package app.simple.inure.glide.modules

import android.content.Context
import app.simple.inure.glide.uricover.UriCoverLoader
import app.simple.inure.glide.uricover.UriCoverModel
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.LibraryGlideModule
import java.io.InputStream

@GlideModule
class UriCoverModule : LibraryGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.append(UriCoverModel::class.java, InputStream::class.java, UriCoverLoader.Factory())
    }
}