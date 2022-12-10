package app.simple.inure.popups.bootmanager

import android.view.Gravity
import android.view.View
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.popup.BasePopupWindow
import app.simple.inure.extensions.popup.PopupLinearLayout

class PopupBootManager(view: View) : BasePopupWindow() {

    private val enableAll: DynamicRippleTextView
    private val disableAll: DynamicRippleTextView
    private var popupBootManagerCallbacks: PopupBootManagerCallbacks? = null

    init {
        val contentView = View.inflate(view.context, R.layout.popup_boot_manager, PopupLinearLayout(view.context))

        contentView.apply {
            enableAll = findViewById(R.id.popup_enable_all)
            disableAll = findViewById(R.id.popup_disable_all)
        }

        enableAll.setOnClickListener {
            popupBootManagerCallbacks?.onEnableAllClicked()
            dismiss()
        }

        disableAll.setOnClickListener {
            popupBootManagerCallbacks?.onDisableAllClicked()
            dismiss()
        }

        init(contentView, view, Gravity.END)
    }

    fun setOnPopupBootManagerCallbacks(popupBootManagerCallbacks: PopupBootManagerCallbacks) {
        this.popupBootManagerCallbacks = popupBootManagerCallbacks
    }

    companion object {
        interface PopupBootManagerCallbacks {
            fun onEnableAllClicked()
            fun onDisableAllClicked()
        }
    }
}