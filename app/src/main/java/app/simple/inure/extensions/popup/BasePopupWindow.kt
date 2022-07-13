package app.simple.inure.extensions.popup

import android.animation.ValueAnimator
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import app.simple.inure.R
import app.simple.inure.util.ViewUtils
import app.simple.inure.util.ViewUtils.dimBehind

/**
 * A customised version of popup menu that uses [PopupWindow]
 * created to replace ugly material popup menu which does not
 * provide any customizable flexibility. This on the other hand
 * uses custom layout, background, animations and also dims entire
 * window when appears. It is highly recommended to use this
 * and ditch popup menu entirely.
 */
open class BasePopupWindow : PopupWindow() {

    private var valueAnimator: ValueAnimator? = null

    fun init(contentView: View, viewGroup: ViewGroup, xOff: Float, yOff: Float) {
        setContentView(contentView)
        init()
        showAsDropDown(viewGroup, xOff.toInt() - width / 2, yOff.toInt() - height, Gravity.START)
    }

    fun init(contentView: View, view: View) {
        setContentView(contentView)
        init()
        showAsDropDown(view, (width / 1.2F * -1).toInt(), -height / 8, Gravity.NO_GRAVITY)
    }

    fun init(contentView: View, view: View, gravity: Int) {
        setContentView(contentView)
        init()
        showAsDropDown(view, (width / 2F * -1).toInt(), 0, gravity)
    }

    fun init() {
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        contentView.clipToOutline = false
        width = contentView.measuredWidth
        height = contentView.measuredHeight
        animationStyle = R.style.PopupAnimation
        isClippingEnabled = false
        isFocusable = true
        elevation = 40F

        ViewUtils.addShadow(contentView)

        overlapAnchor = true

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            setIsClippedToScreen(false)
            setIsLaidOutInScreen(false)
        }
    }

    override fun showAsDropDown(anchor: View?, xoff: Int, yoff: Int, gravity: Int) {
        super.showAsDropDown(anchor, xoff, yoff, gravity)
        valueAnimator = animateElevation(50F)
        dimBehind(contentView)
    }

    override fun showAsDropDown(anchor: View?) {
        super.showAsDropDown(anchor)
        valueAnimator = animateElevation(50F)
        dimBehind(contentView)
    }

    override fun dismiss() {
        super.dismiss()
        valueAnimator?.cancel()
    }

    /**
     * Not working
     */
    private fun PopupWindow.animateElevation(elevation: Float): ValueAnimator? {
        val valueAnimator = ValueAnimator.ofFloat(0F, elevation)
        valueAnimator.interpolator = LinearOutSlowInInterpolator()
        valueAnimator.duration = this.contentView.resources.getInteger(R.integer.animation_duration).toLong()
        valueAnimator.addUpdateListener { animation ->
            this.elevation = animation.animatedValue as Float
        }
        valueAnimator.start()
        return valueAnimator
    }
}
