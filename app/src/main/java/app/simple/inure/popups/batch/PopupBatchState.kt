package app.simple.inure.popups.batch

import android.view.Gravity
import android.view.View
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.popup.BasePopupWindow
import app.simple.inure.extensions.popup.PopupLinearLayout

class PopupBatchState(view: View) : BasePopupWindow() {

    private val enableAll: DynamicRippleTextView
    private val disableAll: DynamicRippleTextView
    private var popupBatchStateCallbacks: PopupBatchStateCallbacks? = null

    init {
        val contentView = View.inflate(
                view.context, R.layout.popup_batch_state, PopupLinearLayout(view.context))

        contentView.apply {
            enableAll = findViewById(R.id.popup_enable_all)
            disableAll = findViewById(R.id.popup_disable_all)
        }

        enableAll.setOnClickListener {
            popupBatchStateCallbacks?.onEnableAll()
            dismiss()
        }

        disableAll.setOnClickListener {
            popupBatchStateCallbacks?.onDisableAll()
            dismiss()
        }

        init(contentView, view, Gravity.END)
    }

    fun setOnPopupBatchStateCallbacks(popupBatchStateCallbacks: PopupBatchStateCallbacks) {
        this.popupBatchStateCallbacks = popupBatchStateCallbacks
    }

    companion object {
        interface PopupBatchStateCallbacks {
            fun onEnableAll()
            fun onDisableAll()
        }
    }
}