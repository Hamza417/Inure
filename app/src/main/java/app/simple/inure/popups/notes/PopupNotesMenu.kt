package app.simple.inure.popups.notes

import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import app.simple.inure.R
import app.simple.inure.constants.Misc
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.popup.BasePopupWindow
import app.simple.inure.extensions.popup.PopupLinearLayout

class PopupNotesMenu(view: View) : BasePopupWindow() {

    private val delete: DynamicRippleTextView
    private val open: DynamicRippleTextView
    private val edit: DynamicRippleTextView
    private val share: DynamicRippleTextView

    init {
        val contentView = LayoutInflater.from(view.context)
            .inflate(R.layout.popup_notes_menu, PopupLinearLayout(view.context, LinearLayout.VERTICAL))

        delete = contentView.findViewById(R.id.popup_delete)
        open = contentView.findViewById(R.id.popup_open)
        edit = contentView.findViewById(R.id.popup_edit)
        share = contentView.findViewById(R.id.popup_share)

        init(contentView, view, Misc.xOffset, Misc.yOffset)
    }

    fun setOnPopupNotesMenuCallbackListener(popupNotesMenuCallback: PopupNotesMenuCallback) {
        delete.setOnClickListener {
            popupNotesMenuCallback.onDeleteClicked().also {
                dismiss()
            }
        }

        open.setOnClickListener {
            popupNotesMenuCallback.onOpenClicked().also {
                dismiss()
            }
        }

        edit.setOnClickListener {
            popupNotesMenuCallback.onEditClicked().also {
                dismiss()
            }
        }

        share.setOnClickListener {
            popupNotesMenuCallback.onShareClicked().also {
                dismiss()
            }
        }
    }

    companion object {
        interface PopupNotesMenuCallback {
            fun onDeleteClicked()
            fun onOpenClicked()
            fun onEditClicked()
            fun onShareClicked()
        }
    }
}