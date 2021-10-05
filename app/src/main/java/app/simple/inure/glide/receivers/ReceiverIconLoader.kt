package app.simple.inure.glide.receivers

import android.graphics.Bitmap
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.signature.ObjectKey

class ReceiverIconLoader : ModelLoader<ReceiverIconModel, Bitmap> {
    override fun buildLoadData(model: ReceiverIconModel, width: Int, height: Int, options: Options): ModelLoader.LoadData<Bitmap> {
        return ModelLoader.LoadData(ObjectKey(model), ReceiverDrawableFetcher(model))
    }

    override fun handles(model: ReceiverIconModel): Boolean {
        return true
    }

    internal class Factory : ModelLoaderFactory<ReceiverIconModel, Bitmap> {
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<ReceiverIconModel, Bitmap> {
            return ReceiverIconLoader()
        }

        override fun teardown() {}
    }
}