package app.simple.inure.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.BlurMaskFilter.Blur
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Matrix.ScaleToFit
import android.graphics.Paint
import android.graphics.Picture
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import androidx.core.content.ContextCompat
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

object BitmapHelper {
    private const val shadowColor = -4671304

    /**
     * Converts drawable to bitmap
     */
    fun Int.toBitmap(context: Context, size: Int = 500): Bitmap {
        val drawable = ContextCompat.getDrawable(context, this)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable?.setBounds(0, 0, canvas.width, canvas.height)
        drawable?.draw(canvas)
        return bitmap
    }

    fun Bitmap.toInputStream(): InputStream {
        ByteArrayOutputStream().use {
            this.compress(Bitmap.CompressFormat.PNG, 100, it)
            return ByteArrayInputStream(it.toByteArray())
        }
    }

    fun Bitmap.addLinearGradient(array: IntArray): Bitmap {
        val width = width
        val height = height
        val updatedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(updatedBitmap)
        canvas.drawBitmap(this, 0f, 0f, null)
        val paint = Paint()
        val shader = LinearGradient(0f, 0f, width.toFloat(), height.toFloat(), array[0], array[1], Shader.TileMode.CLAMP)
        paint.shader = shader
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        return updatedBitmap
    }

    fun addLinearGradient(originalBitmap: Bitmap, array: IntArray, verticalOffset: Float): Bitmap {
        val width = originalBitmap.width
        val height = originalBitmap.height
        val updatedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(updatedBitmap)
        canvas.drawBitmap(originalBitmap, 0f, 0f, null)
        val paint = Paint()
        val shader = LinearGradient(0f, verticalOffset, 0f, height.toFloat(), array[0], array[1], Shader.TileMode.CLAMP)
        paint.shader = shader
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        return updatedBitmap
    }

    fun addRadialGradient(originalBitmap: Bitmap, int: Int): Bitmap {
        val width = originalBitmap.width
        val height = originalBitmap.height
        val updatedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(updatedBitmap)
        canvas.drawBitmap(originalBitmap, 0f, 0f, null)
        val paint = Paint()
        val shader = RadialGradient(width.div(2F), height.div(2F), 200f, 0x000000, int, Shader.TileMode.CLAMP)
        paint.shader = shader
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        return updatedBitmap
    }

    /**
     * Add drop shadow to the bitmap layer
     *
     * @param bitmap - the raw bitmap that needs to be modified
     * @param dstHeight - height of the destination view
     * @param dstWidth - width of the destination view
     * @param size - of the the shadow
     * @param dx - xOffset of the shadow
     * @param dy - yOffset of the shadow
     *
     * @return [Bitmap]
     */
    @Deprecated("Use Glide with Renderscript")
    fun addShadow(bitmap: Bitmap, dstHeight: Int, dstWidth: Int, size: Int, dx: Float, dy: Float): Bitmap {
        val mask = Bitmap.createBitmap(dstWidth, dstHeight, Bitmap.Config.ARGB_8888)
        val scaleToFit = Matrix()
        val src = RectF(0F, 0F, bitmap.width.toFloat(), bitmap.height.toFloat())
        val dst = RectF(0F, 0F, dstWidth - dx, dstHeight - dy)
        scaleToFit.setRectToRect(src, dst, ScaleToFit.CENTER)
        val dropShadow = Matrix(scaleToFit)
        dropShadow.postTranslate(dx, dy)
        val maskCanvas = Canvas(mask)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        maskCanvas.drawBitmap(bitmap, scaleToFit, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OUT)
        maskCanvas.drawBitmap(bitmap, dropShadow, paint)
        val filter = BlurMaskFilter(size.toFloat(), Blur.NORMAL)
        paint.reset()
        paint.isAntiAlias = true
        paint.color = shadowColor
        paint.maskFilter = filter
        paint.isFilterBitmap = true
        val finalBitmap = Bitmap.createBitmap(dstWidth, dstHeight, Bitmap.Config.ARGB_8888)
        val retCanvas = Canvas(finalBitmap)
        retCanvas.drawBitmap(mask, 0F, 0F, paint)
        retCanvas.drawBitmap(bitmap, scaleToFit, null)
        mask.recycle()
        return finalBitmap
    }

    //Convert Picture to Bitmap
    fun Picture.toBitmap(): Bitmap {
        val pd = PictureDrawable(this)
        val bitmap = Bitmap.createBitmap(pd.intrinsicWidth, pd.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawPicture(pd.picture)
        return bitmap
    }

    fun getBitmapFromDrawable(drawable: Drawable): Bitmap {
        val bmp = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bmp
    }

    fun Drawable.toBitmap(dimension: Int = 300): Bitmap {
        val bitmap = Bitmap.createBitmap(dimension, dimension, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        setBounds(0, 0, dimension, dimension)
        draw(canvas)
        return bitmap
    }

    /**
     * Convert bitmap to grayscale
     */
    fun Bitmap.toGrayscale(): Bitmap {
        val width = width
        val height = height
        val bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmpGrayscale)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0F)
        val f = ColorMatrixColorFilter(colorMatrix)
        paint.colorFilter = f
        canvas.drawBitmap(this, 0F, 0F, paint)
        return bmpGrayscale
    }
}
