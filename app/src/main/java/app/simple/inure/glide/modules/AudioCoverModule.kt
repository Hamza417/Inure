package app.simple.inure.glide.modules

import android.content.Context
import app.simple.inure.glide.filedescriptorcover.DescriptorCoverLoader
import app.simple.inure.glide.filedescriptorcover.DescriptorCoverModel
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.LibraryGlideModule
import java.io.InputStream

@GlideModule
class AudioCoverModule : LibraryGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.append(DescriptorCoverModel::class.java, InputStream::class.java, DescriptorCoverLoader.Factory())
    }
}