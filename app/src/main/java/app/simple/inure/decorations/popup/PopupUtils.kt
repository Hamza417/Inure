package app.simple.inure.decorations.popup

import android.content.Context
import android.view.View
import android.view.WindowManager

object PopupUtils {
    /**
     * Dim the background when PopupWindow shows
     * Should be called from [BasePopupWindow.showAsDropDown] function
     * because this is when container's parent is
     * initialized
     */
    fun dimBehind(contentView: View) {
        val container = contentView.rootView
        val windowManager = contentView.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val layoutParams = container.layoutParams as WindowManager.LayoutParams
        layoutParams.flags = layoutParams.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
        layoutParams.dimAmount = 0.3f
        windowManager.updateViewLayout(container, layoutParams)
    }
}