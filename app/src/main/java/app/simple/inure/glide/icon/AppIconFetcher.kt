package app.simple.inure.glide.icon

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher

class AppIconFetcher internal constructor(private val appIcon: AppIcon) : DataFetcher<Bitmap> {
    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in Bitmap>) {
        try {
            callback.onDataReady(appIcon.context.packageManager.getApplicationIcon(appIcon.packageName).toBitmap())
        } catch (e: PackageManager.NameNotFoundException) {
            callback.onLoadFailed(e)
        } catch (e: NullPointerException) {
            callback.onLoadFailed(e)
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