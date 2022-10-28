package app.simple.inure.popups.stacktraces

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.popup.BasePopupWindow
import app.simple.inure.extensions.popup.PopupLinearLayout

class PopupStackTracesMenu(view: View) : BasePopupWindow() {

    private val delete: DynamicRippleTextView

    init {
        val contentView = LayoutInflater.from(view.context).inflate(R.layout.popup_stacktrace_menu, PopupLinearLayout(view.context))

        delete = contentView.findViewById(R.id.popup_delete)

        init(contentView, view, Gravity.END)
    }

    fun setOnDeleteListener(onClickListener: () -> Unit) {
        delete.setOnClickListener {
            onClickListener()
            dismiss()
        }
    }
}