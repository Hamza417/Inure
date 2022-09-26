package app.simple.inure.decorations.views

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.widget.ProgressBar
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import app.simple.inure.R
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.util.ColorUtils
import app.simple.inure.util.ConditionUtils.invert

class CustomProgressBar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : ProgressBar(context, attrs, defStyleAttr) {

    private var valueAnimator: ValueAnimator? = null

    init {
        if (isInEditMode.invert()) {
            progressTintList = ColorStateList.valueOf(AppearancePreferences.getAccentColor())
            indeterminateTintList = ColorStateList.valueOf(AppearancePreferences.getAccentColor())
            backgroundTintList = ColorStateList.valueOf(ColorUtils.lightenColor(AppearancePreferences.getAccentColor()))
        }
    }

    /**
     * Set progress but with animation for all API levels
     *
     * @param progress progress of the seekbar
     * @param animate animate the progress change
     */
    fun animateProgress(progress: Int, animate: Boolean = true) {
        if (animate) {
            valueAnimator = ValueAnimator.ofInt(this.progress, progress)
            valueAnimator?.interpolator = LinearOutSlowInInterpolator()
            valueAnimator?.duration = resources.getInteger(R.integer.animation_duration).toLong()
            valueAnimator?.addUpdateListener { animation -> setProgress(animation.animatedValue as Int) }
            valueAnimator?.start()
        } else {
            setProgress(progress)
        }
    }

    fun changeColor(color: Int, animate: Boolean) {
        if (animate) {
            valueAnimator = ValueAnimator.ofArgb(progressTintList!!.defaultColor, color)
            valueAnimator?.interpolator = LinearOutSlowInInterpolator()
            valueAnimator?.duration = resources.getInteger(R.integer.animation_duration).toLong()
            valueAnimator?.addUpdateListener { animation ->
                progressTintList = ColorStateList.valueOf(animation.animatedValue as Int)
            }
            valueAnimator?.start()
        } else {
            progressTintList = ColorStateList.valueOf(color)
        }
    }

    override fun onDetachedFromWindow() {
        clearAnimation()
        super.onDetachedFromWindow()
    }

    override fun clearAnimation() {
        valueAnimator?.removeAllUpdateListeners()
        valueAnimator?.cancel()
        super.clearAnimation()
    }
}