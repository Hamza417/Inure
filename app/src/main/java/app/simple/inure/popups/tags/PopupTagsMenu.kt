package app.simple.inure.popups.tags

import android.view.LayoutInflater
import android.view.View
import app.simple.inure.R
import app.simple.inure.constants.Misc
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.popup.BasePopupWindow
import app.simple.inure.extensions.popup.PopupLinearLayout

class PopupTagsMenu(view: View, tagsMenuCallback: TagsMenuCallback) : BasePopupWindow() {

    private val open: DynamicRippleTextView
    private val delete: DynamicRippleTextView
    private val createShortcut: DynamicRippleTextView

    init {
        val containerView = LayoutInflater.from(view.context)
            .inflate(R.layout.popup_tags_menu, PopupLinearLayout(view.context), true)

        open = containerView.findViewById(R.id.open)
        delete = containerView.findViewById(R.id.delete)
        createShortcut = containerView.findViewById(R.id.create_shortcut)

        open.setOnClickListener {
            tagsMenuCallback.onOpenClicked()
            dismiss()
        }

        delete.setOnClickListener {
            tagsMenuCallback.onDeleteClicked()
            dismiss()
        }

        createShortcut.setOnClickListener {
            tagsMenuCallback.onCreateShortcutClicked()
            dismiss()
        }

        init(containerView, view, Misc.xOffset, Misc.yOffset)
    }

    companion object {
        interface TagsMenuCallback {
            fun onOpenClicked()
            fun onDeleteClicked()
            fun onCreateShortcutClicked()
        }
    }
}