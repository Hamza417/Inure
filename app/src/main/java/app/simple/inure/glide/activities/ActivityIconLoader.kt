package app.simple.inure.glide.activities

import android.graphics.Bitmap
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.signature.ObjectKey

class ActivityIconLoader : ModelLoader<ActivityIconModel, Bitmap> {
    override fun buildLoadData(model: ActivityIconModel, width: Int, height: Int, options: Options): ModelLoader.LoadData<Bitmap> {
        return ModelLoader.LoadData(ObjectKey(model), ActivityIconFetcher(model))
    }

    override fun handles(model: ActivityIconModel): Boolean {
        return true
    }

    internal class Factory : ModelLoaderFactory<ActivityIconModel, Bitmap> {
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<ActivityIconModel, Bitmap> {
            return ActivityIconLoader()
        }

        override fun teardown() {}
    }
}