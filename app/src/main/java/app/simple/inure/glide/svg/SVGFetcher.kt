package app.simple.inure.glide.svg

import android.graphics.Bitmap
import android.os.Build
import app.simple.inure.util.BitmapHelper.toBitmap
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

class SVGFetcher(private val svg: SVG) : DataFetcher<InputStream> {
    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream>) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val bitmap = svg.context.contentResolver.openInputStream(svg.uri).use {
            // TODO - fix resource leak here
            val svg = com.caverock.androidsvg.SVG.getFromInputStream(it)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                Bitmap.createBitmap(svg.renderToPicture(), 1080, 1080, Bitmap.Config.ARGB_8888)
            } else {
                with(150F) {
                    svg.documentWidth = this
                    svg.documentHeight = this
                }
                svg.renderToPicture().toBitmap()
            }
        }

        bitmap!!.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, byteArrayOutputStream)

        ByteArrayInputStream(byteArrayOutputStream.toByteArray()).use {
            callback.onDataReady(it)
        }

        bitmap.recycle()
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