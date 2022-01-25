package app.simple.inure.util

import android.content.Context
import android.graphics.*
import android.graphics.BlurMaskFilter.Blur
import android.graphics.Matrix.ScaleToFit
import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import androidx.core.content.ContextCompat
import app.simple.inure.preferences.AppearancePreferences

object BitmapHelper {
    private const val shadowColor = -4671304

    /**
     * Converts drawable to bitmap
     */
    fun Int.toBitmap(context: Context, size: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(context, this)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable?.setBounds(0, 0, canvas.width, canvas.height)
        drawable?.draw(canvas)
        return bitmap
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
    fun Picture.toBitmap(): Bitmap? {
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

    fun Drawable.toBitmap(dimension: Int = AppearancePreferences.getIconSize()): Bitmap? {
        val bitmap = Bitmap.createBitmap(dimension, dimension, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        setBounds(0, 0, dimension, dimension)
        draw(canvas)
        return bitmap
    }
}
