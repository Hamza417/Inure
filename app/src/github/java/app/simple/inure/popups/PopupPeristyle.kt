package app.simple.inure.popups

import android.view.View
import app.simple.inure.R
import app.simple.inure.constants.Misc
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.popup.BasePopupWindow
import app.simple.inure.extensions.popup.PopupLinearLayout

class PopupPeristyle(view: View) : BasePopupWindow() {

    private val github: DynamicRippleTextView
    private val fdroid: DynamicRippleTextView
    private val izzyondroid: DynamicRippleTextView
    private var popupLinksCallbacks: PopupLinksCallbacks? = null

    init {
        val contentView = View.inflate(view.context, R.layout.popup_links_peristyle, PopupLinearLayout(view.context))

        contentView.apply {
            github = findViewById(R.id.popup_github)
            fdroid = findViewById(R.id.popup_fdroid)
            izzyondroid = findViewById(R.id.popup_izzyondroid)
        }

        github.setOnClickListener {
            popupLinksCallbacks?.onGithubClicked()
            dismiss()
        }

        fdroid.setOnClickListener {
            popupLinksCallbacks?.onFdroidClicked()
            dismiss()
        }

        izzyondroid.setOnClickListener {
            popupLinksCallbacks?.onIzzyondroidClicked()
            dismiss()
        }

        init(contentView, view, Misc.xOffset, Misc.yOffset)
    }

    fun setPopupOtherAppsCallbacks(callbacks: PopupLinksCallbacks) {
        this.popupLinksCallbacks = callbacks
    }
}