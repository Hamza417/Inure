package app.simple.inure.popups.viewers

import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import app.simple.inure.R
import app.simple.inure.constants.Misc
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.popup.BasePopupWindow
import app.simple.inure.extensions.popup.PopupLinearLayout

class PopupSharedPreferences(view: View) : BasePopupWindow() {

    private val delete: DynamicRippleTextView
    private val open: DynamicRippleTextView

    init {
        val contentView = LayoutInflater.from(view.context)
            .inflate(R.layout.popup_shared_prefs, PopupLinearLayout(view.context, LinearLayout.VERTICAL))

        delete = contentView.findViewById(R.id.popup_delete)
        open = contentView.findViewById(R.id.popup_open)

        init(contentView, view, Misc.xOffset, Misc.yOffset)
    }

    fun setOnPopupNotesMenuCallbackListener(popupSharedPrefsMenuCallback: PopupSharedPrefsMenuCallback) {
        delete.setOnClickListener {
            popupSharedPrefsMenuCallback.onDeleteClicked().also {
                dismiss()
            }
        }

        open.setOnClickListener {
            popupSharedPrefsMenuCallback.onOpenClicked().also {
                dismiss()
            }
        }
    }

    companion object {
        interface PopupSharedPrefsMenuCallback {
            fun onDeleteClicked()
            fun onOpenClicked()
        }
    }
}