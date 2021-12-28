package app.simple.inure.glide.services

import android.graphics.Bitmap
import app.simple.inure.util.BitmapHelper.toBitmap
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher

class ServiceIconFetcher internal constructor(private val serviceIconModel: ServiceIconModel) : DataFetcher<Bitmap> {
    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in Bitmap>) {
        kotlin.runCatching {
            callback.onDataReady(serviceIconModel.serviceInfo.loadIcon(
                    serviceIconModel.context.packageManager).toBitmap())
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