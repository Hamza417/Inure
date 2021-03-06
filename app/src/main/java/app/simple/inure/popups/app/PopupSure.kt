package app.simple.inure.popups.app

import android.view.Gravity
import android.view.View
import app.simple.inure.R
import app.simple.inure.decorations.popup.BasePopupWindow
import app.simple.inure.decorations.popup.PopupMenuCallback
import app.simple.inure.decorations.ripple.DynamicRippleTextView

class PopupSure(contentView: View, view: View) : BasePopupWindow() {

    private lateinit var popupMainMenuCallbacks: PopupMenuCallback

    init {
        init(contentView, view, Gravity.TOP and Gravity.END)

        val context = contentView.context

        contentView.findViewById<DynamicRippleTextView>(R.id.sure_affirmative).onClick(context.getString(R.string.yes))

        contentView.findViewById<DynamicRippleTextView>(R.id.sure_negative).setOnClickListener {
            dismiss()
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