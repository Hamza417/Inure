package app.simple.inure.popups.analytics

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extension.popup.BasePopupWindow
import app.simple.inure.extension.popup.PopupLinearLayout
import app.simple.inure.preferences.AnalyticsPreferences
import kotlin.math.roundToInt

class PopupSdkValue(view: View) : BasePopupWindow() {

    private val code: DynamicRippleTextView
    private val name: DynamicRippleTextView

    init {
        val contentView = LayoutInflater.from(view.context).inflate(R.layout.popup_sdk_value, PopupLinearLayout(view.context))

        code = contentView.findViewById(R.id.version_code)
        name = contentView.findViewById(R.id.version_name)

        code.setOnClickListener {
            AnalyticsPreferences.setSDKValue(true).also {
                dismiss()
            }
        }

        name.setOnClickListener {
            AnalyticsPreferences.setSDKValue(false).also {
                dismiss()
            }
        }

        setContentView(contentView)
        init()
        showAsDropDown(view, (-width / 1.4).roundToInt(), height / 16, Gravity.NO_GRAVITY)
    }
}