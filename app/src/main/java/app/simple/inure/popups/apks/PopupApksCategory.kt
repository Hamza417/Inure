package app.simple.inure.popups.apks

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.popup.BasePopupWindow
import app.simple.inure.extensions.popup.PopupLinearLayout
import app.simple.inure.preferences.ApkBrowserPreferences

class PopupApksCategory(view: View) : BasePopupWindow() {

    private val split: DynamicRippleTextView
    private val apk: DynamicRippleTextView
    private val both: DynamicRippleTextView

    init {
        val contentView = LayoutInflater.from(view.context).inflate(R.layout.popup_apks_filter, PopupLinearLayout(view.context))

        split = contentView.findViewById(R.id.split)
        apk = contentView.findViewById(R.id.apk)
        both = contentView.findViewById(R.id.both)

        split.onClick(SPLIT)
        apk.onClick(APK)
        both.onClick(BOTH)

        when (ApkBrowserPreferences.getAppsCategory()) {
            APK -> apk.isSelected = true
            SPLIT -> split.isSelected = true
            BOTH -> both.isSelected = true
        }

        init(contentView, view)
    }

    private fun TextView.onClick(category: String) {
        this.setOnClickListener {
            ApkBrowserPreferences.setAppsCategory(category)
            dismiss()
        }
    }

    companion object {
        const val SPLIT = "split"
        const val APK = "apk"
        const val BOTH = "both"
    }
}