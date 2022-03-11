package app.simple.inure.popups.app

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import app.simple.inure.R
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extension.popup.BasePopupWindow
import app.simple.inure.extension.popup.PopupLinearLayout
import app.simple.inure.extension.popup.PopupMenuCallback
import kotlin.math.roundToInt

@Deprecated("")
class PopupAnalytics(anchor: View) : BasePopupWindow() {

    private lateinit var popupMenuCallback: PopupMenuCallback

    init {
        val contentView = LayoutInflater.from(anchor.context).inflate(R.layout.popup_analytics, PopupLinearLayout(anchor.context))

        contentView.findViewById<TypeFaceTextView>(R.id.popup_analytics_refresh)
            .onClick(contentView.context.getString(R.string.refresh))

        setContentView(contentView)
        init()
        showAsDropDown(anchor, (-width / 1.4).roundToInt(), height / 16, Gravity.NO_GRAVITY)
    }

    fun TypeFaceTextView.onClick(string: String) {
        setOnClickListener {
            dismiss()
            popupMenuCallback.onMenuItemClicked(string)
        }
    }

    fun setOnPopupMenuCallback(popupMenuCallback: PopupMenuCallback) {
        this.popupMenuCallback = popupMenuCallback
    }
}