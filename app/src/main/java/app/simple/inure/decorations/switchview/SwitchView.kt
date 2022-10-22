package app.simple.inure.decorations.switchview

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
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.themes.interfaces.ThemeChangedListener
import app.simple.inure.themes.manager.Theme
import app.simple.inure.themes.manager.ThemeManager
import app.simple.inure.util.ColorUtils.animateColorChange
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.LocaleHelper.isRTL
import app.simple.inure.util.ViewUtils

@SuppressLint("ClickableViewAccessibility")
class SwitchView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : SwitchFrameLayout(context, attrs, defStyleAttr), ThemeChangedListener {

    private var thumb: ImageView
    private var switchCallbacks: SwitchCallbacks? = null

    private val tension = 3.5F
    val w = context.resources.getDimensionPixelOffset(R.dimen.switch_width)
    val p = context.resources.getDimensionPixelOffset(R.dimen.switch_padding)
    private val thumbWidth = context.resources.getDimensionPixelOffset(R.dimen.switch_thumb_dimensions)

    private var isChecked: Boolean = false

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.switch_view, this, true)

        thumb = view.findViewById(R.id.switch_thumb)

        clipChildren = false
        clipToPadding = false
        clipToOutline = false

        if (isInEditMode.invert()) {
            ViewUtils.addShadow(this)
        }

        view.setOnClickListener {
            if (!isEnabled) return@setOnClickListener

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
        if (isEnabled) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    requestDisallowInterceptTouchEvent(true)
                    thumb.animate()
                        .scaleY(1.5F)
                        .scaleX(1.5F)
                        .setInterpolator(DecelerateInterpolator(1.5F))
                        .setDuration(500L)
                        .start()
                }
                MotionEvent.ACTION_MOVE,
                MotionEvent.ACTION_UP -> {
                    requestDisallowInterceptTouchEvent(false)
                    thumb.animate()
                        .scaleY(1.0F)
                        .scaleX(1.0F)
                        .setInterpolator(DecelerateInterpolator(1.5F))
                        .setDuration(500L)
                        .start()
                }
            }
        }

        return super.onTouchEvent(event)
    }

    /**
     * Change checked status of the switch.
     *
     * This method will animate the checked status, to
     * change without animation use [animateChecked] method.
     */
    fun setChecked(checked: Boolean) {
        isChecked = if (checked) {
            animateChecked()
            checked
        } else {
            animateUnchecked()
            checked
        }
    }

    /**
     * Change checked status of the switch without animation.
     *
     * This method will animate the checked status, to
     * change with animation use [setChecked] method.
     */
    fun staticChecked(boolean: Boolean) {
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
            .translationX(if (resources.isRTL()) (w - p * 2 - thumbWidth).toFloat() else 0F)
            .setInterpolator(OvershootInterpolator(tension))
            .setDuration(500)
            .start()

        animateColorChange(ThemeManager.theme.switchViewTheme.switchOffColor)
        animateElevation(0F)
    }

    private fun unchecked() {
        thumb.translationX = if (resources.isRTL()) (w - p * 2 - thumbWidth).toFloat() else 0F
        this.backgroundTintList = ColorStateList.valueOf(ThemeManager.theme.switchViewTheme.switchOffColor)
        this.elevation = 0F
    }

    private fun animateChecked() {
        thumb.animate()
            .translationX(if (resources.isRTL()) 0F else (w - p * 2 - thumbWidth).toFloat())
            .setInterpolator(OvershootInterpolator(tension))
            .setDuration(500)
            .start()

        animateColorChange(AppearancePreferences.getAccentColor())
        animateElevation(25F)
    }

    private fun checked() {
        thumb.translationX = if (resources.isRTL()) 0F else (w - p * 2 - thumbWidth).toFloat()
        this.backgroundTintList = ColorStateList.valueOf(AppearancePreferences.getAccentColor())
        this.elevation = 25F
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

    fun isChecked() = isChecked

    fun setOnSwitchCheckedChangeListener(switchCallbacks: SwitchCallbacks) {
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
        isChecked = !isChecked
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
        ThemeManager.addListener(this)
    }
}