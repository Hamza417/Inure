package app.simple.inure.popups.viewers

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import app.simple.inure.R
import app.simple.inure.decorations.views.CustomCheckBox
import app.simple.inure.extension.popup.BasePopupWindow
import app.simple.inure.extension.popup.PopupLinearLayout
import app.simple.inure.preferences.ImageViewerPreferences

class PopupImageViewerMenu(view: View) : BasePopupWindow() {
    init {
        val contentView = LayoutInflater.from(view.context).inflate(R.layout.popup_image_viewer_options, PopupLinearLayout(view.context))

        val checkBox = contentView.findViewById<CustomCheckBox>(R.id.image_background_checkbox)

        checkBox.isChecked = ImageViewerPreferences.isBackgroundDark()

        checkBox.setOnCheckedChangeListener { _, isChecked ->
            ImageViewerPreferences.setBackgroundMode(isChecked)
        }

        init(contentView, view, Gravity.END)
    }
}