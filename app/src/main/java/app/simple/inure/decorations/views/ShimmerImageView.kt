package app.simple.inure.decorations.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import androidx.appcompat.widget.AppCompatImageView

class ShimmerImageView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val shimmerPaint = Paint()
    private var gradient: LinearGradient? = null
    private val gradientMatrix = Matrix()

    private var shimmerAnimator: ValueAnimator? = null
    private var shimmerTranslate = 0f

    private val shimmerWidthFraction = 0.2f // % of view width used for shimmer band

    init {
        startShimmer()
    }

    private fun createShimmerGradient(viewWidth: Int, viewHeight: Int) {
        val shimmerWidth = viewWidth * shimmerWidthFraction

        gradient = LinearGradient(
                -shimmerWidth, -shimmerWidth, shimmerWidth, shimmerWidth, // diagonal: top-left ➝ bottom-right
                intArrayOf(Color.TRANSPARENT, 0x51FFFFFF, Color.TRANSPARENT),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
        )
        shimmerPaint.shader = gradient
        shimmerPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
    }

    private fun startShimmer() {
        shimmerAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 2500L
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()

            addUpdateListener {
                shimmerTranslate = it.animatedFraction
                invalidate()
            }

            start()
        }
    }

    private fun stopShimmer() {
        shimmerAnimator?.cancel()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopShimmer()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (drawable == null || width == 0 || height == 0) return

        if (gradient == null) {
            createShimmerGradient(width, height)
        }

        val shimmerWidth = width * shimmerWidthFraction
        val dx = width + shimmerWidth * 2
        val dy = height + shimmerWidth * 2

        val translateX = shimmerTranslate * dx - shimmerWidth
        val translateY = shimmerTranslate * dy - shimmerWidth

        gradientMatrix.reset()
        gradientMatrix.setTranslate(translateX, translateY)
        gradient?.setLocalMatrix(gradientMatrix)

        canvas.saveLayer(null, null)
        super.onDraw(canvas)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), shimmerPaint)
        canvas.restore()
    }
}
