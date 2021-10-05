package app.simple.inure.popups.app

import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import app.simple.inure.R
import app.simple.inure.decorations.popup.BasePopupWindow
import app.simple.inure.decorations.popup.PopupLinearLayout
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.preferences.AppearancePreferences

class PopupAppTheme(view: View) : BasePopupWindow() {

    init {
        val contentView = LayoutInflater.from(view.context).inflate(R.layout.popup_application_theme, PopupLinearLayout(view.context))
        contentView.findViewById<DynamicRippleTextView>(R.id.popup_theme_dark).onClick(AppCompatDelegate.MODE_NIGHT_YES)
        contentView.findViewById<DynamicRippleTextView>(R.id.popup_theme_light).onClick(AppCompatDelegate.MODE_NIGHT_NO)
        contentView.findViewById<DynamicRippleTextView>(R.id.popup_theme_follow_system).onClick(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        init(contentView, view)
    }

    private fun DynamicRippleTextView.onClick(theme: Int) {
        this.setOnClickListener {
            AppearancePreferences.setAppTheme(theme)
            dismiss()
        }
    }
}
