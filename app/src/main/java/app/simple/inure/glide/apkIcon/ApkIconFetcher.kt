package app.simple.inure.glide.apkIcon

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import app.simple.inure.R
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.util.BitmapHelper.toBitmap
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import net.dongliu.apk.parser.ApkFile
import java.io.ByteArrayInputStream

class ApkIconFetcher internal constructor(private val apkIcon: ApkIcon) : DataFetcher<Bitmap> {
    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in Bitmap>) {
        kotlin.runCatching {
            val p0 = ApkFile(apkIcon.file).allIcons
            callback.onDataReady(BitmapFactory.decodeStream(ByteArrayInputStream(p0.last().data)))
        }.getOrElse {
            it.printStackTrace()
            callback.onDataReady(R.drawable.ic_app_icon.toBitmap(apkIcon.context, AppearancePreferences.getIconSize()))
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