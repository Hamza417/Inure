package app.simple.inure.glide.activities

import android.graphics.Bitmap
import app.simple.inure.util.BitmapHelper
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher


class ActivityIconFetcher internal constructor(private val activityIconModel: ActivityIconModel) : DataFetcher<Bitmap> {
    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in Bitmap>) {
        kotlin.runCatching {
            callback.onDataReady(
                BitmapHelper
                        .getBitmapFromDrawable(
                            activityIconModel.activityInfo.loadIcon(
                                activityIconModel.context.packageManager)))

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