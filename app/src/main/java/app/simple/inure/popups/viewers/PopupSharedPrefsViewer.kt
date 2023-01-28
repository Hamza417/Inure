package app.simple.inure.popups.viewers

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.popup.BasePopupWindow
import app.simple.inure.extensions.popup.PopupLinearLayout

class PopupSharedPrefsViewer(view: View) : BasePopupWindow() {

    private lateinit var popupSharedPrefsCallbacks: PopupSharedPrefsCallbacks

    init {
        val contentView = LayoutInflater.from(view.context).inflate(R.layout.popup_shared_prefs_viewer, PopupLinearLayout(view.context))

        contentView.findViewById<DynamicRippleTextView>(R.id.popup_copy).onClick(contentView.context.getString(R.string.copy))
        contentView.findViewById<DynamicRippleTextView>(R.id.popup_save).onClick(contentView.context.getString(R.string.save))
        contentView.findViewById<DynamicRippleTextView>(R.id.popup_export).onClick(contentView.context.getString(R.string.export))

        init(contentView, view, Gravity.NO_GRAVITY)
    }

    private fun DynamicRippleTextView.onClick(s: String) {
        this.setOnClickListener {
            popupSharedPrefsCallbacks.onPopupItemClicked(s)
            dismiss()
        }
    }

    fun setOnPopupClickedListener(sharedPrefsCallbacks: PopupSharedPrefsCallbacks) {
        this.popupSharedPrefsCallbacks = sharedPrefsCallbacks
    }

    interface PopupSharedPrefsCallbacks {
        fun onPopupItemClicked(source: String)
    }
}