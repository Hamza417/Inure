package app.simple.inure.decorations.colorpicker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

class ColorPointer @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0,
        defStyleRes: Int = 0
) : View(context, attrs, defStyle, defStyleRes) {

    private var pointerRadius = ColorPickerView.COLOR_POINTER_RADIUS_DP
    private var point = PointF()

    init {
        alpha = 0.5f
    }

    private var selectorPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        .apply {
            color = ContextCompat.getColor(context, android.R.color.black)
            style = Paint.Style.STROKE
            strokeWidth = 8f
        }

    override fun onDraw(canvas: Canvas) {
        canvas.drawCircle(point.x, point.y, pointerRadius * 0.66f, selectorPaint)
    }

    fun setPointerRadius(pointerRadius: Float) {
        this.pointerRadius = pointerRadius
    }

    fun setCurrentPoint(point: PointF) {
        this.point = point
        invalidate()
    }
}