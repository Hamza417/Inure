package app.simple.inure.popups.dialogs

import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import app.simple.inure.R
import app.simple.inure.decorations.popup.BasePopupWindow
import app.simple.inure.decorations.popup.PopupMenuCallback
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.preferences.MainPreferences
import app.simple.inure.util.Sort

class SortingStylePopup(contentView: View, view: View)
    : BasePopupWindow() {

    private lateinit var popupMenuCallback: PopupMenuCallback

    init {

        init(contentView, view)

        val name = contentView.findViewById<DynamicRippleTextView>(R.id.sort_name)
        val packageName = contentView.findViewById<DynamicRippleTextView>(R.id.sort_package_name)
        val size = contentView.findViewById<DynamicRippleTextView>(R.id.sort_app_size)
        val installDate = contentView.findViewById<DynamicRippleTextView>(R.id.sort_install_date)
        val reversedCheckBox = contentView.findViewById<CheckBox>(R.id.sort_reversed_checkbox)

        name.onClick(Sort.NAME)
        packageName.onClick(Sort.PACKAGE_NAME)
        size.onClick(Sort.SIZE)
        installDate.onClick(Sort.INSTALL_DATE)

        reversedCheckBox.isChecked = MainPreferences.isReverseSorting()

        contentView.findViewById<DynamicRippleTextView>(R.id.sort_reversed).setOnClickListener {
            reversedCheckBox.isChecked = !reversedCheckBox.isChecked
        }

        reversedCheckBox.setOnCheckedChangeListener { _, isChecked ->
            MainPreferences.setReverseSorting(isChecked)
        }
    }

    override fun dismiss() {
        super.dismiss()
        popupMenuCallback.onDismiss()
    }

    private fun TextView.onClick(style: String) {
        this.setOnClickListener {
            popupMenuCallback.onMenuItemClicked(style)
            dismiss()
        }
    }

    fun setOnMenuItemClickListener(popupMenuCallback: PopupMenuCallback) {
        this.popupMenuCallback = popupMenuCallback
    }
}