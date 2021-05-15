package app.simple.inure.popups.app

import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatDelegate
import app.simple.inure.R
import app.simple.inure.decorations.corners.DynamicCornerLinearLayout
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.popup.BasePopupWindow
import app.simple.inure.preferences.AppearancePreferences

class PopupAppTheme(contentView: View, view: View) : BasePopupWindow() {

    init {
        init(contentView, view)

        contentView.findViewById<DynamicRippleTextView>(R.id.popup_theme_dark).onClick(AppCompatDelegate.MODE_NIGHT_YES)
        contentView.findViewById<DynamicRippleTextView>(R.id.popup_theme_light).onClick(AppCompatDelegate.MODE_NIGHT_NO)
        contentView.findViewById<DynamicRippleTextView>(R.id.popup_theme_follow_system).onClick(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    private fun DynamicRippleTextView.onClick(theme: Int) {
        this.setOnClickListener {
            AppearancePreferences.setAppTheme(theme)
            dismiss()
        }
    }
}
