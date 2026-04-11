package app.simple.inure.popups

import android.view.View
import app.simple.inure.R
import app.simple.inure.constants.Misc
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.popup.BasePopupWindow
import app.simple.inure.extensions.popup.PopupLinearLayout

class PopupOtherApps(view: View) : BasePopupWindow() {

    private val github: DynamicRippleTextView
    private val fdroid: DynamicRippleTextView
    private val izzyondroid: DynamicRippleTextView
    private var popupOtherAppsCallbacks: PopupOtherAppsCallbacks? = null

    init {
        val contentView = View.inflate(view.context, R.layout.popup_other_apps, PopupLinearLayout(view.context))

        contentView.apply {
            github = findViewById(R.id.popup_github)
            fdroid = findViewById(R.id.popup_fdroid)
            izzyondroid = findViewById(R.id.popup_izzyondroid)
        }

        github.setOnClickListener {
            popupOtherAppsCallbacks?.onGithubClicked()
            dismiss()
        }

        fdroid.setOnClickListener {
            popupOtherAppsCallbacks?.onFdroidClicked()
            dismiss()
        }

        izzyondroid.setOnClickListener {
            popupOtherAppsCallbacks?.onIzzyondroidClicked()
            dismiss()
        }

        init(contentView, view, Misc.xOffset, Misc.yOffset)
    }

    fun setPopupOtherAppsCallbacks(callbacks: PopupOtherAppsCallbacks) {
        this.popupOtherAppsCallbacks = callbacks
    }

    companion object {
        interface PopupOtherAppsCallbacks {
            fun onGithubClicked()
            fun onFdroidClicked()
            fun onIzzyondroidClicked()
        }
    }
}