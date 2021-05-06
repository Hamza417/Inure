package app.simple.inure.popups.app

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import app.simple.inure.R
import app.simple.inure.decorations.popup.BasePopupWindow
import app.simple.inure.glide.util.ImageLoader.loadGraphics

class PopupImageViewer(contentView: View, view: ViewGroup, path: String, filePath: String, xOff: Float, yOff: Float) : BasePopupWindow() {
    init {
        init(contentView, view, xOff, yOff)

        val image = contentView.findViewById<ImageView>(R.id.popup_image)

        image.loadGraphics(path, filePath)
    }
}