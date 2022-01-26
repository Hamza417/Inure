package app.simple.inure.glide.transformation

import android.content.Context
import android.graphics.*
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.IntDef
import androidx.core.content.ContextCompat
import app.simple.inure.preferences.AppearancePreferences
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.util.Util
import com.google.android.renderscript.Toolkit
import java.nio.ByteBuffer
import java.security.MessageDigest
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * This transformation applies a shadow intrinsically to the bitmap.
 * This is useful for images with complex shapes where Android does
 * not support elevation shadows. The colour of the shadow, its blur
 * radius, and offset from the image can all be configured.
 *
 *
 * Images should be padded with transparent pixels by at least the
 * blur radius plus the elevation in order for the drawn shadow to
 * display properly without clipping. See: Padding
 */
@Suppress("unused")

/**
 * Default constructor.
 * The shadow is set at 0 elevation and 0 blur, with black colour at 50%
 * opacity, by default.
 *
 * @param context current context
 */
class BlurShadow(private val context: Context) : BitmapTransformation() {
    private var blurRadius = 0f
    private var elevation = 0f
    private var angle = 0f
    private var colour: Int

    @IntDef(EAST, NORTHEAST, NORTH, NORTHWEST, WEST, SOUTHWEST, SOUTH, SOUTHEAST)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Direction

    /**
     * Sets the blur radius of the shadow.
     * It is advised to pad the image by at least this amount plus the
     * elevation to prevent clipping of the shadow.
     *
     * @param  blurRadius  elevation in pixels
     * @return      returns self
     */
    fun setBlurRadius(blurRadius: Float): BlurShadow {
        this.blurRadius = blurRadius
        return this
    }

    /**
     * Sets the elevation, or how much the shadow is offset from the image.
     * It is advised to pad the image by at least this amount plus the
     * blur radius to prevent clipping of the shadow.
     *
     * @param  elevation  elevation in pixels
     * @return      returns self
     */
    fun setElevation(elevation: Float): BlurShadow {
        this.elevation = elevation
        return this
    }

    /**
     * Sets the angle in which the shadow is offset from the image.
     * Zero degrees indicates due west, and angles progress counter-clockwise.
     * Angles larger than 360° or smaller than 0° simply indicate wraps around the circle.
     *
     * @param  angle  the angle in degrees
     * @return      returns self
     */
    fun setAngle(angle: Float): BlurShadow {
        this.angle = angle
        return this
    }

    /**
     * Sets the cardinal direction in which the shadow is offset from the image.
     *
     * @param d the cardinal direction as a @Direction
     * @return returns self
     */
    fun setDirection(@Direction d: Int): BlurShadow {
        angle = getAngle(d)
        return this
    }

    /**
     * Sets the shadow's colour.
     * Shadow is drawn black with 50% opacity by default.
     *
     * @param colour the colour as a @ColorInt
     * @return returns self
     */
    fun setShadowColour(@ColorInt colour: Int): BlurShadow {
        this.colour = colour
        return this
    }

    /**
     * Sets the shadow's colour by colour resource.
     * Shadow is drawn black with 50% opacity by default.
     *
     * @param  res  the colour resource as a @ColorRes
     * @return      returns self
     */
    fun setShadowColourRes(@ColorRes res: Int): BlurShadow {
        colour = ContextCompat.getColor(context, res)
        return this
    }

    private fun getAngle(@Direction d: Int): Float {
        return when (d) {
            EAST -> 0f
            NORTHEAST -> 45f
            NORTH -> 90f
            NORTHWEST -> 135f
            WEST -> 180f
            SOUTHWEST -> 225f
            SOUTH -> 270f
            SOUTHEAST -> 315f
            else -> throw IllegalArgumentException("Invalid Direction")
        }
    }

    override fun transform(pool: BitmapPool, source: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val shadow: Bitmap

        //Calculate Shadow Offset
        val shadowX = elevation * cos(Math.toRadians(angle.toDouble())).toFloat()
        val shadowY = -(elevation * sin(Math.toRadians(angle.toDouble())).toFloat())

        //Create Shadow Paint
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            colorFilter = if (AppearancePreferences.getColoredIconShadows()) {
                ColorMatrixColorFilter(ColorMatrix().apply {
                    setScale(SHADOW_SCALE_RGB, SHADOW_SCALE_RGB, SHADOW_SCALE_RGB, SHADOW_SCALE_ALPHA)
                })
            } else {
                PorterDuffColorFilter(colour, PorterDuff.Mode.SRC_IN)
            }
            isAntiAlias = true
        }

        if (blurRadius <= MAX_BLUR_RADIUS) {
            //Apply Blur
            shadow = if (AppearancePreferences.isIconShadowsOn()) {
                Toolkit.blur(source, blurRadius.toInt())
            } else {
                Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
            }
            //Draw to Canvas
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            canvas.drawBitmap(shadow, 0F, 0F, shadowPaint)
            canvas.drawBitmap(source, 0f, 0f, null)
        } else {
            //Scale
            val scaleFactor = MAX_BLUR_RADIUS / blurRadius
            val scaledWidth = 1.coerceAtLeast((source.width.toFloat() * scaleFactor).roundToInt())
            val scaledHeight = 1.coerceAtLeast((source.height.toFloat() * scaleFactor).roundToInt())
            val scaled = Bitmap.createScaledBitmap(source, source.width, source.height, true)
            //Apply Blur
            shadow = if (AppearancePreferences.isIconShadowsOn()) {
                Toolkit.blur(scaled, MAX_BLUR_RADIUS.toInt())
            } else {
                Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
            }
            //Draw to Canvas
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            canvas.drawBitmap(shadow, shadowX, shadowY, shadowPaint)
            canvas.drawBitmap(source, 0f, 0f, null)
        }

        //Output
        shadow.recycle()
        return bitmap
    }

    override fun equals(other: Any?): Boolean {
        if (other is BlurShadow) {
            return blurRadius == other.blurRadius && elevation == other.elevation && angle == other.angle && colour == other.colour
        }
        return false
    }

    override fun hashCode(): Int {
        return Util.hashCode(ID.hashCode(), Util.hashCode(blurRadius, Util.hashCode(elevation, Util.hashCode(angle, Util.hashCode(colour)))))
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        val messages = ArrayList<ByteArray>()
        messages.add(ID_BYTES)
        messages.add(ByteBuffer.allocate(java.lang.Float.SIZE / java.lang.Byte.SIZE)
                         .putFloat(blurRadius).array())
        messages.add(ByteBuffer.allocate(java.lang.Float.SIZE / java.lang.Byte.SIZE)
                         .putFloat(elevation).array())
        messages.add(ByteBuffer.allocate(java.lang.Float.SIZE / java.lang.Byte.SIZE).putFloat(angle)
                         .array())
        messages.add(ByteBuffer.allocate(Integer.SIZE / java.lang.Byte.SIZE).putInt(colour).array())
        messages.add(ByteBuffer.allocate(java.lang.Long.SIZE).putLong(System.currentTimeMillis())
                         .array())
        for (c in messages.indices) {
            messageDigest.update(messages[c])
        }
    }

    companion object {
        private const val ID = "app.simple.inure.glide.transformations.Shadow"
        private val ID_BYTES = ID.toByteArray()
        const val MAX_BLUR_RADIUS = 25.0f
        const val DEFAULT_SHADOW_SIZE = 20.0F
        private const val SHADOW_SCALE_RGB = 0.85f
        private const val SHADOW_SCALE_ALPHA = 0.8f
        const val EAST = 0
        const val NORTHEAST = 1
        const val NORTH = 2
        const val NORTHWEST = 3
        const val WEST = 4
        const val SOUTHWEST = 5
        const val SOUTH = 6
        const val SOUTHEAST = 7
    }

    init {
        colour = Color.argb(60, 0, 0, 0)
    }
}