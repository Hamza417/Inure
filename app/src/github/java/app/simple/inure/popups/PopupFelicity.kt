package app.simple.inure.popups

import android.view.View
import app.simple.inure.R
import app.simple.inure.constants.Misc
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.popup.BasePopupWindow
import app.simple.inure.extensions.popup.PopupLinearLayout

class PopupFelicity(view: View) : BasePopupWindow() {

    private val github: DynamicRippleTextView
    private val playStore: DynamicRippleTextView
    private var popupLinksCallbacks: PopupLinksCallbacks? = null

    init {
        val contentView = View.inflate(view.context, R.layout.popup_links_felicity, PopupLinearLayout(view.context))

        contentView.apply {
            github = findViewById(R.id.popup_github)
            playStore = findViewById(R.id.popup_playstore)
        }

        github.setOnClickListener {
            popupLinksCallbacks?.onGithubClicked()
            dismiss()
        }

        playStore.setOnClickListener {
            popupLinksCallbacks?.onPlayStoreClicked()
            dismiss()
        }

        init(contentView, view, Misc.xOffset, Misc.yOffset)
    }

    fun setPopupOtherAppsCallbacks(callbacks: PopupLinksCallbacks) {
        this.popupLinksCallbacks = callbacks
    }
}