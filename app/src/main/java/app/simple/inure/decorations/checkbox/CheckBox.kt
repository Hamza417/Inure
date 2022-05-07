package app.simple.inure.decorations.checkbox

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import app.simple.inure.R
import app.simple.inure.decorations.switchview.SwitchCallbacks
import app.simple.inure.themes.interfaces.ThemeChangedListener
import app.simple.inure.themes.manager.Theme
import app.simple.inure.themes.manager.ThemeManager
import app.simple.inure.util.ColorUtils.animateColorChange
import app.simple.inure.util.ColorUtils.resolveAttrColor
import app.simple.inure.util.ViewUtils

@SuppressLint("ClickableViewAccessibility")
class CheckBox @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : CheckBoxFrameLayout(context, attrs, defStyleAttr), ThemeChangedListener {

    private var thumb: ImageView
    private var switchCallbacks: SwitchCallbacks? = null

    private val tension = 3.5F

    private var isChecked: Boolean = false

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.radio_button_view, this, true)

        thumb = view.findViewById(R.id.switch_thumb)

        clipChildren = false
        clipToPadding = false
        clipToOutline = false

        ViewUtils.addShadow(this)

        setOnClickListener {
            isChecked = if (isChecked) {
                animateUnchecked()
                switchCallbacks?.onCheckedChanged(false)
                false
            } else {
                animateChecked()
                switchCallbacks?.onCheckedChanged(true)
                true
            }
        }

        unchecked()
        requestLayout()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                thumb.animate()
                    .scaleY(1.5F)
                    .scaleX(1.5F)
                    .setInterpolator(DecelerateInterpolator(1.5F))
                    .setDuration(500L)
                    .start()
            }
            MotionEvent.ACTION_MOVE,
            MotionEvent.ACTION_UP,
            -> {
                thumb.animate()
                    .scaleY(1.0F)
                    .scaleX(1.0F)
                    .setInterpolator(DecelerateInterpolator(1.5F))
                    .setDuration(500L)
                    .start()
            }
        }

        return super.onTouchEvent(event)
    }

    fun setChecked(boolean: Boolean) {
        isChecked = if (boolean) {
            animateChecked()
            boolean
        } else {
            animateUnchecked()
            boolean
        }
    }

    fun setCheckedWithoutAnimations(boolean: Boolean) {
        isChecked = if (boolean) {
            checked()
            boolean
        } else {
            unchecked()
            boolean
        }
    }

    private fun animateUnchecked() {
        thumb.animate()
            .scaleX(0F)
            .scaleY(0F)
            .alpha(0F)
            .setInterpolator(DecelerateInterpolator())
            .setDuration(500L)
            .start()

        animateColorChange(ThemeManager.theme.switchViewTheme.switchOffColor)
        animateElevation(0F)
    }

    private fun animateChecked() {
        thumb.animate()
            .scaleX(1F)
            .scaleY(1F)
            .alpha(1F)
            .setInterpolator(OvershootInterpolator(tension))
            .setDuration(500L)
            .start()

        animateColorChange(context.resolveAttrColor(R.attr.colorAppAccent))
        animateElevation(25F)
    }

    private fun unchecked() {
        thumb.scaleX = 0F
        thumb.scaleY = 0F
        this.backgroundTintList = ColorStateList.valueOf(ThemeManager.theme.switchViewTheme.switchOffColor)
        elevation = 0F
    }

    private fun checked() {
        thumb.scaleX = 1F
        thumb.scaleY = 1F
        this.backgroundTintList = ColorStateList.valueOf(context.resolveAttrColor(R.attr.colorAppAccent))
        elevation = 25F
    }

    private fun animateElevation(elevation: Float) {
        val valueAnimator = ValueAnimator.ofFloat(this.elevation, elevation)
        valueAnimator.duration = 500L
        valueAnimator.interpolator = LinearOutSlowInInterpolator()
        valueAnimator.addUpdateListener {
            this.elevation = it.animatedValue as Float
        }
        valueAnimator.start()
    }

    fun setOnCheckedChangeListener(switchCallbacks: SwitchCallbacks) {
        this.switchCallbacks = switchCallbacks
    }

    override fun onViewRemoved(child: View?) {
        super.onViewRemoved(child)
        thumb.clearAnimation()
    }

    /**
     * Inverts the switch's checked status. If the switch is checked then
     * it will be unchecked and vice-versa
     */
    fun invertCheckedStatus() {
        isChecked = if (isChecked) {
            animateUnchecked()
            switchCallbacks?.onCheckedChanged(false)
            false
        } else {
            animateChecked()
            switchCallbacks?.onCheckedChanged(true)
            true
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        ThemeManager.addListener(this)
    }

    override fun onThemeChanged(theme: Theme, animate: Boolean) {
        if (!isChecked) {
            if (animate) {
                animateUnchecked()
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        ThemeManager.removeListener(this)
    }
}