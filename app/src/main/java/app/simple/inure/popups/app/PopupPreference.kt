package app.simple.inure.popups.app

import android.view.View
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import app.simple.inure.R
import app.simple.inure.decorations.popup.BasePopupWindow
import app.simple.inure.decorations.popup.PopupMenuCallback
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import org.jetbrains.annotations.NotNull

class PopupPreference(contentView: View, view: View) : BasePopupWindow() {

    private lateinit var popupMainMenuCallbacks: PopupMenuCallback

    init {
        init(contentView, view)

        val context = contentView.context

        contentView.findViewById<DynamicRippleTextView>(R.id.menu_analytics).onClick(context.getString(R.string.device_analytics))
        contentView.findViewById<DynamicRippleTextView>(R.id.menu_terminal).onClick(context.getString(R.string.terminal))
        contentView.findViewById<DynamicRippleTextView>(R.id.menu_preferences).onClick(context.getString(R.string.preferences))
        contentView.findViewById<DynamicRippleTextView>(R.id.menu_usage_stats).onClick(context.getString(R.string.usage_statistics))

        setOnDismissListener {
           popupMainMenuCallbacks.onDismiss()
        }
    }

    private fun DynamicRippleTextView.onClick(string: String) {
        this.setOnClickListener {
            popupMainMenuCallbacks.onMenuItemClicked(string)
            dismiss()
        }
    }

    fun setOnMenuClickListener(popupMainMenuCallbacks: PopupMenuCallback) {
        this.popupMainMenuCallbacks = popupMainMenuCallbacks
    }
}