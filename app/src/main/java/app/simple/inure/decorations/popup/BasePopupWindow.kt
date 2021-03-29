package app.simple.inure.decorations.popup

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import app.simple.inure.R
import app.simple.inure.decorations.popup.PopupUtils.dimBehind

/**
 * A customised version of popup menu that uses [PopupWindow]
 * created to replace ugly material popup menu which does not
 * provide any customizable flexibility. This on the other hand
 * uses custom layout, background, animations and also dims entire
 * window when appears. It is highly recommended to use this
 * and ditch popup menu entirely.
 */
open class BasePopupWindow : PopupWindow() {

    fun init(contentView: View, viewGroup: ViewGroup, xOff: Float, yOff: Float) {
        setContentView(contentView)
        init()
        showAsDropDown(viewGroup, xOff.toInt() - width, yOff.toInt() - height, Gravity.START)
    }

    fun init(contentView: View, view: View) {
        setContentView(contentView)
        init()
        showAsDropDown(view, (width / 1.2F * -1).toInt(), height / 4, Gravity.NO_GRAVITY)
    }

    private fun init() {
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        width = contentView.measuredWidth
        height = contentView.measuredHeight
        animationStyle = R.style.PopupAnimation
        isClippingEnabled = false
        isFocusable = true
        elevation = 25F

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            overlapAnchor = false
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            setIsClippedToScreen(false)
            setIsLaidOutInScreen(true)
        }
    }

    override fun showAsDropDown(anchor: View?, xoff: Int, yoff: Int, gravity: Int) {
        super.showAsDropDown(anchor, xoff, yoff, gravity)
        dimBehind(contentView)
    }

    override fun showAsDropDown(anchor: View?) {
        super.showAsDropDown(anchor)
        dimBehind(contentView)
    }
}
