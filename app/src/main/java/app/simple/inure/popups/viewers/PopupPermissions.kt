package app.simple.inure.popups.viewers

import android.view.Gravity
import android.view.View
import app.simple.inure.R
import app.simple.inure.decorations.popup.BasePopupWindow
import app.simple.inure.decorations.popup.PopupMenuCallback
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.model.PermissionInfo

class PopupPermissions(contentView: View, view: View, val permissionInfo: PermissionInfo) : BasePopupWindow() {

    private lateinit var popupMainMenuCallbacks: PopupMenuCallback

    init {
        init(contentView, view, Gravity.END)

        val context = contentView.context
        val revoke = contentView.findViewById<DynamicRippleTextView>(R.id.popup_revoke)

        revoke.text = if (permissionInfo.isGranted) {
            context.getString(R.string.revoke)
        } else {
            context.getString(R.string.grant)
        }

        revoke.onClick(revoke.text.toString())

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