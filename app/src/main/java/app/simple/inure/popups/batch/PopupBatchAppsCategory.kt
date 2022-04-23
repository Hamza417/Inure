package app.simple.inure.popups.batch

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extension.popup.BasePopupWindow
import app.simple.inure.extension.popup.PopupLinearLayout
import app.simple.inure.popups.apps.PopupAppsCategory.Companion.BOTH
import app.simple.inure.popups.apps.PopupAppsCategory.Companion.SYSTEM
import app.simple.inure.popups.apps.PopupAppsCategory.Companion.USER
import app.simple.inure.preferences.BatchPreferences

class PopupBatchAppsCategory(view: View) : BasePopupWindow() {

    private val system: DynamicRippleTextView
    private val user: DynamicRippleTextView
    private val both: DynamicRippleTextView

    init {
        val contentView = LayoutInflater.from(view.context).inflate(R.layout.popup_apps_category, PopupLinearLayout(view.context))

        system = contentView.findViewById(R.id.popup_category_system)
        user = contentView.findViewById(R.id.popup_category_user)
        both = contentView.findViewById(R.id.popup_category_both)

        system.onClick(SYSTEM)
        user.onClick(USER)
        both.onClick(BOTH)

        when (BatchPreferences.getAppsCategory()) {
            USER -> user.isSelected = true
            SYSTEM -> system.isSelected = true
            BOTH -> both.isSelected = true
        }

        init(contentView, view)
    }

    private fun TextView.onClick(category: String) {
        this.setOnClickListener {
            BatchPreferences.setAppsCategory(category)
            dismiss()
        }
    }
}