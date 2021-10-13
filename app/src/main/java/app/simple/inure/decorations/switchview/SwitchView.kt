package app.simple.inure.decorations.switchview

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import androidx.core.content.ContextCompat
import app.simple.inure.R
import app.simple.inure.decorations.ripple.Utils
import app.simple.inure.util.ColorUtils.resolveAttrColor
import app.simple.inure.util.ViewUtils

@SuppressLint("ClickableViewAccessibility")
class SwitchView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
) : SwitchFrameLayout(context, attrs, defStyleAttr) {

    private var thumb: ImageView
    private var track: SwitchFrameLayout
    private var switchCallbacks: SwitchCallbacks? = null

    private var isChecked: Boolean = false

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.switch_view, this, true)

        thumb = view.findViewById(R.id.switch_thumb)
        track = view.findViewById(R.id.switch_track)

        ViewUtils.addShadow(track)

        view.setOnClickListener {
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

    private fun animateUnchecked() {
        thumb.animate()
                .translationX(0F)
                .setInterpolator(OvershootInterpolator(3F))
                .setDuration(500)
                .start()

        Utils.animateBackground(ContextCompat.getColor(context, R.color.switchTrackOff), track)
        animateElevation(0F)
    }

    private fun animateChecked() {

        val w = context.resources.getDimensionPixelOffset(R.dimen.switch_width)
        val p = context.resources.getDimensionPixelOffset(R.dimen.switch_padding)
        val thumbWidth = context.resources.getDimensionPixelOffset(R.dimen.switch_thumb_dimensions)

        thumb.animate()
                .translationX((w - p * 2 - thumbWidth).toFloat())
                .setInterpolator(OvershootInterpolator(3F))
                .setDuration(500)
                .start()

        Utils.animateBackground(context.resolveAttrColor(R.attr.colorAppAccent), track)
        animateElevation(25F)
    }

    private fun animateElevation(elevation: Float) {
        val valueAnimator = ValueAnimator.ofFloat(track.elevation, elevation)
        valueAnimator.duration = 500L
        valueAnimator.interpolator = DecelerateInterpolator(1.5F)
        valueAnimator.addUpdateListener {
            track.elevation = it.animatedValue as Float
        }
        valueAnimator.start()
    }

    fun setOnSwitchCheckedChangeListener(switchCallbacks: SwitchCallbacks) {
        this.switchCallbacks = switchCallbacks
    }
}