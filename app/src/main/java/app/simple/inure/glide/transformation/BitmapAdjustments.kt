package app.simple.inure.glide.transformation

import android.graphics.Bitmap
import android.graphics.Color
import app.simple.inure.decorations.views.IconView
import app.simple.inure.util.BitmapHelper

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.nio.charset.Charset
import java.security.MessageDigest


class BitmapAdjustments : BitmapTransformation() {
    public override fun transform(pool: BitmapPool, bitmapToTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        return BitmapHelper.addShadow(bitmapToTransform, outHeight, outWidth, 2, 0F, 5F)
    }

    override fun equals(other: Any?): Boolean {
        return other is BitmapAdjustments
    }

    override fun hashCode(): Int {
        return ID.hashCode()
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(ID_BYTES)
    }

    companion object {
        private const val ID = "app.simple.inure.glide.transformation.BitmapAdjustments"
        private val ID_BYTES: ByteArray = ID.toByteArray(Charset.forName("UTF-8"))
    }
}