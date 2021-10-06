package app.simple.inure.glide.services

import android.graphics.Bitmap
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.signature.ObjectKey

class ServiceIconLoader : ModelLoader<ServiceIconModel, Bitmap> {
    override fun buildLoadData(model: ServiceIconModel, width: Int, height: Int, options: Options): ModelLoader.LoadData<Bitmap> {
        return ModelLoader.LoadData(ObjectKey(model), ServiceIconFetcher(model))
    }

    override fun handles(model: ServiceIconModel): Boolean {
        return true
    }

    internal class Factory : ModelLoaderFactory<ServiceIconModel, Bitmap> {
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<ServiceIconModel, Bitmap> {
            return ServiceIconLoader()
        }

        override fun teardown() {}
    }
}