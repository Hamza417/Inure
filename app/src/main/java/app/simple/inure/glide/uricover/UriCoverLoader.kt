package app.simple.inure.glide.uricover

import android.graphics.Bitmap
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.signature.ObjectKey

class UriCoverLoader : ModelLoader<UriCoverModel, Bitmap> {
    override fun buildLoadData(uriCoverModel: UriCoverModel, width: Int, height: Int, options: Options): ModelLoader.LoadData<Bitmap> {
        return ModelLoader.LoadData(ObjectKey(uriCoverModel), UriCoverFetcher(uriCoverModel))
    }

    fun getResourceFetcher(model: UriCoverModel): DataFetcher<Bitmap> {
        return UriCoverFetcher(model)
    }

    override fun handles(model: UriCoverModel): Boolean {
        return true
    }

    internal class Factory : ModelLoaderFactory<UriCoverModel, Bitmap> {
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<UriCoverModel, Bitmap> {
            return UriCoverLoader()
        }

        override fun teardown() {}
    }
}