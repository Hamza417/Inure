package app.simple.inure.popups.terminal

import android.net.wifi.WifiManager
import android.os.PowerManager
import android.view.LayoutInflater
import android.view.View
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extension.popup.BasePopupWindow
import app.simple.inure.extension.popup.PopupLinearLayout
import app.simple.inure.extension.popup.PopupMenuCallback

class PopupTerminal(view: View, mWakeLock: PowerManager.WakeLock, mWifiLock: WifiManager.WifiLock) : BasePopupWindow() {

    private lateinit var menuCallbacks: PopupMenuCallback

    private var windows: DynamicRippleTextView
    private var toggleKeyboard: DynamicRippleTextView
    private var specialKeys: DynamicRippleTextView
    private var preferences: DynamicRippleTextView
    private var reset: DynamicRippleTextView
    private var copy: DynamicRippleTextView
    private var wakeLock: DynamicRippleTextView
    private var wifiLock: DynamicRippleTextView

    init {
        val contentView = LayoutInflater.from(view.context).inflate(R.layout.popup_terminal_menu, PopupLinearLayout(view.context))
        val context = contentView.context

        windows = contentView.findViewById(R.id.terminal_windows)
        toggleKeyboard = contentView.findViewById(R.id.terminal_toggle_keyboard)
        specialKeys = contentView.findViewById(R.id.terminal_special_keys)
        preferences = contentView.findViewById(R.id.terminal_preferences)
        reset = contentView.findViewById(R.id.terminal_reset)
        copy = contentView.findViewById(R.id.terminal_copy)
        wakeLock = contentView.findViewById(R.id.terminal_wake_lock)
        wifiLock = contentView.findViewById(R.id.terminal_wifi_lock)

        if (mWakeLock.isHeld) {
            wakeLock.text = context.getString(R.string.disable_wakelock)
        } else {
            wakeLock.text = context.getString(R.string.enable_wakelock)
        }

        if (mWifiLock.isHeld) {
            wifiLock.text = context.getString(R.string.disable_wifilock)
        } else {
            wifiLock.text = context.getString(R.string.enable_wifilock)
        }

        windows.onClick(0)
        toggleKeyboard.onClick(1)
        specialKeys.onClick(2)
        preferences.onClick(3)
        reset.onClick(4)
        copy.onClick(5)
        wakeLock.onClick(6)
        wifiLock.onClick(7)

        init(contentView, view)
    }

    private fun DynamicRippleTextView.onClick(source: Int) {
        this.setOnClickListener {
            dismiss()
            menuCallbacks.onMenuItemClicked(source)
        }
    }

    fun setOnMenuClickListener(popupMainMenuCallbacks: PopupMenuCallback) {
        this.menuCallbacks = popupMainMenuCallbacks
    }
}