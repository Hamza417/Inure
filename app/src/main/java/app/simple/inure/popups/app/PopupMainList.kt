package app.simple.inure.popups.app

import android.content.pm.ApplicationInfo
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import app.simple.inure.R
import app.simple.inure.decorations.popup.PopupMenuCallback
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.util.ViewUtils
import app.simple.inure.util.ViewUtils.dimBehind
import app.simple.inure.util.ViewUtils.makeGoAway

/**
 * A customised version of popup menu that uses [PopupWindow]
 * created to replace ugly material popup menu which does not
 * provide any customizable flexibility. This on the other hand
 * uses custom layout, background, animations and also dims entire
 * window when appears. It is highly recommended to use this
 * and ditch popup menu entirely.
 */
class PopupMainList(
        contentView: View,
        viewGroup: ViewGroup,
        xOff: Float,
        yOff: Float,
        private val applicationInfo: ApplicationInfo,
        private val icon: ImageView,
) : PopupWindow() {

    lateinit var popupMenuCallback: PopupMenuCallback

    init {
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        width = contentView.measuredWidth
        height = contentView.measuredHeight
        animationStyle = R.style.PopupAnimation
        isClippingEnabled = false
        isFocusable = true
        elevation = 50F
        ViewUtils.addShadow(contentView)

        contentView.findViewById<TypeFaceTextView>(R.id.menu_launch).onClick()
        contentView.findViewById<TypeFaceTextView>(R.id.menu_uninstall).onClick()
        contentView.findViewById<TypeFaceTextView>(R.id.menu_sort_name).onClick()
        contentView.findViewById<TypeFaceTextView>(R.id.menu_kill).onClick()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            overlapAnchor = false
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            setIsClippedToScreen(false)
            setIsLaidOutInScreen(true)
        }

        setContentView(contentView)

        showAsDropDown(viewGroup, xOff.toInt() - width / 2, yOff.toInt() - height, Gravity.NO_GRAVITY)
    }

    override fun showAsDropDown(anchor: View?, xoff: Int, yoff: Int, gravity: Int) {
        super.showAsDropDown(anchor, xoff, yoff, gravity)
        dimBehind(contentView)
    }

    private fun TextView.onClick() {
        this.setOnClickListener {
            popupMenuCallback.onMenuItemClicked(this.text.toString(), applicationInfo, icon)
            dismiss()
        }
    }

    fun setOnMenuItemClickListener(popupMenuCallback: PopupMenuCallback) {
        this.popupMenuCallback = popupMenuCallback
    }
}
