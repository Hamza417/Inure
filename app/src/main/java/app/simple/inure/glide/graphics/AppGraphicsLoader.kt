package app.simple.inure.glide.graphics

import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.signature.ObjectKey
import java.io.InputStream

class AppGraphicsLoader : ModelLoader<AppGraphicsModel, InputStream> {
    override fun buildLoadData(model: AppGraphicsModel, width: Int, height: Int, options: Options): ModelLoader.LoadData<InputStream> {
        return ModelLoader.LoadData(ObjectKey(model), AppGraphicsFetcher(model))
    }

    override fun handles(model: AppGraphicsModel): Boolean {
        return true
    }

    internal class Factory : ModelLoaderFactory<AppGraphicsModel, InputStream> {
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<AppGraphicsModel, InputStream> {
            return AppGraphicsLoader()
        }

        override fun teardown() {}
    }
}