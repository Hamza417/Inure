package app.simple.inure.glide.modules

import android.content.Context
import app.simple.inure.glide.svg.SVG
import app.simple.inure.glide.svg.SVGLoader
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.LibraryGlideModule
import java.io.InputStream

@GlideModule
class SvgImageModule : LibraryGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.append(SVG::class.java, InputStream::class.java, SVGLoader.Factory())
    }
}