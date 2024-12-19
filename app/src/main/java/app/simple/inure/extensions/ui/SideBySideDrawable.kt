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
    private val drawable2: Drawable?

    constructor(drawable1: Drawable, drawable2: Drawable? = null) : super() {
        this.drawable1 = drawable1
        this.drawable2 = drawable2
        this.bounds = Rect()
    }

    constructor(context: Context, drawable1: Int, drawable2: Int? = null) : super() {
        this.drawable1 = ContextCompat.getDrawable(context, drawable1)!!
        this.drawable2 = drawable2?.let { ContextCompat.getDrawable(context, it) }
        this.bounds = Rect()
    }

    constructor(context: Context, drawable1: Drawable, drawable2: Int?) {
        this.drawable1 = drawable1
        this.drawable2 = drawable2?.let { ContextCompat.getDrawable(context, it) }
        this.bounds = Rect()
    }

    private var bounds: Rect

    override fun draw(canvas: Canvas) {
        if (drawable2 == null) {
            drawable1.bounds = bounds
            drawable1.draw(canvas)
        } else {
            val totalWidth = drawable1.intrinsicWidth + drawable2.intrinsicWidth + padding
            val scale = bounds.width().toFloat() / totalWidth

            val scaledWidth1 = (drawable1.intrinsicWidth * scale).toInt()
            val scaledWidth2 = (drawable2.intrinsicWidth * scale).toInt()

            drawable1.setBounds(bounds.left, bounds.top, bounds.left + scaledWidth1, bounds.bottom)
            drawable2.setBounds(bounds.right - scaledWidth2, bounds.top, bounds.right, bounds.bottom)

            drawable1.draw(canvas)
            drawable2.draw(canvas)
        }
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
        return if (drawable2 == null) {
            drawable1.intrinsicWidth
        } else {
            drawable1.intrinsicWidth + drawable2.intrinsicWidth + padding
        }
    }

    override fun getIntrinsicHeight(): Int {
        return if (drawable2 == null) {
            drawable1.intrinsicHeight
        } else {
            max(drawable1.intrinsicHeight, drawable2.intrinsicHeight)
        }
    }

    override fun setAlpha(alpha: Int) {
        drawable1.alpha = alpha
        drawable2?.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        drawable1.colorFilter = colorFilter
        drawable2?.colorFilter = colorFilter
    }

    fun setPadding(padding: Int) {
        this.padding = padding
        invalidateSelf()
    }

    @Deprecated(
            "Deprecated in Java",
            ReplaceWith("PixelFormat.TRANSLUCENT", "android.graphics.PixelFormat")
    )
    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }
}
