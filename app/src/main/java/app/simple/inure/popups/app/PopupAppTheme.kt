package app.simple.inure.popups.app

import android.view.LayoutInflater
import android.view.View
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extension.popup.BasePopupWindow
import app.simple.inure.extension.popup.PopupLinearLayout
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.themes.manager.ThemeManager

class PopupAppTheme(view: View) : BasePopupWindow() {

    init {
        val contentView = LayoutInflater.from(view.context).inflate(R.layout.popup_application_theme, PopupLinearLayout(view.context))
        contentView.findViewById<DynamicRippleTextView>(R.id.popup_theme_dark).onClick(ThemeManager.dark)
        contentView.findViewById<DynamicRippleTextView>(R.id.popup_theme_light).onClick(ThemeManager.light)
        contentView.findViewById<DynamicRippleTextView>(R.id.popup_theme_amoled).onClick(ThemeManager.amoled)
        contentView.findViewById<DynamicRippleTextView>(R.id.popup_theme_day_night).onClick(ThemeManager.dayNight)
        contentView.findViewById<DynamicRippleTextView>(R.id.popup_theme_follow_system).onClick(ThemeManager.followSystem)

        init(contentView, view)
    }

    private fun DynamicRippleTextView.onClick(theme: Int) {
        this.setOnClickListener {
            AppearancePreferences.setTheme(theme)
            dismiss()
        }
    }
}
