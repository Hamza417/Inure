package app.simple.inure.popups.terminal

import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import app.simple.inure.R
import app.simple.inure.constants.Misc
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.popup.BasePopupWindow
import app.simple.inure.extensions.popup.PopupLinearLayout

class PopupTerminalCommands(view: View) : BasePopupWindow() {

    private val delete: DynamicRippleTextView
    private val run: DynamicRippleTextView
    private val edit: DynamicRippleTextView

    init {
        val contentView = LayoutInflater.from(view.context)
            .inflate(R.layout.popup_terminal_commands, PopupLinearLayout(view.context, LinearLayout.VERTICAL))

        delete = contentView.findViewById(R.id.popup_delete)
        run = contentView.findViewById(R.id.popup_run)
        edit = contentView.findViewById(R.id.popup_edit)

        init(contentView, view, Misc.xOffset, Misc.yOffset)
    }

    fun setOnPopupNotesMenuCallbackListener(popupTerminalCommandsCallbacks: PopupTerminalCommandsCallbacks) {
        delete.setOnClickListener {
            popupTerminalCommandsCallbacks.onDeleteClicked().also {
                dismiss()
            }
        }

        run.setOnClickListener {
            popupTerminalCommandsCallbacks.onRunClicked().also {
                dismiss()
            }
        }

        edit.setOnClickListener {
            popupTerminalCommandsCallbacks.onEditClicked().also {
                dismiss()
            }
        }
    }

    companion object {
        interface PopupTerminalCommandsCallbacks {
            fun onDeleteClicked()
            fun onRunClicked()
            fun onEditClicked()
        }
    }
}