package app.simple.inure.popups.app

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import app.simple.inure.R
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.popup.BasePopupWindow
import app.simple.inure.extensions.popup.PopupLinearLayout
import app.simple.inure.extensions.popup.PopupMenuCallback
import kotlin.math.roundToInt

class PopupHome(anchor: View) : BasePopupWindow() {

    private lateinit var popupMenuCallback: PopupMenuCallback

    init {
        val contentView = LayoutInflater.from(anchor.context).inflate(R.layout.popup_home_menu, PopupLinearLayout(anchor.context))

        contentView.findViewById<TypeFaceTextView>(R.id.popup_home_refresh)
            .onClick(R.string.refresh)

        contentView.findViewById<TypeFaceTextView>(R.id.popup_home_prefs)
            .onClick(R.string.preferences)

        setContentView(contentView)
        init()
        showAsDropDown(anchor, (-width / 1.4).roundToInt(), height / 16, Gravity.NO_GRAVITY)
    }

    fun TypeFaceTextView.onClick(string: Int) {
        setOnClickListener {
            dismiss()
            popupMenuCallback.onMenuItemClicked(string)
        }
    }

    fun setOnPopupMenuCallback(popupMenuCallback: PopupMenuCallback) {
        this.popupMenuCallback = popupMenuCallback
    }
}