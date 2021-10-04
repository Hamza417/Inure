package app.simple.inure.popups.viewers

import android.view.Gravity
import android.view.View
import app.simple.inure.R
import app.simple.inure.decorations.popup.BasePopupWindow
import app.simple.inure.decorations.popup.PopupMenuCallback
import app.simple.inure.decorations.ripple.DynamicRippleTextView

class PopupActivitiesMenu(contentView: View, view: View, isComponentEnabled: Boolean) : BasePopupWindow() {

    private lateinit var popupMainMenuCallbacks: PopupMenuCallback
    private var componentState: DynamicRippleTextView

    init {
        init(contentView, view, Gravity.END)

        val context = contentView.context

        componentState = contentView.findViewById(R.id.popup_component_state_toggle)

        componentState.text = if (isComponentEnabled) {
            contentView.context.getString(R.string.disable)
        } else {
            contentView.context.getString(R.string.enable)
        }.also {
            componentState.onClick(it)
        }

        contentView.findViewById<DynamicRippleTextView>(R.id.popup_launch).onClick(context.getString(R.string.force_launch))
        contentView.findViewById<DynamicRippleTextView>(R.id.popup_launch_with_action).onClick(context.getString(R.string.force_launch_with_action))

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