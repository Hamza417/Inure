package app.simple.inure.decorations.colorpicker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.SweepGradient
import android.util.AttributeSet
import android.view.View
import app.simple.inure.preferences.ColorPickerPreferences

class ColorPalette @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0, defStyleRes: Int = 0) : View(context, attrs, defStyle, defStyleRes) {

    private var radius = 0f
    private var centerX = 0f
    private var centerY = 0f

    private var huePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var saturationPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var hueColors = intArrayOf(
            Color.RED,
            Color.MAGENTA,
            Color.BLUE,
            Color.CYAN,
            Color.GREEN,
            Color.YELLOW,
            Color.RED
    )

    private var pastelHues = intArrayOf(
            Color.parseColor("#FAA0A0"),
            Color.parseColor("#f49ac2"),
            Color.parseColor("#aec6cf"),
            Color.parseColor("#A4D8D8"),
            Color.parseColor("#C1E1C1"),
            Color.parseColor("#FDFD96"),
            Color.parseColor("#FAA0A0")
    )

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val netWidth = w - paddingLeft - paddingRight
        val netHeight = h - paddingTop - paddingBottom
        radius = netWidth.coerceAtMost(netHeight) * 0.5f

        if (radius < 0) return

        centerX = w * 0.5f
        centerY = h * 0.5f

        huePaint.shader = when {
            isInEditMode -> {
                SweepGradient(centerX, centerY, hueColors, null)
            }
            ColorPickerPreferences.isColorHueModePastel() -> {
                SweepGradient(centerX, centerY, pastelHues, null)
            }
            ColorPickerPreferences.isColorHueModeDefault() -> {
                SweepGradient(centerX, centerY, hueColors, null)
            }
            else -> {
                SweepGradient(centerX, centerY, hueColors, null)
            }
        }

        saturationPaint.shader = RadialGradient(
                centerX, centerY, radius,
                Color.WHITE, 0x00FFFFFF, Shader.TileMode.CLAMP
        )
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawCircle(centerX, centerY, radius, huePaint)
        canvas.drawCircle(centerX, centerY, radius, saturationPaint)
    }

    override fun invalidate() {
        huePaint.shader = when {
            isInEditMode -> {
                SweepGradient(centerX, centerY, hueColors, null)
            }
            ColorPickerPreferences.isColorHueModePastel() -> {
                SweepGradient(centerX, centerY, pastelHues, null)
            }
            ColorPickerPreferences.isColorHueModeDefault() -> {
                SweepGradient(centerX, centerY, hueColors, null)
            }
            else -> {
                SweepGradient(centerX, centerY, hueColors, null)
            }
        }

        saturationPaint.shader = RadialGradient(
                centerX, centerY, radius,
                Color.WHITE, 0x00FFFFFF, Shader.TileMode.CLAMP
        )

        super.invalidate()
    }
}
