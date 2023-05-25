package app.simple.inure.glide.drawable

import android.graphics.Bitmap
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.signature.ObjectKey

class DrawableLoader : ModelLoader<DrawableModel, Bitmap> {
    override fun buildLoadData(model: DrawableModel, width: Int, height: Int, options: Options): ModelLoader.LoadData<Bitmap>? {
        return ModelLoader.LoadData(ObjectKey(model), DrawableFetcher(model))
    }

    override fun handles(model: DrawableModel): Boolean {
        return true
    }

    internal class Factory : ModelLoaderFactory<DrawableModel, Bitmap> {
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<DrawableModel, Bitmap> {
            return DrawableLoader()
        }

        override fun teardown() {}
    }
}