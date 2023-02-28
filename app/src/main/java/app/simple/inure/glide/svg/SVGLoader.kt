package app.simple.inure.glide.svg

import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.signature.ObjectKey
import java.io.InputStream

class SVGLoader : ModelLoader<SVG, InputStream> {
    override fun buildLoadData(model: SVG, width: Int, height: Int, options: Options): ModelLoader.LoadData<InputStream> {
        return ModelLoader.LoadData(ObjectKey(model), SVGFetcher(model))
    }

    override fun handles(model: SVG): Boolean {
        return true
    }

    internal class Factory : ModelLoaderFactory<SVG, InputStream> {
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<SVG, InputStream> {
            return SVGLoader()
        }

        override fun teardown() {
            /* no-op */
        }
    }
}