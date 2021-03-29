package app.simple.inure.popups.dialogs

import android.view.View
import android.widget.TextView
import app.simple.inure.R
import app.simple.inure.decorations.animatedbackground.AnimatedBackgroundTextView
import app.simple.inure.decorations.popup.BasePopupWindow
import app.simple.inure.decorations.popup.PopupMenuCallback

class AppCategoryPopup(contentView: View, view: View) : BasePopupWindow() {

    private lateinit var popupMenuCallback: PopupMenuCallback

    init {

        init(contentView, view)

        contentView.findViewById<AnimatedBackgroundTextView>(R.id.popup_category_system).onClick(SYSTEM)
        contentView.findViewById<AnimatedBackgroundTextView>(R.id.popup_category_user).onClick(USER)
        contentView.findViewById<AnimatedBackgroundTextView>(R.id.popup_category_both).onClick(BOTH)
    }

    override fun dismiss() {
        super.dismiss()
        popupMenuCallback.onDismiss()
    }

    private fun TextView.onClick(category: String) {
        this.setOnClickListener {
            popupMenuCallback.onMenuItemClicked(category)
            dismiss()
        }
    }

    fun setOnMenuItemClickListener(popupMenuCallback: PopupMenuCallback) {
        this.popupMenuCallback = popupMenuCallback
    }

    companion object {
        const val SYSTEM = "system"
        const val USER = "user"
        const val BOTH = "both"
    }
}