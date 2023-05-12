package app.simple.inure.glide.uricover

import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.signature.ObjectKey
import java.io.InputStream

class UriCoverLoader : ModelLoader<UriCoverModel, InputStream> {
    override fun buildLoadData(uriCoverModel: UriCoverModel, width: Int, height: Int, options: Options): ModelLoader.LoadData<InputStream> {
        return ModelLoader.LoadData(ObjectKey(uriCoverModel), UriCoverFetcher(uriCoverModel))
    }

    fun getResourceFetcher(model: UriCoverModel): DataFetcher<InputStream> {
        return UriCoverFetcher(model)
    }

    override fun handles(model: UriCoverModel): Boolean {
        return true
    }

    internal class Factory : ModelLoaderFactory<UriCoverModel, InputStream> {
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<UriCoverModel, InputStream> {
            return UriCoverLoader()
        }

        override fun teardown() {}
    }
}