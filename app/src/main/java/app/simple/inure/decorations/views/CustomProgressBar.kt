package app.simple.inure.decorations.views

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ProgressBar
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import app.simple.inure.R
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible

class CustomProgressBar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : ProgressBar(context, attrs, defStyleAttr) {

    private var valueAnimator: ValueAnimator? = null
    private val duration = 500L

    /**
     * Set progress but with animation for all API levels
     *
     * @param progress progress of the seekbar
     * @param animate animate the progress change
     * @param fromStart start from the beginning or start from the already progressed value
     */
    fun setProgress(progress: Int, animate: Boolean, fromStart: Boolean) {
        if (animate) {
            valueAnimator = ValueAnimator.ofInt(if (fromStart) 0 else this.progress, progress)
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

    fun hide() {
        animate()
                .alpha(0F)
                .setInterpolator(AccelerateInterpolator())
                .setDuration(duration)
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator?) {
                        /* no-op */
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        gone()
                    }

                    override fun onAnimationCancel(animation: Animator?) {
                        /* no-op */
                    }

                    override fun onAnimationRepeat(animation: Animator?) {
                        /* no-op */
                    }
                })
                .start()
    }

    fun show() {
        animate()
                .alpha(1F)
                .setInterpolator(DecelerateInterpolator())
                .setDuration(duration)
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator?) {
                        visible()
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        /* no-op */
                    }

                    override fun onAnimationCancel(animation: Animator?) {
                        /* no-op */
                    }

                    override fun onAnimationRepeat(animation: Animator?) {
                        /* no-op */
                    }
                })
                .start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        clearAnimation()
        valueAnimator?.cancel()
    }

    override fun clearAnimation() {
        valueAnimator?.removeAllUpdateListeners()
        valueAnimator?.cancel()
        super.clearAnimation()
    }
}