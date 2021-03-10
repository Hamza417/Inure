package app.simple.inure.decorations.popups

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupWindow
import android.widget.TextView
import app.simple.inure.R
import app.simple.inure.interfaces.menu.PopupMenuCallback

class BasePopupWindow(contentView: View, viewGroup: ViewGroup, xOff: Float, yOff: Float) : PopupWindow() {

    init {
        setContentView(contentView)
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        width = contentView.measuredWidth
        height = contentView.measuredHeight
        animationStyle = R.style.PopupAnimation
        isClippingEnabled = false
        isFocusable = true
        elevation = 100F

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            overlapAnchor = false
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            setIsClippedToScreen(false)
            setIsLaidOutInScreen(true)
        }

        //println("${xOff.toInt() - width / 2} & ${yOff.toInt() - height / 2}")

        // TODO - fix vertical positioning issue when view is opened at the bottom of the screen
        showAsDropDown(viewGroup, xOff.toInt() - width / 2, yOff.toInt() - height / 2, Gravity.NO_GRAVITY)
    }

    override fun showAsDropDown(anchor: View?, xoff: Int, yoff: Int, gravity: Int) {
        super.showAsDropDown(anchor, xoff, yoff, gravity)
        dimBehind()
    }

    /**
     * Dim the background when PopupWindow shows
     * Should be called from [showAsDropDown] function
     * because this is when container's parent is
     * initialized
     */
    private fun dimBehind() {
        val container = contentView.rootView
        val windowManager = contentView.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val layoutParams = container.layoutParams as WindowManager.LayoutParams
        layoutParams.flags = layoutParams.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
        layoutParams.dimAmount = 0.3f
        windowManager.updateViewLayout(container, layoutParams)
    }
}
