package app.simple.inure.popups.batch

import android.view.Gravity
import android.view.View
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.popup.BasePopupWindow
import app.simple.inure.extensions.popup.PopupLinearLayout

class PopupBatchProfileMenu(view: View) : BasePopupWindow() {

    private val select: DynamicRippleTextView
    private val delete: DynamicRippleTextView

    private var callbacks: BatchProfileMenuCallbacks? = null

    init {
        val contentView = View.inflate(view.context, R.layout.popup_batch_profile, PopupLinearLayout(view.context))

        select = contentView.findViewById(R.id.select)
        delete = contentView.findViewById(R.id.delete)

        select.setOnClickListener {
            dismiss()
            callbacks?.onSelect()
        }

        delete.setOnClickListener {
            dismiss()
            callbacks?.onDelete()
        }

        init(contentView, view, Gravity.END)
    }

    fun setCallbacks(callbacks: BatchProfileMenuCallbacks) {
        this.callbacks = callbacks
    }

    companion object {
        interface BatchProfileMenuCallbacks {
            fun onSelect()
            fun onDelete()
        }
    }
}