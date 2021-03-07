package app.simple.inure.glide.icon

import android.graphics.drawable.Drawable
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.signature.ObjectKey

class AppIconLoader : ModelLoader<AppIcon, Drawable> {
    override fun buildLoadData(appIcon: AppIcon, width: Int, height: Int, options: Options): ModelLoader.LoadData<Drawable>? {
        return ModelLoader.LoadData(ObjectKey(appIcon), AppIconFetcher(appIcon))
    }

    override fun handles(model: AppIcon): Boolean {
        return true
    }

    internal class Factory : ModelLoaderFactory<AppIcon, Drawable> {
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<AppIcon, Drawable> {
            return AppIconLoader()
        }

        override fun teardown() {
            /* no-op */
        }
    }
}