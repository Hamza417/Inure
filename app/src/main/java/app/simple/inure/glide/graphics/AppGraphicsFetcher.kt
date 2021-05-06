package app.simple.inure.glide.graphics

import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class AppGraphicsFetcher internal constructor(private val appGraphicsModel: AppGraphicsModel) : DataFetcher<InputStream> {
    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream>) {
        var zipFile: ZipFile? = null
        try {
            zipFile = ZipFile(appGraphicsModel.path)
            val entries: Enumeration<out ZipEntry?> = zipFile.entries()
            while (entries.hasMoreElements()) {
                val entry: ZipEntry? = entries.nextElement()
                val name: String = entry!!.name
                if (name == appGraphicsModel.filePath) {
                    callback.onDataReady(BufferedInputStream(ZipFile(appGraphicsModel.path).getInputStream(entry)))
                }
            }
        } catch (e: IOException) {
            callback.onLoadFailed(e)
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close()
                } catch (ignored: IOException) {
                }
            }
        }
    }

    override fun cleanup() {
        /* no-op */
    }

    override fun cancel() {
        /* no-op */
    }

    override fun getDataClass(): Class<InputStream> {
        return InputStream::class.java
    }

    override fun getDataSource(): DataSource {
        return DataSource.LOCAL
    }

}