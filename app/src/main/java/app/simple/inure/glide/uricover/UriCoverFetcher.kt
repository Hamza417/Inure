package app.simple.inure.glide.uricover

import app.simple.inure.R
import app.simple.inure.util.BitmapHelper.toBitmap
import app.simple.inure.util.BitmapHelper.toInputStream
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import java.io.FileNotFoundException
import java.io.InputStream

class UriCoverFetcher internal constructor(private val uriCoverModel: UriCoverModel) : DataFetcher<InputStream> {
    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream>) {
        try {
            uriCoverModel.context.contentResolver.openInputStream(uriCoverModel.artUri).use {
                callback.onDataReady(it)
            }
        } catch (_: IllegalArgumentException) {
        } catch (e: FileNotFoundException) {
            R.drawable.ic_app_icon.toBitmap(uriCoverModel.context).toInputStream().use {
                callback.onDataReady(it)
            }
        }
    }

    override fun cleanup() {
        // Cleared
    }

    override fun cancel() {
        // Probably already cleared
    }

    override fun getDataClass(): Class<InputStream> {
        return InputStream::class.java
    }

    override fun getDataSource(): DataSource {
        return DataSource.LOCAL
    }
}