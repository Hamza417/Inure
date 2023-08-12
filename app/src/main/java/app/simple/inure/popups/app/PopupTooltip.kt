package app.simple.inure.popups.app

import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import app.simple.inure.R
import app.simple.inure.decorations.corners.DynamicCornerTextView
import app.simple.inure.extensions.popup.BasePopupWindow

class PopupTooltip(view: View) : BasePopupWindow() {

    private val handler = Handler(Looper.getMainLooper())

    init {
        isBlurEnabled = false
        val contentView: DynamicCornerTextView = View.inflate(view.context, R.layout.popup_tooltip, null) as DynamicCornerTextView

        contentView.text = view.contentDescription

        init(contentView, view, Gravity.BOTTOM or Gravity.START)

        postDelayed {
            dismiss()
        }

        setOnDismissListener {
            handler.removeCallbacksAndMessages(null)
        }
    }

    private fun postDelayed(function: () -> Unit) {
        handler.postDelayed(function, 2000)
    }
}