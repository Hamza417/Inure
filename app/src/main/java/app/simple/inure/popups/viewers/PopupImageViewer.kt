package app.simple.inure.popups.viewers

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import app.simple.inure.R
import app.simple.inure.extension.popup.BasePopupWindow
import app.simple.inure.extension.popup.PopupLinearLayout
import app.simple.inure.glide.util.ImageLoader.loadGraphics

class PopupImageViewer(view: ViewGroup, path: String, filePath: String, xOff: Float, yOff: Float) : BasePopupWindow() {
    init {
        val contentView = LayoutInflater.from(view.context).inflate(R.layout.popup_image_viewer, PopupLinearLayout(view.context))
        init(contentView, view, xOff, yOff)

        val image = contentView.findViewById<ImageView>(R.id.popup_image)

        // TODO - fix image dimension bug if there is use for this class
        image.loadGraphics(path, filePath)
    }
}