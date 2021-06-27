package app.simple.inure.glide.filedescriptorcover

import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.signature.ObjectKey
import java.io.InputStream

class DescriptorCoverLoader : ModelLoader<DescriptorCoverModel, InputStream> {
    override fun buildLoadData(descriptorCoverModel: DescriptorCoverModel, width: Int, height: Int, options: Options): ModelLoader.LoadData<InputStream> {
        return ModelLoader.LoadData(ObjectKey(descriptorCoverModel), DescriptorCoverFetcher(descriptorCoverModel))
    }

    override fun handles(model: DescriptorCoverModel): Boolean {
        return true
    }

    internal class Factory : ModelLoaderFactory<DescriptorCoverModel, InputStream> {
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<DescriptorCoverModel, InputStream> {
            return DescriptorCoverLoader()
        }

        override fun teardown() {}
    }
}