package app.simple.inure.glide.transformation

import android.content.Context
import android.graphics.*
import android.os.Build
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.util.Util
import java.nio.ByteBuffer
import java.security.MessageDigest

/**
 * This transformation adds padding intrinsically to the bitmap.
 * This is used to add a coloured border to the image, or create
 * transparent padding to prevent clipping when drawing shadows.
 */
@Suppress("unused")
class Padding : BitmapTransformation {
    private var paddingLeft: Int
    private var paddingRight: Int
    private var paddingTop: Int
    private var paddingBottom: Int
    private var colour: Int = Color.argb(0, 0, 0, 0)

    /**
     * Default constructor.
     * The padding is transparent by default.
     *
     * @param  padding  thickness of padding in pixels
     * @return      returns self
     */
    constructor(padding: Int) {
        paddingLeft = padding
        paddingRight = padding
        paddingTop = padding
        paddingBottom = padding
    }

    /**
     * Constructor.
     * The padding is transparent and zero by default.
     *
     * @return      returns self
     */
    constructor() {
        paddingLeft = 0
        paddingRight = 0
        paddingTop = 0
        paddingBottom = 0
    }

    fun setPadding(left: Int, right: Int, top: Int, bottom: Int): Padding {
        paddingLeft = left
        paddingRight = right
        paddingTop = top
        paddingBottom = bottom
        return this
    }

    /**
     * Sets the colour of the padding.
     * The padding is transparent by default.
     *
     * @param  colour  the colour as a @ColorInt
     * @return      returns self
     */
    fun setColour(@ColorInt colour: Int): Padding {
        this.colour = colour
        return this
    }

    /**
     * Sets the colour of the padding by resource.
     * The padding is transparent by default.
     *
     * @param  res  the colour as a @ColorRes
     * @return      returns self
     */
    fun setColourRes(@ColorRes res: Int, context: Context): Padding {
        if (Build.VERSION.SDK_INT < 23) {
            @Suppress("deprecation")
            colour = context.resources.getColor(res)
        } else {
            colour = context.resources.getColor(res, null)
        }
        return this
    }

    override fun transform(pool: BitmapPool, source: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        //Size Image
        val paddedWidth = 0.coerceAtLeast(source.width - (paddingLeft + paddingRight))
        val paddedHeight = 0.coerceAtLeast(source.height - (paddingTop + paddingBottom))
        val bitmap = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val bitmapBounds = Rect(paddingLeft, paddingTop, paddedWidth + paddingLeft, paddedHeight + paddingTop)
        //Create Image Paint
        val paint = Paint()
        paint.isAntiAlias = true
        paint.isFilterBitmap = true
        paint.isDither = true
        //Draw to Canvas
        val canvas = Canvas(bitmap)
        canvas.drawColor(colour)
        canvas.drawBitmap(source, null, bitmapBounds, paint)
        return bitmap
    }

    override fun equals(other: Any?): Boolean {
        if (other is Padding) {
            return paddingLeft == other.paddingLeft && paddingRight == other.paddingRight && paddingTop == other.paddingTop && paddingBottom == other.paddingBottom && colour == other.colour
        }
        return false
    }

    override fun hashCode(): Int {
        return Util.hashCode(ID.hashCode(),
                             Util.hashCode(paddingLeft,
                                           Util.hashCode(paddingRight,
                                                         Util.hashCode(paddingTop,
                                                                       Util.hashCode(paddingBottom,
                                                                                     Util.hashCode(colour))))))
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        val messages: ArrayList<ByteArray> = ArrayList()
        messages.add(ID_BYTES)
        messages.add(ByteBuffer.allocate(Integer.SIZE / java.lang.Byte.SIZE).putInt(paddingLeft).array())
        messages.add(ByteBuffer.allocate(Integer.SIZE / java.lang.Byte.SIZE).putInt(paddingRight).array())
        messages.add(ByteBuffer.allocate(Integer.SIZE / java.lang.Byte.SIZE).putInt(paddingTop).array())
        messages.add(ByteBuffer.allocate(Integer.SIZE / java.lang.Byte.SIZE).putInt(paddingBottom).array())
        messages.add(ByteBuffer.allocate(Integer.SIZE / java.lang.Byte.SIZE).putInt(colour).array())
        for (c in 0 until messages.size) {
            messageDigest.update(messages[c])
        }
    }

    companion object {
        private const val ID = "app.simple.inure.glide.transformations.Padding"
        private val ID_BYTES = ID.toByteArray()
    }
}