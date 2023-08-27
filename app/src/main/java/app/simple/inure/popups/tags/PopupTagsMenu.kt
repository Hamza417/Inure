package app.simple.inure.popups.tags

import android.view.LayoutInflater
import android.view.View
import app.simple.inure.R
import app.simple.inure.constants.Misc
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.popup.BasePopupWindow
import app.simple.inure.extensions.popup.PopupLinearLayout

class PopupTagsMenu(view: View, tagsMenuCallback: TagsMenuCallback) : BasePopupWindow() {

    private val delete: DynamicRippleTextView

    init {
        val containerView = LayoutInflater.from(view.context)
            .inflate(R.layout.popup_tag_menu, PopupLinearLayout(view.context), true)

        delete = containerView.findViewById(R.id.delete)

        delete.setOnClickListener {
            tagsMenuCallback.onDeleteClicked()
            dismiss()
        }

        init(containerView, view, Misc.xOffset, Misc.yOffset)
    }

    companion object {
        interface TagsMenuCallback {
            fun onDeleteClicked()
        }
    }
}