package app.simple.inure.popups.app

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import app.simple.inure.R
import app.simple.inure.constants.ThemeConstants
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extension.popup.BasePopupWindow
import app.simple.inure.extension.popup.PopupLinearLayout
import app.simple.inure.preferences.AppearancePreferences

@SuppressLint("ClickableViewAccessibility")
class PopupAppTheme(view: View) : BasePopupWindow() {

    init {
        val contentView = LayoutInflater.from(view.context).inflate(R.layout.popup_application_theme, PopupLinearLayout(view.context))

        contentView.findViewById<DynamicRippleTextView>(R.id.popup_theme_dark).onClick(ThemeConstants.DARK_THEME)
        contentView.findViewById<DynamicRippleTextView>(R.id.popup_theme_light).onClick(ThemeConstants.LIGHT_THEME)
        contentView.findViewById<DynamicRippleTextView>(R.id.popup_theme_amoled).onClick(ThemeConstants.AMOLED)
        contentView.findViewById<DynamicRippleTextView>(R.id.popup_theme_day_night).onClick(ThemeConstants.DAY_NIGHT)
        contentView.findViewById<DynamicRippleTextView>(R.id.popup_theme_follow_system).onClick(ThemeConstants.FOLLOW_SYSTEM)
        contentView.findViewById<DynamicRippleTextView>(R.id.popup_theme_slate).onClick(ThemeConstants.SLATE)
        contentView.findViewById<DynamicRippleTextView>(R.id.popup_theme_high_contrast).onClick(ThemeConstants.HIGH_CONTRAST)

        init(contentView, view)
    }

    private fun DynamicRippleTextView.onClick(theme: Int) {
        this.setOnClickListener {
            AppearancePreferences.setTheme(theme)

            when (theme) {
                ThemeConstants.HIGH_CONTRAST,
                ThemeConstants.DARK_THEME,
                ThemeConstants.SLATE,
                ThemeConstants.AMOLED -> {
                    AppearancePreferences.setLastDarkTheme(theme)
                }
            }

            dismiss()
        }
    }
}