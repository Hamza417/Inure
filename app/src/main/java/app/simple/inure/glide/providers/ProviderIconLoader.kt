package app.simple.inure.glide.providers

import android.graphics.Bitmap
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.signature.ObjectKey

class ProviderIconLoader : ModelLoader<ProviderIconModel, Bitmap> {
    override fun buildLoadData(model: ProviderIconModel, width: Int, height: Int, options: Options): ModelLoader.LoadData<Bitmap> {
        return ModelLoader.LoadData(ObjectKey(model), ProviderIconFetcher(model))
    }

    override fun handles(model: ProviderIconModel): Boolean {
        return true
    }

    internal class Factory : ModelLoaderFactory<ProviderIconModel, Bitmap> {
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<ProviderIconModel, Bitmap> {
            return ProviderIconLoader()
        }

        override fun teardown() {}
    }
}