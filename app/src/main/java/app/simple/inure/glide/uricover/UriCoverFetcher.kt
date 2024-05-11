package app.simple.inure.glide.uricover

import app.simple.inure.preferences.AppearancePreferences
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import java.io.FileNotFoundException
import java.io.InputStream

class UriCoverFetcher internal constructor(private val uriCoverModel: UriCoverModel) : DataFetcher<InputStream> {

    private val colorArray = intArrayOf(AppearancePreferences.getAccentColor(),
                                        AppearancePreferences.getAccentColorLight(uriCoverModel.context))

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream>) {
        try {
            uriCoverModel.context.contentResolver.openInputStream(uriCoverModel.artUri).use {
                callback.onDataReady(it)
            }
        } catch (e: IllegalArgumentException) {
            callback.onLoadFailed(e)
        } catch (e: FileNotFoundException) {
            callback.onLoadFailed(e)
        } catch (e: Exception) {
            callback.onLoadFailed(e)
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
