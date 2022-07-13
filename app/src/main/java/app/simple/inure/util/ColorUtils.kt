package app.simple.inure.util

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.Size
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.TextViewCompat
import app.simple.inure.R
import com.google.android.material.animation.ArgbEvaluatorCompat

object ColorUtils {
    @ColorInt
    fun Context.resolveAttrColor(@AttrRes attr: Int): Int {
        val a = theme.obtainStyledAttributes(intArrayOf(attr))
        val color: Int
        try {
            color = a.getColor(0, 0)
        } finally {
            a.recycle()
        }
        return color
    }

    @ColorInt
    fun Resources.Theme.resolveAttrColor(@AttrRes attr: Int): Int {
        val a = obtainStyledAttributes(intArrayOf(attr))
        val color: Int
        try {
            color = a.getColor(0, 0)
        } finally {
            a.recycle()
        }
        return color
    }

    fun changeAlpha(origColor: Int, userInputAlpha: Int): Int {
        return origColor and 0x00ffffff or (userInputAlpha shl 24)
    }

    fun Int.toHexColor(): String {
        return String.format("#%06X", 0xFFFFFF and this)
    }

    /**
     * Lightens the given color int
     *
     * @param value between 0.0F - 1.0F of which closer to one being
     *              the transparent while 0.0 will be original
     *              strength
     */
    @ColorInt
    fun lightenColor(@ColorInt color: Int, value: Float = 0.4F): Int {
        val hsl = colorToHSL(color)
        hsl[2] += value
        hsl[2] = 0f.coerceAtLeast(hsl[2].coerceAtMost(1f))
        return hslToColor(hsl)
    }

    @ColorInt
    fun darkenColor(@ColorInt color: Int, value: Float): Int {
        val hsl = colorToHSL(color)
        hsl[2] -= value
        hsl[2] = 0f.coerceAtLeast(hsl[2].coerceAtMost(1f))
        return hslToColor(hsl)
    }

    @Size(3)
    private fun colorToHSL(@ColorInt color: Int): FloatArray {
        val hsl = FloatArray(3)

        val r = Color.red(color) / 255f
        val g = Color.green(color) / 255f
        val b = Color.blue(color) / 255f

        val max = r.coerceAtLeast(g.coerceAtLeast(b))
        val min = r.coerceAtMost(g.coerceAtMost(b))

        hsl[2] = (max + min) / 2
        if (max == min) {
            hsl[1] = 0f
            hsl[0] = hsl[1]
        } else {
            val d = max - min
            hsl[1] = if (hsl[2] > 0.5f) d / (2f - max - min) else d / (max +
                    min)
            when (max) {
                r -> hsl[0] = (g - b) / d + (if (g < b) 6 else 0)
                g -> hsl[0] = (b - r) / d + 2
                b -> hsl[0] = (r - g) / d + 4
            }
            hsl[0] /= 6f
        }
        return hsl
    }

    @ColorInt
    private fun hslToColor(@Size(3) hsl: FloatArray): Int {
        val r: Float
        val g: Float
        val b: Float
        val h = hsl[0]
        val s = hsl[1]
        val l = hsl[2]
        if (s == 0f) {
            b = l
            g = b
            r = g
        } else {
            val q = if (l < 0.5f) l * (1 + s) else l + s - l * s
            val p = 2 * l - q
            r = hue2rgb(p, q, h + 1f / 3)
            g = hue2rgb(p, q, h)
            b = hue2rgb(p, q, h - 1f / 3)
        }
        return Color.rgb((r * 255).toInt(), (g * 255).toInt(), (b * 255).toInt())
    }

    private fun hue2rgb(p: Float, q: Float, t: Float): Float {
        var valueT = t
        if (valueT < 0) valueT += 1f
        if (valueT > 1) valueT -= 1f
        if (valueT < 1f / 6) return p + (q - p) * 6f * valueT
        if (valueT < 1f / 2) return q
        return if (valueT < 2f / 3) p + (q - p) * (2f / 3 - valueT) * 6f else p
    }

    fun ViewGroup.animateColorChange(endColor: Int) {
        val colorAnim = ValueAnimator.ofObject(ArgbEvaluatorCompat(), this.backgroundTintList?.defaultColor, endColor)
        colorAnim.duration = resources.getInteger(R.integer.theme_change_duration).toLong()
        colorAnim.interpolator = DecelerateInterpolator()
        colorAnim.addUpdateListener { animation -> this.backgroundTintList = ColorStateList.valueOf(animation.animatedValue as Int) }
        colorAnim.start()
    }

    fun View.animateColorChange(endColor: Int) {
        val colorAnim = ValueAnimator.ofObject(ArgbEvaluatorCompat(), this.backgroundTintList?.defaultColor, endColor)
        colorAnim.duration = resources.getInteger(R.integer.theme_change_duration).toLong()
        colorAnim.interpolator = DecelerateInterpolator()
        colorAnim.addUpdateListener { animation -> this.backgroundTintList = ColorStateList.valueOf(animation.animatedValue as Int) }
        colorAnim.start()
    }

    fun TextView.animateColorChange(endColor: Int) {
        val colorAnim = ValueAnimator.ofObject(ArgbEvaluatorCompat(), this.currentTextColor, endColor)
        colorAnim.duration = resources.getInteger(R.integer.theme_change_duration).toLong()
        colorAnim.interpolator = DecelerateInterpolator(1.5F)
        colorAnim.addUpdateListener { animation -> this.setTextColor(animation.animatedValue as Int) }
        colorAnim.start()
    }

    fun AppCompatButton.animateColorChange(endColor: Int) {
        val colorAnim = ValueAnimator.ofObject(ArgbEvaluatorCompat(), this.backgroundTintList!!.defaultColor, endColor)
        colorAnim.duration = resources.getInteger(R.integer.theme_change_duration).toLong()
        colorAnim.interpolator = DecelerateInterpolator(1.5F)
        colorAnim.addUpdateListener { animation -> this.backgroundTintList = ColorStateList.valueOf(animation.animatedValue as Int) }
        colorAnim.start()
    }

    fun AppCompatTextView.animateDrawableColorChange(startColor: Int, endColor: Int) {
        val colorAnim = ValueAnimator.ofObject(ArgbEvaluatorCompat(), startColor, endColor)
        colorAnim.duration = resources.getInteger(R.integer.theme_change_duration).toLong()
        colorAnim.interpolator = DecelerateInterpolator(1.5F)
        colorAnim.addUpdateListener { animation ->
            TextViewCompat.setCompoundDrawableTintList(this, ColorStateList.valueOf(animation.animatedValue as Int))
        }
        colorAnim.start()
    }
}