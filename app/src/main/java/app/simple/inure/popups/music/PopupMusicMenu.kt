package app.simple.inure.popups.music

import android.net.Uri
import android.view.View
import app.simple.inure.R
import app.simple.inure.constants.Misc
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.popup.BasePopupWindow
import app.simple.inure.extensions.popup.PopupLinearLayout
import app.simple.inure.interfaces.menus.PopupMusicMenuCallbacks

class PopupMusicMenu(view: View, uri: Uri) : BasePopupWindow() {

    private val play: DynamicRippleTextView
    private val delete: DynamicRippleTextView
    private val share: DynamicRippleTextView

    private var popupMusicMenuCallbacks: PopupMusicMenuCallbacks? = null

    init {
        val contentView = View.inflate(view.context, R.layout.popup_music_menu, PopupLinearLayout(view.context))

        play = contentView.findViewById(R.id.popup_play)
        delete = contentView.findViewById(R.id.popup_delete)
        share = contentView.findViewById(R.id.popup_send)

        play.setOnClickListener {
            popupMusicMenuCallbacks?.onPlay(uri)
            dismiss()
        }

        delete.setOnClickListener {
            popupMusicMenuCallbacks?.onDelete(uri)
            dismiss()
        }

        share.setOnClickListener {
            popupMusicMenuCallbacks?.onShare(uri)
            dismiss()
        }

        init(contentView, view, Misc.xOffset, Misc.yOffset)
    }

    fun setOnPopupMusicMenuCallbacks(popupMusicMenuCallbacks: PopupMusicMenuCallbacks) {
        this.popupMusicMenuCallbacks = popupMusicMenuCallbacks
    }
}