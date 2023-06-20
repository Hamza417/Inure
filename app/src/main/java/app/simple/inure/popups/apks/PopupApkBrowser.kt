package app.simple.inure.popups.apks

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.popup.BasePopupWindow
import app.simple.inure.extensions.popup.PopupLinearLayout

class PopupApkBrowser(view: View) : BasePopupWindow() {

    private val install: DynamicRippleTextView
    private val delete: DynamicRippleTextView
    private val send: DynamicRippleTextView
    private val manifest: DynamicRippleTextView
    private val info: DynamicRippleTextView
    private val select: DynamicRippleTextView

    private var popupApkBrowserCallbacks: PopupApkBrowserCallbacks? = null

    init {
        val contentView = LayoutInflater.from(view.context).inflate(R.layout.popup_apk_browser_menu, PopupLinearLayout(view.context))

        install = contentView.findViewById(R.id.popup_install)
        delete = contentView.findViewById(R.id.popup_delete)
        send = contentView.findViewById(R.id.popup_send)
        manifest = contentView.findViewById(R.id.popup_manifest)
        info = contentView.findViewById(R.id.popup_info)
        select = contentView.findViewById(R.id.popup_select)

        install.setOnClickListener {
            popupApkBrowserCallbacks?.onInstallClicked()
            dismiss()
        }

        delete.setOnClickListener {
            popupApkBrowserCallbacks?.onDeleteClicked()
            dismiss()
        }

        send.setOnClickListener {
            popupApkBrowserCallbacks?.onSendClicked()
            dismiss()
        }

        manifest.setOnClickListener {
            popupApkBrowserCallbacks?.onManifestClicked()
            dismiss()
        }

        info.setOnClickListener {
            popupApkBrowserCallbacks?.onInfoClicked()
            dismiss()
        }

        select.setOnClickListener {
            popupApkBrowserCallbacks?.onSelectClicked()
            dismiss()
        }

        init(contentView, view, Gravity.END)
    }

    fun setPopupApkBrowserCallbacks(popupApkBrowserCallbacks: PopupApkBrowserCallbacks) {
        this.popupApkBrowserCallbacks = popupApkBrowserCallbacks
    }

    companion object {
        interface PopupApkBrowserCallbacks {
            fun onInstallClicked()
            fun onDeleteClicked()
            fun onSendClicked()
            fun onManifestClicked()
            fun onInfoClicked()
            fun onSelectClicked()
        }
    }
}