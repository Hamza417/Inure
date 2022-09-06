package app.simple.inure.glide.apkIcon

import android.graphics.Bitmap
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.signature.ObjectKey

class ApkIconLoader : ModelLoader<ApkIcon, Bitmap> {
    override fun buildLoadData(apkIcon: ApkIcon, width: Int, height: Int, options: Options): ModelLoader.LoadData<Bitmap> {
        return ModelLoader.LoadData(ObjectKey(apkIcon), ApkIconFetcher(apkIcon))
    }

    override fun handles(model: ApkIcon): Boolean {
        return true
    }

    internal class Factory : ModelLoaderFactory<ApkIcon, Bitmap> {
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<ApkIcon, Bitmap> {
            return ApkIconLoader()
        }

        override fun teardown() {
            /* no-op */
        }
    }
}