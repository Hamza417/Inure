package app.simple.inure.glide.graphics

import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.os.Build
import app.simple.inure.util.BitmapHelper.toBitmap
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import com.caverock.androidsvg.SVG
import java.io.*
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class AppGraphicsFetcher internal constructor(private val appGraphicsModel: AppGraphicsModel) : DataFetcher<InputStream> {
    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream>) {
        var zipFile: ZipFile? = null
        val byteArrayOutputStream = ByteArrayOutputStream()
        try {
            zipFile = ZipFile(appGraphicsModel.path)
            val entries: Enumeration<out ZipEntry?> = zipFile.entries()
            while (entries.hasMoreElements()) {
                val entry: ZipEntry? = entries.nextElement()
                if (entry!!.name == appGraphicsModel.filePath) {
                    if (entry.name.endsWith(".svg")) {
                        val bitmap = ZipFile(appGraphicsModel.path).getInputStream(entry).use {
                            // TODO - fix resource leak here
                            val svg = SVG.getFromInputStream(it)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                Bitmap.createBitmap(svg.renderToPicture(), 500, 500, Bitmap.Config.ARGB_8888)
                            } else {
                                with(150F) {
                                    svg.documentWidth = this
                                    svg.documentHeight = this
                                }
                                svg.renderToPicture().toBitmap()
                            }
                        }

                        bitmap!!.compress(CompressFormat.PNG, 0 /*ignored for PNG*/, byteArrayOutputStream)

                        ByteArrayInputStream(byteArrayOutputStream.toByteArray()).use {
                            callback.onDataReady(it)
                        }

                        bitmap.recycle()

                        break
                    } else {
                        ZipFile(appGraphicsModel.path).use { file ->
                            file.getInputStream(entry).use { inputStream ->
                                BufferedInputStream(inputStream).use {
                                    callback.onDataReady(it)
                                }
                            }
                        }

                        break
                    }
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

            byteArrayOutputStream.close()
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