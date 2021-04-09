package app.simple.inure.decorations.views

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import app.simple.inure.R
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.util.ColorUtils.resolveAttrColor
import app.simple.inure.util.TypeFace

class Pie @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr) {

    private val totalValuePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val fractionalValuePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    private var rectF = RectF()
    private val typedArray: TypedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.TypeFaceTextView, 0, 0)

    private val dimension = context.resources.getDimensionPixelOffset(R.dimen.pie_dimens).toFloat()
    private val radius = dimension / 3F

    var value = 0F
        set(value) {
            field = value
            animateValue(value)
        }

    private var sweepAngle = 90F

    init {

        totalValuePaint.apply {
            isAntiAlias = true
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
            color = context.resolveAttrColor(R.attr.colorAppAccent)
            strokeWidth = radius / 4F
            strokeCap = Paint.Cap.ROUND
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
        }

        fractionalValuePaint.apply {
            isAntiAlias = true
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
            color = Color.parseColor("#F4F6F6")
            strokeWidth = radius / 4F
            strokeCap = Paint.Cap.ROUND
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
        }

        textPaint.apply {
            isAntiAlias = true
            textSize = radius / 4F
            color = ContextCompat.getColor(context, R.color.textSecondary)
            style = Paint.Style.FILL
            typeface = TypeFace.getTypeFace(AppearancePreferences.getAppFont(), typedArray.getInt(R.styleable.TypeFaceTextView_appFontStyle, 0), context)
        }

        setWillNotDraw(false)
    }

    override fun onDraw(canvas: Canvas) {

        val height = height / 2F
        val width = width / 2F
        rectF.set(width - radius, height - radius, width + radius, height + radius)

        canvas.drawArc(rectF, 0F, 360F, false, fractionalValuePaint)
        canvas.drawArc(rectF, 0F, sweepAngle, false, totalValuePaint)

        drawCenter(canvas, String.format("%.1f%%", sweepAngle / 360F * 100.0F))

        super.onDraw(canvas)
    }

    private fun animateValue(endValue: Float) {
        val valueAnimator = ValueAnimator.ofFloat(0F, endValue)
        valueAnimator.duration = 1000L
        valueAnimator.interpolator = DecelerateInterpolator(1.5F)
        valueAnimator.addUpdateListener {
            sweepAngle = it.animatedValue as Float
            invalidate()
        }
        valueAnimator.start()
    }

    private fun drawCenter(canvas: Canvas, text: String) {
        val r = Rect()
        canvas.getClipBounds(r)
        val cHeight: Int = r.height()
        val cWidth: Int = r.width()
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.getTextBounds(text, 0, text.length, r)
        val x: Float = cWidth / 2f - r.width() / 2f - r.left
        val y: Float = cHeight / 2f + r.height() / 2f - r.bottom
        canvas.drawText(text, x, y, textPaint)
    }
}
