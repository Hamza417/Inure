package app.simple.inure.popups.stacktraces

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.popup.BasePopupWindow
import app.simple.inure.extensions.popup.PopupLinearLayout
import app.simple.inure.interfaces.popups.PopupStackTracesCallbacks

class PopupStackTracesMenu(view: View) : BasePopupWindow() {

    private val delete: DynamicRippleTextView
    private val send: DynamicRippleTextView
    private val open: DynamicRippleTextView
    private val copy: DynamicRippleTextView

    private var popupStackTracesCallbacks: PopupStackTracesCallbacks? = null

    init {
        val contentView = LayoutInflater.from(view.context).inflate(R.layout.popup_stacktrace_menu, PopupLinearLayout(view.context))

        delete = contentView.findViewById(R.id.popup_delete)
        send = contentView.findViewById(R.id.popup_send)
        open = contentView.findViewById(R.id.popup_open)
        copy = contentView.findViewById(R.id.popup_copy)

        delete.setOnClickListener {
            popupStackTracesCallbacks?.onDelete()
            dismiss()
        }

        send.setOnClickListener {
            popupStackTracesCallbacks?.onSend()
            dismiss()
        }

        open.setOnClickListener {
            popupStackTracesCallbacks?.onOpen()
            dismiss()
        }

        copy.setOnClickListener {
            popupStackTracesCallbacks?.onCopy()
            dismiss()
        }

        init(contentView, view, Gravity.END)
    }

    fun setOnPopupStackTracesCallbacks(popupStackTracesCallbacks: PopupStackTracesCallbacks) {
        this.popupStackTracesCallbacks = popupStackTracesCallbacks
    }
}