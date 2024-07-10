import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import kotlin.math.max

class SideBySideDrawable : Drawable {

    private var padding = 5
    private val drawable1: Drawable
    private val drawable2: Drawable

    @Suppress("unused")
    constructor(drawable1: Drawable, drawable2: Drawable) : super() {
        this.drawable1 = drawable1
        this.drawable2 = drawable2
        this.bounds = Rect()
    }

    constructor(context: Context, drawable1: Int, drawable2: Int) : super() {
        this.drawable1 = ContextCompat.getDrawable(context, drawable1)!!
        this.drawable2 = ContextCompat.getDrawable(context, drawable2)!!
        this.bounds = Rect()
    }

    private var bounds: Rect

    override fun draw(canvas: Canvas) {
        val totalWidth = drawable1.intrinsicWidth + drawable2.intrinsicWidth + padding
        val scale = bounds.width().toFloat() / totalWidth

        val scaledWidth1 = (drawable1.intrinsicWidth * scale).toInt()
        val scaledWidth2 = (drawable2.intrinsicWidth * scale).toInt()

        drawable1.setBounds(bounds.left, bounds.top, bounds.left + scaledWidth1, bounds.bottom)
        drawable2.setBounds(bounds.right - scaledWidth2, bounds.top, bounds.right, bounds.bottom)

        drawable1.draw(canvas)
        drawable2.draw(canvas)
    }

    override fun setBounds(bounds: Rect) {
        super.setBounds(bounds)
        this.bounds = bounds
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom)
        this.bounds = Rect(left, top, right, bottom)
    }

    override fun getIntrinsicWidth(): Int {
        return drawable1.intrinsicWidth + drawable2.intrinsicWidth + padding
    }

    override fun getIntrinsicHeight(): Int {
        return max(drawable1.intrinsicHeight, drawable2.intrinsicHeight)
    }

    override fun setAlpha(alpha: Int) {
        drawable1.alpha = alpha
        drawable2.alpha = alpha
    }

    /**
     * Set the color filter manually, overriding it here
     * does not work.
     *
     * Example:
     * PorterDuffColorFilter(your_color, PorterDuff.Mode.SRC_IN)
     *
     * @param colorFilter The color filter to apply
     * @see Drawable.setColorFilter
     */
    override fun setColorFilter(colorFilter: ColorFilter?) {
        drawable1.colorFilter = colorFilter
        drawable2.colorFilter = colorFilter
    }

    fun setPadding(padding: Int) {
        this.padding = padding
        invalidateSelf()
    }

    @Deprecated(
            "Deprecated in Java",
            ReplaceWith("PixelFormat.TRANSLUCENT",
                        "android.graphics.PixelFormat"))
    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

}
