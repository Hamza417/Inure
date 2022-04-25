package app.simple.inure.popups.notes

import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.extension.popup.BasePopupWindow
import app.simple.inure.extension.popup.PopupLinearLayout
import kotlin.math.roundToInt

class PopupBackgroundSpan(anchor: View) : BasePopupWindow() {

    private var red: DynamicRippleImageButton
    private var purple: DynamicRippleImageButton
    private var blue: DynamicRippleImageButton
    private var green: DynamicRippleImageButton

    private var popupBackgroundSpanCallback: PopupBackgroundSpanCallback? = null

    init {
        val contentView = LayoutInflater.from(anchor.context)
            .inflate(R.layout.popup_background_span, PopupLinearLayout(anchor.context, LinearLayout.HORIZONTAL))

        red = contentView.findViewById(R.id.red)
        purple = contentView.findViewById(R.id.purple)
        blue = contentView.findViewById(R.id.blue)
        green = contentView.findViewById(R.id.green)

        red.setOnClickListener {
            popupBackgroundSpanCallback?.onColorClicked(Color.parseColor("#f1948a"))
        }

        purple.setOnClickListener {
            popupBackgroundSpanCallback?.onColorClicked(Color.parseColor("#d2b4de"))
        }

        blue.setOnClickListener {
            popupBackgroundSpanCallback?.onColorClicked(Color.parseColor("#aed6f1"))
        }

        green.setOnClickListener {
            popupBackgroundSpanCallback?.onColorClicked(Color.parseColor("#a2d9ce"))
        }

        setContentView(contentView)
        init()
        showAsDropDown(anchor, (-width / 1.4).roundToInt(), height / 16, Gravity.NO_GRAVITY)
    }

    fun setOnPopupBackgroundCallbackListener(popupBackgroundSpanCallback: PopupBackgroundSpanCallback) {
        this.popupBackgroundSpanCallback = popupBackgroundSpanCallback
    }

    companion object {
        interface PopupBackgroundSpanCallback {
            fun onColorClicked(color: Int)
        }
    }
}