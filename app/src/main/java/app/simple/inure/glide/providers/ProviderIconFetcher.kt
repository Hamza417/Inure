package app.simple.inure.glide.providers

import android.graphics.Bitmap
import app.simple.inure.util.BitmapHelper.toBitmap
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher

class ProviderIconFetcher internal constructor(private val providerIconModel: ProviderIconModel) : DataFetcher<Bitmap> {
    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in Bitmap>) {
        kotlin.runCatching {
            callback.onDataReady(providerIconModel.providerInfo.loadIcon(
                    providerIconModel.context.packageManager).toBitmap())
        }.getOrElse {
            callback.onLoadFailed(it as Exception)
        }
    }

    override fun cleanup() {
        /* no-op */
    }

    override fun cancel() {
        /* no-op */
    }

    override fun getDataClass(): Class<Bitmap> {
        return Bitmap::class.java
    }

    override fun getDataSource(): DataSource {
        return DataSource.LOCAL
    }
}